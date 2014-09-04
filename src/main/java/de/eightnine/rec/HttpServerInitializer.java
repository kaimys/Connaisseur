package de.eightnine.rec;

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

    public HttpServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }

        // This is used on HttpHelloWorldServer
        p.addLast(new HttpServerCodec());
        // This is used on HttpSnoopServer
        //p.addLast(new HttpRequestDecoder());
        //p.addLast(new HttpResponseEncoder());

        // Uncomment the following line if you want automatic content compression.
        //p.addLast(new HttpContentCompressor());

        switch(0) {
            case 0:
                // If you don't want to handle HttpChunks.
                p.addLast(new HttpObjectAggregator(1048576));
                p.addLast(new HttpRecommendationServerHandler());
                break;

            case 1:
                p.addLast(new HttpSnoopServerHandler());
                break;

            case 2:
                p.addLast(new HttpHelloWorldServerHandler());
                break;
        }

    }

}
