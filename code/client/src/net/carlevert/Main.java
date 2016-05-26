package net.carlevert;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class Main {

    private Map highscores;

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
            return 1;
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

        printWriter.println("username=" + username + "&score=" + score);
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





    public static void main(String[] args) throws Exception {
        // System.out.println(getScore("User_0028188"));
        int i = 0;
        int numScores = 1000 * 1000;
        int mapSize = 0;
        int startRank = 1;
        int pageSize;

        while (mapSize < numScores) {
            pageSize = (int) (startRank * 0.02) + 10;
            mapSize += pageSize;
            System.out.println(++i + ": startRank: " + startRank + " pageSize: " + pageSize + " mapSize: " + mapSize);
            startRank += pageSize;
        }
    }
}
