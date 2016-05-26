import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.cmd.Query;
import net.carlevert.Scoreboard.Highscore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class SetupHighscores extends HttpServlet {

    public static int NUM_HIGHSCORES = 100 * 1000;
    private static final Logger log = Logger.getLogger(SetupHighscores.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Map params = req.getParameterMap();

        if (params.containsKey("reset")) {

            Query<Highscore> highscores = ofy().load().type(Highscore.class);
            QueryResultIterator<Highscore> iterator = highscores.iterator();
            Random random = new Random(1000);

            Highscore highscore;
            while (iterator.hasNext()) {
                highscore = iterator.next();

                int score = 0;
                while (score < 1000)
                    score = (int) (1000000.0 * random.nextGaussian() + 2000.0);

                highscore.setScore(score);
                ofy().save().entity(highscore);
            }

            ScanHighscoresServlet.scanHighscores();

        }

        if (params.containsKey("top10")) {
            Query<Highscore> top10 = ofy().load().type(Highscore.class).order("score").limit(30);
            QueryResultIterator<Highscore> iterator = top10.iterator();
            PrintWriter out = resp.getWriter();
            out.println("<pre>");
            int i = 1;
            while (iterator.hasNext()) {
                Highscore h = iterator.next();
                out.println(i++ + " " + h.getUsername() + "&nbsp;" + h.getScore());

            }
            out.println("</pre>");
        }

        if (params.containsKey("create")) {
            Queue queue = QueueFactory.getDefaultQueue();
            queue.add(TaskOptions.Builder.withPayload(new GenerateRandomHighscores()));
            DeferredTask generateRandomHighscores = new GenerateRandomHighscores();
            generateRandomHighscores.run();
            resp.getWriter().println("Creating " + NUM_HIGHSCORES + " highscores.");
        }

        if (params.containsKey("delete")) {

            resp.getWriter().println("Deleting all highscores & rankestimator.");

            Query<RankEstimator> rankEstimators = ofy().load().type(RankEstimator.class);
            for (RankEstimator rankEstimator : rankEstimators)
                ofy().delete().entity(rankEstimator).now();

            String cursorStr = req.getParameter("cursor");
            Query<Highscore> highscoreQuery = ofy().load().type(Highscore.class);

            if (cursorStr == null) {
                QueryResultIterator iterator = highscoreQuery.iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    if (i % 1000 == 0) {
                        Cursor cursor = iterator.getCursor();
                        Queue queue = QueueFactory.getDefaultQueue();
                        TaskOptions taskOptions = TaskOptions.Builder
                                .withUrl("/highscores_admin")
                                .param("cursor", cursor.toWebSafeString())
                                .param("delete", "1")
                                .method(TaskOptions.Method.GET);
                        queue.add(taskOptions);
                    }
                    iterator.next();
                    i++;
                }
            } else {
                highscoreQuery.limit(1000).startAt(Cursor.fromWebSafeString(cursorStr));
                QueryResultIterator<Highscore> iterator = highscoreQuery.iterator();
                while (iterator.hasNext())
                    ofy().delete().entity(iterator.next());
            }

        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }


    public static class GenerateRandomHighscores implements DeferredTask {

        @Override
        public void run() {

            Random random = new Random(1000);

            for (int i = 0; i < SetupHighscores.NUM_HIGHSCORES; i++) {
                Highscore highscore = new Highscore();
                highscore.setUsername("User_" + String.format("%07d", i));

                int score = 0;
                while (score < 1000)
                    score = (int) (1000000.0 * random.nextGaussian() + 2000.0);

                highscore.setScore(score);
                ofy().save().entity(highscore).now();

            }

            log.info("Successfully created " + SetupHighscores.NUM_HIGHSCORES + " highscores");

        }

    }

}

