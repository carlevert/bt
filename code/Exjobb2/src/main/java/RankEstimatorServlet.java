import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class RankEstimatorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        RankEstimator rankEstimator = RankEstimator.getInstance();

        Map parameterMap = req.getParameterMap();

        if (parameterMap.containsKey("score")) {
            String scoreStr = req.getParameter("score");
            int score = Integer.parseInt(scoreStr);
            int estimate = rankEstimator.getRankEstimate(score, 0);
            resp.getWriter().println(estimate);
            return;
        }

        int i = 0;

        resp.getWriter().println("<table>");
        resp.getWriter().println("<tr>");
        resp.getWriter().print("<td>#</td>");
        resp.getWriter().print("<td>Lower</td>");
        resp.getWriter().print("<td>Pagesize</td>");
        resp.getWriter().print("<td>Start&nbsp;rank</td>");
        resp.getWriter().println("</tr>");

        for (RankEstimator.Bucket bucket : rankEstimator.buckets) {
            resp.getWriter().println("<tr>");
            resp.getWriter().print("<td>" + (++i) + "</td>");
            resp.getWriter().print("<td>" + bucket.startScore + "</td>");
            resp.getWriter().print("<td>" + bucket.getBucketSize()+ "</td>");
            resp.getWriter().print("<td>" + bucket.startRank + "</td>");
            resp.getWriter().println("</tr>");
        }

        resp.getWriter().println("</table>");

    }

}
