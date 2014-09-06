package de.eightnine.rec;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;

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
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "latin1"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split("\\|", -1);
            if (false) {
                for (int i = 0; i < values.length; i++) {
                    System.out.print(values[i] + ", ");
                }
                System.out.println();
            }
            Movie m = new Movie();
            m.setId(Integer.parseInt(values[0]));
            m.setTitle(values[1]);
            if (!"".equals(values[2])) {
                m.setReleaseDate(formatter.parse(values[2]));
            }
            m.setUrl(values[4]);
            result.put(m.getId(), m);
        }
        br.close();
        return result;
    }

    public static Map<Long, User> loadUser(String filename) throws Exception {
        Map<Long, User> result = new HashMap<Long, User>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "latin1"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split("\\|", -1);
            if (false) {
                for (int i = 0; i < values.length; i++) {
                    System.out.print(values[i] + ", ");
                }
                System.out.println();
            }
            User u = new User();
            u.setId(Integer.parseInt(values[0]));
            u.setAge(Integer.parseInt(values[1]));
            u.setGender(values[2].charAt(0));
            u.setOccupation(values[3]);
            u.setZip(values[4]);
            result.put(u.getId(), u);
        }
        br.close();
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