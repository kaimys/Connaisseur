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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kai on 04.09.14.
 */
public class MovieLens {

    private DataModel model;
    private ItemSimilarity similarity;
    private GenericItemBasedRecommender recommender;
    private Map<Long, Movie> movies;

    private static MovieLens movieLens;

    private MovieLens() throws IOException {
        this.model = new FileDataModel(new File("data/movies.csv"));
        this.similarity = new LogLikelihoodSimilarity(this.model);
        //TanimotoCoefficientSimilarity sim = new TanimotoCoefficientSimilarity(dm);
        this.recommender = new GenericItemBasedRecommender(this.model, this.similarity);
        this.movies = loadMovies("data/movielens/u.item");
    }

    public List<RecommendedItem> recommendSimilarItems(long itemId, int howMany) throws TasteException {
        return recommender.mostSimilarItems(itemId, howMany);
    }

    public Movie getMovieById(long id) {
        return this.movies.get(id);
    }

    public static MovieLens getInstance() throws IOException {
        if(movieLens == null) {
            movieLens = new MovieLens();
        }
        return movieLens;
    }

    public static void main(String[] args) throws IOException {
        convert();
    }

    public static Map<Long, Movie> loadMovies(String filename) throws IOException {
        Map<Long, Movie> result = new HashMap<Long, Movie>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while((line = br.readLine()) != null) {
            String[] values = line.split("\\|", -1);
            /*for(int i=0; i<values.length; i++) {
                System.out.print(values[i] + ", ");
            }
            System.out.println();*/
            Movie m = new Movie();
            m.setId(Integer.parseInt(values[0]));
            m.setTitle(values[1]);
            result.put(m.getId(), m);
        }
        br.close();
        return result;
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

    public static void recommend(int count) {
        try {
            MovieLens ml = MovieLens.getInstance();

            int x=1;
            for(LongPrimitiveIterator items = ml.model.getItemIDs(); items.hasNext();) {
                long itemId = items.nextLong();
                List<RecommendedItem> recommendations = ml.recommendSimilarItems(itemId, 5);

                for(RecommendedItem recommendation : recommendations) {
                    System.out.println(itemId + "," + recommendation.getItemID() + "," + recommendation.getValue());
                }
                x++;
                if(x>count) System.exit(1);
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
