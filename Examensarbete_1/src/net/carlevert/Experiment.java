package net.carlevert;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.DeadlineExceededException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static com.googlecode.objectify.ObjectifyService.ofy;

@SuppressWarnings("serial")
public class Experiment extends HttpServlet {

	private static final Logger log = Logger.getLogger(Poem.class.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/plain");
		resp.getWriter().println("Allo");

		Poem poem = new Poem();
		ofy().save().entity(poem).now();

		poem.mutate();

		try {
			long time = ApiProxy.getCurrentEnvironment().getRemainingMillis() - 50;
			log.warning("Sleeping for " + time + " ms. ID: " + poem.getId());
			Thread.sleep(time);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		while (true) {
			ofy().save().entity(poem).now();
			poem.mutate();
		}


	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String url = req.getRequestURL().toString();
		Long id = getExperimentId(url);

		Key<Poem> key = Key.create(Poem.class, id);
		Result<Poem> result = ofy().load().key(key);

		Poem poem = result.now();

		if (poem != null) {
			Response response = new Response();
			response.setId(poem.getId());
			response.setValid(poem.isValid());

			ObjectMapper mapper = new ObjectMapper();
			resp.setContentType("application/json");
			resp.getWriter().println(mapper.writeValueAsString(response));
		} else {
			resp.getWriter().println("Not found");
		}

	}

	private Long getExperimentId(String url) {
		Pattern pattern = Pattern.compile("/experiment/(\\d+)");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			String match = matcher.group(1);
			return new Long(match);
		} else {
			return -1l;
		}
	}

}
