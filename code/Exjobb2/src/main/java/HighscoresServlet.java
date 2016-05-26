import com.fasterxml.jackson.databind.ObjectMapper;
import net.carlevert.Scoreboard.Highscore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class HighscoresServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // List<Highscore> highscores = ofy().load().type(Highscore.class).list();
        List<Highscore> highscores = ofy().load().type(Highscore.class).order("score").list();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(resp.getOutputStream(), highscores);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
