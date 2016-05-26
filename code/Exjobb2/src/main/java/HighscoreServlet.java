import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.objectify.Key;
import net.carlevert.Scoreboard.Highscore;
import net.carlevert.Test.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Purpose of this servlet is to accept new highscores
 */
public class HighscoreServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(HighscoreServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        // ------------------------------

        ObjectMapper objectMapper = new ObjectMapper();
        Highscore newHighscore = objectMapper.readValue(req.getInputStream(), Highscore.class);

        assert newHighscore != null;

        String username = newHighscore.getUsername();
        Key<String> parentKey = Key.create(String.class, "Highscores");
        final Key<Highscore> key = Key.create(parentKey, Highscore.class, username);

        Highscore highscore = ofy().load().key(key).now();
        int oldHighscore = highscore.getScore();
        highscore.setScore(newHighscore.getScore());
        ofy().save().entity(highscore).now();

        RankEstimator rankEstimator = RankEstimator.getInstance();

        int rankEstimate;
        if (req.getParameter("method").equals("a"))
            rankEstimate = rankEstimator.getRankEstimate(newHighscore.getScore(), newHighscore.newIndex);
        else
            rankEstimate = rankEstimator.getRankEstimate(newHighscore.getScore(), oldHighscore, newHighscore.newIndex);

        // ------------------------------

        long stop = System.currentTimeMillis();

        Response response = new Response();
        response.rankEstimate = rankEstimate;
        response.time = stop - start;
        objectMapper.writeValue(resp.getOutputStream(), response);

    }


    /**
     * TODO: Should be something like get all highscores or get one highscore depending on query string
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getParameterMap().containsKey("username")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            Key<String> parentKey = Key.create(String.class, "Highscores");
            Key<Highscore> key = Key.create(parentKey, Highscore.class, req.getParameter("username"));
            Highscore highscore = ofy().load().key(key).now();
            if (highscore == null)
                resp.getWriter().println("-1");
            else
                resp.getWriter().println(highscore.getScore());
        }
    }

}
