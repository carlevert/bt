package net.carlevert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Test {

    private List<Highscore> highscores;

    public Test() {
        try {
            getAllHighscores();

            postScore("User_0002712", 950);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void getAllHighscores() throws IOException {

        URL url = new URL("http://localhost:8080/highscores");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
        BufferedReader in = new BufferedReader(inputStreamReader);
        StringBuilder response = new StringBuilder();
        String line;
        int i = 0;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        in.close();

        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        CollectionType collectionType = typeFactory.constructCollectionType(List.class, Highscore.class);
        highscores = objectMapper.readValue(response.toString(), collectionType);

        for (Highscore h : highscores) {
            if (i++ > 10)
                continue;
            System.out.println(h.username);
        }

    }


    private static int getScore(String username) throws IOException {

        URL url = new URL("http://localhost:8080/highscore?username=" + username);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader in = new BufferedReader(inputStreamReader);
            String line = in.readLine();
            in.close();
            return Integer.parseInt(line);
        } else {
            System.out.println("Error");
        }

        return 0;

    }

    private static int postScore(String username, int score) throws IOException {

        URL url = new URL("http://localhost:8080/highscore");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);

        printWriter.print("username=" + username + "&score=" + score);
        printWriter.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader in = new BufferedReader(inputStreamReader);
            String line = in.readLine();
            in.close();
            return Integer.parseInt(line);
        } else {
            System.out.println("Error");
        }

        return 0;

    }


}
