package de.eightnine.rec;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import java.io.*;
import java.util.List;

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

    public static void recommend() {
        try {
            DataModel dm = new FileDataModel(new File("data/movies.csv"));

            ItemSimilarity sim = new LogLikelihoodSimilarity(dm);
            //TanimotoCoefficientSimilarity sim = new TanimotoCoefficientSimilarity(dm);

            GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dm, sim);

            int x=1;
            for(LongPrimitiveIterator items = dm.getItemIDs(); items.hasNext();) {
                long itemId = items.nextLong();
                List<RecommendedItem> recommendations = recommender.mostSimilarItems(itemId, 5);

                for(RecommendedItem recommendation : recommendations) {
                    System.out.println(itemId + "," + recommendation.getItemID() + "," + recommendation.getValue());
                }
                x++;
                if(x>10) System.exit(1);
            }



        } catch (IOException e) {
            System.out.println("There was an error.");
            e.printStackTrace();
        } catch (TasteException e) {
            System.out.println("There was a Taste Exception");
            e.printStackTrace();
        }
    }

}
