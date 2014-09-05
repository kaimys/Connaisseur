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
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * Created by kai on 03.09.14.
 */
public class HttpRecommendationServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(HttpRecommendationServerHandler.class);

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
        writeResponse(req, g);
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

    protected void writeResponse(FullHttpRequest req, JsonGenerator g) {
        try {
            // Routing
            String[] path = req.getUri().split("/");
            if(path.length != 3) {
                g.writeStringField("result", "error");
                return;
            } else if(!"items".equals(path[1])) {
                g.writeStringField("result", "file not found");
                return;
            }
            int itemId = Integer.parseInt(path[2]);
            g.writeStringField("result", "ok");

            // Retrieve recommendations
            MovieLens ml = MovieLens.getInstance();
            List<RecommendedItem> recs = ml.recommendSimilarItems(itemId, 5);
            g.writeNumberField("itemId", itemId);
            g.writeArrayFieldStart("recommendations");
            for(RecommendedItem rec : recs) {
                g.writeStartObject();
                g.writeNumberField("id", rec.getItemID());
                g.writeNumberField("weight", rec.getValue());
                g.writeEndObject();
            }
            g.writeEndArray();
        } catch (NumberFormatException e) {
            logger.error("Error retrieving recommendations", e);
        } catch (IOException e) {
            logger.error("Error retrieving recommendations", e);
        } catch (TasteException e) {
            logger.error("Error retrieving recommendations", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        logger.error("Caught exception in channel handler", cause);
        ctx.close();
    }

}
