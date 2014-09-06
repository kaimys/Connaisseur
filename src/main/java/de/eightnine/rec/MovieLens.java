package de.eightnine.rec;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

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
    private UserSimilarity userSimilarity;
    private UserNeighborhood neighborhood;
    private UserBasedRecommender userRecommender;
    private Map<Long, Movie> movies;
    private Map<Long, User> users;

    private static MovieLens movieLens;

    private MovieLens() throws Exception {
        this.model = new FileDataModel(new File("data/movies.csv"));
        // Item recommendations
        this.similarity = new LogLikelihoodSimilarity(this.model);
        //TanimotoCoefficientSimilarity sim = new TanimotoCoefficientSimilarity(dm);
        this.recommender = new GenericItemBasedRecommender(this.model, this.similarity);
        // User recommendations
        this.userSimilarity = new PearsonCorrelationSimilarity(this.model);
        this.neighborhood = new ThresholdUserNeighborhood(0.1, this.userSimilarity, this.model);
        this.userRecommender = new GenericUserBasedRecommender(this.model, this.neighborhood, this.userSimilarity);
        // Movie metadata
        this.movies = loadMovies("data/movielens/u.item");
        this.users = loadUser("data/movielens/u.user");
    }

    public List<RecommendedItem> recommendSimilarItems(long itemId, int howMany) throws TasteException {
        return recommender.mostSimilarItems(itemId, howMany);
    }

    public List<RecommendedItem> recommendItemsForUser(long userId, int howMany) throws TasteException {
        return this.userRecommender.recommend(userId, howMany);
    }

    public Movie getMovieById(long id) {
        return this.movies.get(id);
    }

    public User getUserById(long id) {
        return this.users.get(id);
    }

    public static MovieLens getInstance() throws Exception {
        if (movieLens == null) {
            movieLens = new MovieLens();
        }
        return movieLens;
    }

    public static void main(String[] args) throws IOException {
        convert();
    }

    public static Map<Long, Movie> loadMovies(String filename) throws Exception {
        Map<Long, Movie> result = new HashMap<Long, Movie>();
        CsvReader csv = new CsvReader(filename, "latin1");
        while(csv.next()) {
            //csv.printLine();
            Movie m = new Movie();
            m.setId(csv.getInt(0));
            m.setTitle(csv.getString(1));
            m.setReleaseDate(csv.getDate(2));
            m.setUrl(csv.getString(4));
            result.put(m.getId(), m);
        }
        return result;
    }

    public static Map<Long, User> loadUser(String filename) throws Exception {
        Map<Long, User> result = new HashMap<Long, User>();
        CsvReader csv = new CsvReader(filename, "latin1");
        while(csv.next()) {
            //csv.printLine();
            User u = new User();
            u.setId(csv.getInt(0));
            u.setAge(csv.getInt(1));
            u.setGender(csv.getChar(2));
            u.setOccupation(csv.getString(3));
            u.setZip(csv.getString(4));
            result.put(u.getId(), u);
        }
        return result;
    }

    public static void convert() throws IOException {

        BufferedReader br = new BufferedReader(new FileReader("data/movielens/u.data"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("data/movies.csv"));

        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split("\\t", -1);
            bw.write(values[0] + "," + values[1] + "," + values[2] + "\n");
        }

        br.close();
        bw.close();

    }

    public static void recommend(int count) {
        try {
            MovieLens ml = MovieLens.getInstance();

            int x = 1;
            for (LongPrimitiveIterator items = ml.model.getItemIDs(); items.hasNext(); ) {
                long itemId = items.nextLong();
                List<RecommendedItem> recommendations = ml.recommendSimilarItems(itemId, 5);

                for (RecommendedItem recommendation : recommendations) {
                    System.out.println(itemId + "," + recommendation.getItemID() + "," + recommendation.getValue());
                }
                if(++x > count) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("There was an error.");
            e.printStackTrace();
        }
    }

    public static void recommendUser(int count) {
        try {
            MovieLens ml = MovieLens.getInstance();
            int x = 1;
            for (LongPrimitiveIterator items = ml.model.getUserIDs(); items.hasNext(); ) {
                long userId = items.nextLong();
                List<RecommendedItem> recommendations = ml.recommendItemsForUser(userId, 5);
                for (RecommendedItem recommendation : recommendations) {
                    System.out.println(userId + "," + recommendation.getItemID() + "," + recommendation.getValue());
                }
                if(++x > count) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}