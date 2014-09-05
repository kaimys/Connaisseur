package de.eightnine.rec;

import org.apache.commons.cli.*;

/**
 * Created by kai on 04.09.14.
 */
public class Recommender {

    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        if(args.length > 0) {
            String cmd = args[0];
            String[] args2 = new String[args.length-1];
            System.arraycopy(args, 1, args2, 0, args.length-1);

            if("server".equals(cmd)) {
                options.addOption("s", "use-ssl", false, "Use SSL");
                HttpServer.startServer();
            } else if("convert".equals(cmd)) {
                MovieLens.convert();
            } else if("recommend".equals(cmd)) {
                options.addOption("r", "recommendations", true, "Number of recommendations");
                try {
                    CommandLine line = parser.parse(options, args2);
                    int recs = 10;
                    if(line.hasOption("recommendations")) {
                        recs = Integer.parseInt(line.getOptionValue("recommendations"));
                    }
                    MovieLens.recommend(recs);
                } catch(ParseException ex) {
                    System.err.println(ex.getMessage() );
                    usage();
                } catch(NumberFormatException ex) {
                    System.err.println("Invalid number format: " + ex.getMessage() );
                    usage();
                }
            } else {
                usage();
            }
        } else {
            usage();
        }
    }

    public static void usage() {
        System.out.println("usage: java de.eightnine.rec.Recommender [ server | convert | recommend ]");
    }

}
