Connaisseur a Recommender System with Mahout and Netty
======================================================

Connaisseur aims to be a sample recommender system based on Apache Mahout and Netty.
It is currently work in progress. It provides a REST API for integration and an API 
explorer. For quick starting the  MovieLens 100k data set is already included. You 
just have to install Apache Maven to get started.

- https://mahout.apache.org/
- http://netty.io/
- http://grouplens.org/datasets/movielens/

## Quick start

- Install [Maven](http://maven.apache.org/)
- Run `mvn clean compile assembly:single` to build a JAR file with all dependencies
- Convert the MovieLens data to CSV `./recsys convert`
- Start the server `./recsys server`
- Point your browser to [localhost:8080/](http://127.0.0.1:8080/) to start the API explorer.

