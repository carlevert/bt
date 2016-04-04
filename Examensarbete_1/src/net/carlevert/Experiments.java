package net.carlevert;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.googlecode.objectify.Key;

import static com.googlecode.objectify.ObjectifyService.ofy;

@SuppressWarnings("serial")
public class Experiments extends HttpServlet {

	private static final Logger log = Logger.getLogger(Poem.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		List<Poem> poems = ofy().load().type(Poem.class).list();

		Predicate<Poem> invalid = new Predicate<Poem>() {
			@Override
			public boolean apply(Poem input) {
				return !input.isValid();
			}
		};

		if (req.getParameterMap().containsKey("invalid_only")) {
			Collection<Poem> invalids = Collections2.filter(poems, invalid);
			poems.retainAll(invalids);
		}

		ObjectMapper mapper = new ObjectMapper();
		resp.setContentType("application/json");
		resp.getWriter().println(mapper.writeValueAsString(poems));
	}

	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		ofy().delete().type(Poem.class);

		List<Key<Poem>> keys = ofy().load().type(Poem.class).keys().list();
		ofy().delete().keys(keys).now();
		log.info("DELETE EVERYTHING");

	}

}
