package de.eightnine.rec;

import java.io.*;

/**
 * Created by kai on 04.09.14.
 */
public class MovieLens {

    public static void main(String[] args) throws IOException {
        convert();
    }

    public static void convert() throws IOException  {

        BufferedReader br = new BufferedReader(new FileReader("data/movielens/u.data"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("data/movies.csv"));

        String line;
        while((line = br.readLine()) != null) {
            String[] values = line.split("\\t", -1);
            bw.write(values[0] + "," + values[1] + "," + values[2] + "\n");
        }

        br.close();
        bw.close();

    }

}
