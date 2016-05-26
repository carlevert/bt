import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Test {

    // Number of test runs
    private static final int NUM_TESTS = 100000;

    // Always use same seed in test to generate random numbers
    private static final long RANDOM_SEED = 1000L;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final String DEFAULT_PATH = "/home/carlevert/experiments/2";
    private static final String OUTFILE_PREFIX = "Exp_";

    private static String testType = "a";
    private List<Highscore> highscores;

    private int totalDiff;
    private int totalTime;
    private float relativeError;

    public static void main(String[] args) {

        String timestamp = dateFormat.format(new Date());
        String outputFilename = DEFAULT_PATH + OUTFILE_PREFIX + testType + "_" + timestamp;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFilename);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            PrintStream printStream = new PrintStream(bufferedOutputStream);
            System.setOut(printStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Test test = new Test();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // -----------------------

        testType = "b";

        outputFilename = DEFAULT_PATH + OUTFILE_PREFIX + testType + "_" + timestamp;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFilename);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            PrintStream printStream = new PrintStream(bufferedOutputStream);
            System.setOut(printStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Test test = new Test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Test() throws IOException {

        // Preparations
        resetHighscores();
        loadHighscores();
        sortHighscores();


        System.err.println("Starting test at " + dateFormat.format(new Date()));
        System.err.println();


        Random random = new Random(RANDOM_SEED);

        for (int i = 0; i < NUM_TESTS; i++) {

            // Pick one at random
            Highscore highscore = highscores.get(random.nextInt(highscores.size()));

            // Index in local list
            int prevIndex = highscores.indexOf(highscore);

            // Previous score
            int prevScore = highscore.getScore();

            // Update highscore and post to server
            int newScore = highscore.getScore() - random.nextInt(1000);
            highscore.setScore(newScore);

            // Sort local list and get new index of the highscore
            sortHighscores();
            int newIndex = highscores.indexOf(highscore);

            // Send
            Response response = postHighscore(highscore.getUsername(), highscore.getScore(), newIndex);


            // Index is zero-based rank starts from 1
            int absDiff = Math.abs((newIndex + 1) - response.rankEstimate);

            // Relative error for current test
            float relativeError0 = Math.abs(((float) response.rankEstimate / (newIndex + 1)) - 1.0f);

            totalDiff += absDiff;
            totalTime += response.time;
            relativeError += relativeError0;

            // 1: relativeError
            // 2: relativeError0
            // 3: absDiff
            // 4: totalDiff
            // 5: totalTime
            String outString = relativeError + "\t" + relativeError0 + "\t" + absDiff + "\t"
                    + totalDiff + "\t" + totalTime + "\t" + prevScore + "\t" + newScore;
            System.out.println(outString);

            if (i % 100 == 0) {
                printProgress(i);
                System.err.println(outString);
                printSummary();
            }

        }


        System.err.println("Done " + dateFormat.format(new Date()));

    }

    private void printProgress(int i) {
        float percent = 100 * (float) i / NUM_TESTS;
        System.err.println(i + " of " + NUM_TESTS + " - " + (int) percent + "%");
    }

    private void sortHighscores() {
        Collections.sort(highscores, Highscore.Comparators.ASC);
    }

    private void loadHighscores() throws IOException {
        System.err.println("Loading highscores...");
        URL url = new URL("http://localhost:8080/list_highscores");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);

        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType collectionType = TypeFactory.defaultInstance()
                .constructCollectionType(List.class, Highscore.class);
        highscores = objectMapper.readValue(connection.getInputStream(), collectionType);
        System.err.println("Loaded " + highscores.size() + " highscores");
        connection.disconnect();
    }

    private void resetHighscores() throws IOException {
        System.err.println("Resetting...");
        URL url = new URL("http://localhost:8080/highscores_admin?reset");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(false);
        connection.getInputStream();
        connection.disconnect();
    }

    private Response postHighscore(String username, int score, int newIndex) throws IOException {

        URL url = new URL("http://localhost:8080/highscore?method=" + testType);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        //       connection.setRequestProperty("Content-Type", "binary/octet-stream");
        //       connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)");
        connection.setDoOutput(true);

        Highscore highscore = new Highscore(username, score, newIndex);
        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        objectMapper.writeValue(dataOutputStream, highscore);
        dataOutputStream.close();

        Response response = objectMapper.readValue(connection.getInputStream(), Response.class);

        return response;

    }

    private void printSummary() {
        System.err.println("Total diff: " + totalDiff);
        System.err.println("Total time: " + totalTime);
        System.err.println("Total relative error: " + relativeError);
        System.err.println();
    }


}
