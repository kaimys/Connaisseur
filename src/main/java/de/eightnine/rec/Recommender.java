package de.eightnine.rec;

/**
 * Created by kai on 04.09.14.
 */
public class Recommender {

    public static void main(String[] args) throws Exception {
        if(args.length > 0) {
            if("server".equals(args[0])) {
                HttpServer.startServer();
            } else if("convert".equals(args[0])) {
                MovieLens.convert();
            } else if("recommend".equals(args[0])) {
                MovieLens.recommend();
            }
        } else {
            System.out.println("usage: java de.eightnine.rec.Recommender [ server | convert | recommend ]");
        }
    }

}
