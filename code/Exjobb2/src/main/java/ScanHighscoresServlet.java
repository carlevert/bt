import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;
import net.carlevert.Scoreboard.Highscore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by carlevert on 4/21/16.
 */
public class ScanHighscoresServlet extends HttpServlet {

    private static float GROWTH = 0.02f;
    private static int START_BUCKET_SIZE = 10;

    private static final Logger log = Logger.getLogger(ScanHighscoresServlet.class.getName());

    public static void scanHighscores() {

        ofy().delete().key(RankEstimator.getKey()).now();

        RankEstimator rankEstimator = RankEstimator.getInstance();

        Query<Highscore> highscoreQuery = ofy().load().type(Highscore.class).order("score");
        QueryResultIterator<Highscore> iterator = highscoreQuery.iterator();

        int startRank = 1;
        while (iterator.hasNext()) {

            int bucketSize = getBucketSize(startRank);

            Highscore highscore = iterator.next();

            // The highest score for the bucket (numerically lowest)
            int startScore = highscore.getScore();

            // Skip to the last highscore in the bucket
            // First highscore in this iteration is consumed above, hence start i from 1
            int i;
            for (i = 1; i < bucketSize && iterator.hasNext(); i++)
                highscore = iterator.next();

            rankEstimator.newBucket(startScore, startRank, i);

            // Add a final empty bucket
            if (!iterator.hasNext())
                rankEstimator.newBucket(highscore.getScore() + 1, startRank + i, 0);

            startRank += bucketSize;

        }

        ofy().save().entity(rankEstimator).now();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Measure execution time
        long startTime = System.currentTimeMillis();

        scanHighscores();

        long stopTime = System.currentTimeMillis();

        // Output homebaked JSON with time in millis
        resp.getWriter().println("{\"time\":" + (stopTime - startTime) + "}");

    }

    /**
     * Returns the bucket size for the page starting at rank startRank
     *
     * @param startRank the start rank
     * @return the bucket size
     */
    private static int getBucketSize(int startRank) {
        float GROWTH = 0.03f;
        int START_BUCKET_SIZE = 20;
        int bucketSize = (int) (startRank * GROWTH) + START_BUCKET_SIZE;
        return bucketSize;

    }

}

