package de.eightnine.rec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.ReferenceCountUtil;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * Created by kai on 03.09.14.
 */
public class HttpRecommendationServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(HttpRecommendationServerHandler.class);
    private String[] path;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.info("channelReadComplete");
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        logger.info("channelRead0: " + req.getUri());
        boolean jsonp = false;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if(jsonp) {
            os.write("rec_callback(".getBytes("ASCII"));
        }
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createGenerator(os);
        g.writeStartObject();
        g.writeObjectFieldStart("request");
        g.writeStringField("method", req.getMethod().toString());
        g.writeStringField("version", req.getProtocolVersion().toString());
        g.writeStringField("uri", req.getUri());
        g.writeStringField("host", HttpHeaders.getHost(req, "unknown"));
        g.writeEndObject();
        g.writeObjectFieldStart("response");

        // Routing
        path = req.getUri().split("/");
        if(path.length <= 1) {
            g.writeStringField("result", "file not found");
        } else if("items".equals(path[1]))  {
            writeResponseItemRec(req, g);
        } else if("itemSearch".equals(path[1]))  {
            writeResponseItemSearch(req, g);
        } else if("user".equals(path[1]))  {
            writeResponseUserRec(req, g);
        } else {
            g.close();
            os.close();
            logger.info("RefCnt=" + Integer.toString(req.refCnt()));
            req.retain();
            ctx.fireChannelRead(req);
            //ReferenceCountUtil.release(req);
            return;
            //g.writeStringField("result", "file not found");
        }

        g.writeEndObject();
        g.writeEndObject();
        g.close();
        if(jsonp) {
            os.write(");".getBytes("ASCII"));
        }

        if (HttpHeaders.is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(os.toByteArray()));
        if(jsonp) {
            res.headers().set(CONTENT_TYPE, "text/javascript");
        } else {
            res.headers().set(CONTENT_TYPE, "application/json");
        }
        res.headers().set(CONTENT_LENGTH, res.content().readableBytes());
        if (!HttpHeaders.isKeepAlive(req)) {
            ctx.write(res).addListener(ChannelFutureListener.CLOSE);
        } else {
            res.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.write(res);
        }
    }

    private void writeResponseUserRec(FullHttpRequest req, JsonGenerator g) {
        try {
            if (path.length != 3) {
                g.writeStringField("result", "error");
                return;
            }
            int userId = Integer.parseInt(path[2]);
            // Retrieve recommendations
            MovieLens ml = MovieLens.getInstance();
            User u = ml.getUserById(userId);
            if(u == null) {
                g.writeStringField("result", "user not found");
                return;
            }
            g.writeNumberField("id", userId);
            g.writeStringField("zip", u.getZip());
            g.writeNumberField("age", u.getAge());
            g.writeStringField("gender", Character.toString(u.getGender()));
            g.writeStringField("occupation", u.getOccupation());
            g.writeStringField("result", "ok");
            List<RecommendedItem> recs = ml.recommendItemsForUser(userId, 5);
            g.writeArrayFieldStart("recommendations");
            for(RecommendedItem rec : recs) {
                Movie m = ml.getMovieById(rec.getItemID());
                g.writeStartObject();
                g.writeNumberField("id", rec.getItemID());
                g.writeNumberField("weight", rec.getValue());
                g.writeStringField("title", m.getTitle());
                g.writeStringField("url", m.getUrl());
                g.writeNumberField("releaseDate", m.getReleaseDate().getTime() / 1000);
                g.writeEndObject();
            }
            g.writeEndArray();
        } catch(Exception e) {
            logger.error("Error retrieving user recommendations", e);
        }
    }

    private void writeResponseItemSearch(FullHttpRequest req, JsonGenerator g) {
        try {
            if(path.length != 3) {
                g.writeStringField("result", "error");
                return;
            }
            // Retrieve recommendations
            MovieLens ml = MovieLens.getInstance();
            List<Movie> movies = ml.searchMovies(path[2]);
            if(movies.isEmpty()) {
                g.writeStringField("result", "movie not found");
                return;
            }
            g.writeStringField("result", "ok");
            g.writeStringField("query", path[2]);
            g.writeArrayFieldStart("searchResult");
            Iterator<Movie> i = movies.iterator();
            while(i.hasNext()) {
                Movie m = i.next();
                g.writeStartObject();
                g.writeNumberField("id", m.getId());
                g.writeStringField("title", m.getTitle());
                g.writeStringField("url", m.getUrl());
                g.writeNumberField("releaseDate", m.getReleaseDate().getTime() / 1000);
                g.writeEndObject();
            }
            g.writeEndArray();
        } catch (Exception e) {
            try {
                g.writeStringField("result", e.getMessage());
            } catch (IOException e1) {
                logger.error("Could not return error as JSON", e1);
            }
            logger.error("Error retrieving item recommendations", e);
        }
    }

    protected void writeResponseItemRec(FullHttpRequest req, JsonGenerator g) {
        try {
            if(path.length != 3) {
                g.writeStringField("result", "error");
                return;
            }
            int itemId = Integer.parseInt(path[2]);
            // Retrieve recommendations
            MovieLens ml = MovieLens.getInstance();
            Movie m = ml.getMovieById(itemId);
            if(m == null) {
                g.writeStringField("result", "movie not found");
                return;
            }
            g.writeStringField("result", "ok");
            g.writeNumberField("id", itemId);
            g.writeStringField("title", m.getTitle());
            g.writeStringField("url", m.getUrl());
            g.writeNumberField("releaseDate", m.getReleaseDate().getTime() / 1000);
            List<RecommendedItem> recs = ml.recommendSimilarItems(itemId, 5);
            g.writeArrayFieldStart("recommendations");
            for(RecommendedItem rec : recs) {
                m = ml.getMovieById(rec.getItemID());
                g.writeStartObject();
                g.writeNumberField("id", rec.getItemID());
                g.writeNumberField("weight", rec.getValue());
                g.writeStringField("title", m.getTitle());
                g.writeStringField("url", m.getUrl());
                g.writeNumberField("releaseDate", m.getReleaseDate().getTime() / 1000);
                g.writeEndObject();
            }
            g.writeEndArray();
        } catch (Exception e) {
            try {
                g.writeStringField("result", e.getMessage());
            } catch (IOException e1) {
                logger.error("Could not return error as JSON", e1);
            }
            logger.error("Error retrieving item recommendations", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Caught exception in channel handler", cause);
        ctx.close();
    }

}
