import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.cmd.Query;
import net.carlevert.Scoreboard.Highscore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Entity
@Cache
public class RankEstimator {

    private static final Logger log = Logger.getLogger(RankEstimator.class.getName());

    private static final Long ID = new Long(1L);

    private static final int MIN_BUCKET_INDEX_TO_ESTIMATE = 3;

    @Id
    private Long id;

    public List<Bucket> buckets;

    public RankEstimator() {
        id = new Long(ID);
        buckets = new ArrayList<>();
    }

    public void newBucket(int startScore, int startRank, int pageSize) {
        Bucket bucket = new Bucket(startScore, startRank, pageSize);
        buckets.add(bucket);
    }

    public int getRankEstimate(int newScore, int newIndex) {

        // Adjust lower score on first page if necessary
        Bucket firstBucket = buckets.get(0);
        if (newScore < firstBucket.startScore) {
            firstBucket.startScore = newScore;
            ofy().save().entity(this).now();
            return 1;
        }

        int bucketIndex = getBucketIndexForScore(newScore, 0);

        if (bucketIndex < MIN_BUCKET_INDEX_TO_ESTIMATE) {
            int realRank = getRealRank(newScore);
            return realRank;
        }

        int estimate = estimate(newScore, bucketIndex);

        // float relativeError0 = Math.abs(((float) estimate / (newIndex + 1)) - 1.0f);

        return estimate;

    }

    public int getRankEstimate(int newScore, int oldScore, int newIndex) {
        int bucketIndex = adjustPages(newScore, oldScore);

        ofy().save().entity(this).now();

        float relativeError0;

        if (bucketIndex < MIN_BUCKET_INDEX_TO_ESTIMATE) {
            int realRank = getRealRank(newScore);

            relativeError0 = Math.abs(((float) realRank / (newIndex + 1)) - 1.0f);


            return realRank;
        }

        int estimate = estimate(newScore, bucketIndex);

        relativeError0 = Math.abs(((float) estimate / (newIndex + 1)) - 1.0f);

        String bucket1 = buckets.get(bucketIndex).toString();
        String bucket2 = buckets.get(bucketIndex + 1).toString();



        return estimate;
    }

    private int getRealRank(int score) {

        Query<Highscore> highscoreQuery;
        highscoreQuery= ofy().load().type(Highscore.class).order("score");
        QueryResultIterator<Highscore> iterator = highscoreQuery.iterator();

        int rank = 1;
        Highscore highscore;
        while (iterator.hasNext()) {
            highscore = iterator.next();
            if (highscore.getScore() >= score)
                break;
            rank++;
        }

        return rank;
    }

    private int estimate(int score, int bucketIndex) {
        Bucket bucket = buckets.get(bucketIndex);
        int scoreSpan = buckets.get(bucketIndex + 1).startScore - bucket.startScore;
        double where = (double) (score - bucket.startScore) / scoreSpan;
        int rankEstimate = bucket.startRank + (int) (Math.ceil(bucket.getBucketSize() * where));
        return rankEstimate;
    }

    private int adjustPages(int newScore, int oldScore) {

        // Adjust lower score on first page if necessary
        Bucket firstBucket = buckets.get(0);

        // Index for page containing new score
        int newIndex;
        if (newScore < firstBucket.startScore) {
            firstBucket.startScore = newScore;
            newIndex = 0;
        } else {
            newIndex = getBucketIndexForScore(newScore, 0);
        }

        int oldIndex = getBucketIndexForScore(oldScore, newIndex);

        if (newIndex != oldIndex) {

            buckets.get(newIndex).increaseBucketSize();
            buckets.get(oldIndex).decreaseBucketSize();


            if (buckets.get(oldIndex).getBucketSize() == 0)
                buckets.remove(oldIndex);

            // Adjust startRank for buckets
            for (int i = newIndex + 1; i < buckets.size(); i++) {
                buckets.get(i).startRank = buckets.get(i - 1).startRank + buckets.get(i - 1).getBucketSize();
            }

        }


        return newIndex;
    }

    private int getBucketIndexForScore(int score, int startFrom) {
/*
        int bIndex = Collections.binarySearch(buckets, new Bucket(score, 0, 0));

        if (bIndex < 0)
            bIndex = -bIndex - 2;
        else {
            bIndex--;
            log.info("bIndex >= 0: " + bIndex
                    + " score: " + score
                    + " bucket startscore: " + buckets.get(bIndex).startScore);
        }


        return bIndex;
*/
        for (int i = (startFrom + 1); i < buckets.size(); i++) {
            if (buckets.get(i).startScore > score) {
/*                log.info("getBucketIndexForScore: " + bIndex + " " + (i - 1));
                assert bIndex == (i -1) : bIndex; */
                return i - 1;
            }
        }




        if (score > buckets.get(buckets.size() - 1).startScore)
            return (buckets.size() - 1);

        assert false;

        return -1;

    }

    public static Key<RankEstimator> getKey() {
        Key<RankEstimator> key = Key.create(null, RankEstimator.class, ID);
        return key;
    }

    public static class Bucket implements Comparable<Bucket> {

        public int startScore;
        public int startRank;
        private int bucketSize;

        public Bucket() {
        }

        public Bucket(int startScore, int startRank, int bucketSize) {
            this.startScore = startScore;
            this.startRank = startRank;
            this.bucketSize = bucketSize;
        }

        @Override
        public String toString() {
            return "startScore: " + startScore + "bucketSize: " + bucketSize + " startRank: " + startRank;
        }

        public void increaseBucketSize() {
            bucketSize++;
        }

        public void decreaseBucketSize() {
            bucketSize--;
        }

        public int getBucketSize() {
            return bucketSize;
        }


        @Override
        public int compareTo(Bucket b) {
            return startScore - b.startScore ;
        }

    }

    public static RankEstimator getInstance() {

        RankEstimator rankEstimator;

        try {
            Key<RankEstimator> key = Key.create(null, RankEstimator.class, ID);
            rankEstimator = ofy().load().key(key).safe();
        } catch (NotFoundException e) {
            rankEstimator = new RankEstimator();
            ofy().save().entity(rankEstimator).now();
        }

        return rankEstimator;

    }

}