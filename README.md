Sample Recommender System using Mahout and Netty
================================================

This little projects aims to create a small recommender system based on Mahout and Netty.
It is based on the Netty [HTTP Hello World example](https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example/http/helloworld)
and the [Static Mahout Tutorial](https://github.com/RevBooyah/Static-mahout-recommender-tutorial)

The data directory contains the MovieLens 100k movie ratings. For more information look on
the [MovieLens website](http://grouplens.org/datasets/movielens/).

## Build with all dependencies

    # mvn clean compile assembly:single

## Run REST service
 
    # java -jar target/Recommender-jar-with-dependencies.jar
    or
    # java -cp target/Recommender-jar-with-dependencies.jar de.eightnine.rec.Recommender server
    Sep 02, 2014 5:13:24 PM io.netty.handler.logging.LoggingHandler channelRegistered
    Information: [id: 0x9ead90e3] REGISTERED
    Sep 02, 2014 5:13:24 PM io.netty.handler.logging.LoggingHandler bind
    Information: [id: 0x9ead90e3] BIND(0.0.0.0/0.0.0.0:8080)
    Open your web browser and navigate to http://127.0.0.1:8080/
    Sep 02, 2014 5:13:24 PM io.netty.handler.logging.LoggingHandler channelActive
    Information: [id: 0x9ead90e3, /0:0:0:0:0:0:0:0:8080] ACTIVE

## Run converter

    java -cp target/Recommender-jar-with-dependencies.jar de.eightnine.rec.Recommender convert
 