package net.carlevert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by carlevert on 4/29/16.
 */
public class Distr {

    int numPages;
    int ranks[];
    int scores[];


    public Distr(String filename) {


        try {
            Path path = Paths.get(filename);
            List<String> lines = Files.readAllLines(path);
            if (lines.size() != 2)
                throw new Exception("Expected two lines");

            String[] ranks = lines.get(0).split(",");
            String[] scores = lines.get(1).split(",");
            if (ranks.length != scores.length)
                throw new Exception("Ranks and scores have different length");

            for (int i = 0; i < ranks.length; i++) {
                ranks[i] = ranks[i].replace("\"", "");
                scores[i] = scores[i].replace("\"", "");
            }

            int[] r = new int[ranks.length];
            int[] s = new int[scores.length];
            for (int i = 0; i < ranks.length; i++) {
                r[i] = Integer.parseInt(ranks[i]);
                s[i] = Integer.parseInt(scores[i]);
                System.out.println(r[i] + "\t" + s[i]);
            }



            /* System.err.println("Num pages: " + ranks.length);
            for (int i = 0; i < ranks.length - 1; i++) {
                int pageSize = r[i + 1] - r[i];
                int scoreRange = s[i + 1] - s[i];

                for (int j = 0; j < pageSize; j++) {

                    int interpolatedRank = r[i] + j;
                    System.out.print("" + interpolatedRank + "\t");

                    int interpolatedScore = s[i] + j * (scoreRange / pageSize);

                    System.out.println(interpolatedScore);
                }
            } */

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }


    }

}
