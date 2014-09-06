package de.eightnine.rec;

import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

/**
 * Hello world!
 *
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final int handler;

    public HttpServerInitializer(SslContext sslCtx, int handler) {
        this.sslCtx = sslCtx;
        this.handler = handler;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        Logger logger = LoggerFactory.getLogger(HttpServerInitializer.class);

        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }

        // Uncomment the following line if you want automatic content compression.
        //p.addLast(new HttpContentCompressor());

        switch(this.handler) {
            case 0:
                logger.info("Starting HttpRecommendationServerHandler...");
                p.addLast(new HttpServerCodec());
                // If you don't want to handle HttpChunks.
                p.addLast(new HttpObjectAggregator(1048576));
                p.addLast(new HttpRecommendationServerHandler());
                break;

            case 1:
                logger.info("Starting HttpSnoopServerHandler...");
                p.addLast(new HttpRequestDecoder());
                p.addLast(new HttpResponseEncoder());
                p.addLast(new HttpSnoopServerHandler());
                break;

            case 2:
                logger.info("Starting HttpHelloWorldServerHandler...");
                p.addLast(new HttpServerCodec());
                p.addLast(new HttpHelloWorldServerHandler());
                break;

            case 3:
                p.addLast(new HttpServerCodec());
                p.addLast(new HttpObjectAggregator(1048576));
                p.addLast(new HttpRecommendationServerHandler());
                p.addLast(new ChunkedWriteHandler());
                p.addLast(new HttpStaticFileServerHandler());
                break;

            default:
                logger.error("Unknown handler");
                System.exit(1);
        }

    }

}
