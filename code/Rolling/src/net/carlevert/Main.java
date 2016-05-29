package net.carlevert;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Main {

    public final List<Double> dLines;
    public final List<Double> rolling;
    public static final int WINDOW = 8000;

    public static String filename = "2Exp_b_23:07:18.csv";
    public static String outFilename = "B" + WINDOW + ".csv";

    public Main(String filename) throws IOException {

        BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFilename));

        dLines = new ArrayList<>();
        Stream<String> lines = Files.lines(Paths.get(filename));
        lines.forEach((line) -> dLines.add(Double.valueOf(line)));

        int numLines = dLines.size();
        System.out.println(numLines + " read");

        rolling = new ArrayList<>(dLines.size());

        Queue<Double> queue = new LinkedList<>();
        for (int i = 0; i < WINDOW; i++) {
            queue.offer(dLines.get(i));
            rolling.add(averageQueue(queue));
        }

        for (int i = WINDOW; i < numLines; i++) {
            queue.remove();
            queue.offer(dLines.get(i));
            rolling.add(averageQueue(queue));
        }

        for (int i = 0; i < numLines; i++) {
            String out = dLines.get(i) + "\t" + rolling.get(i);
                System.out.println(out);
                writer.write(out + "\n");
        }

    }

    public static double averageQueue(Queue<Double> queue) {
        double sum = 0.0;
        Object[] theQueue = queue.toArray();
        for (int i = 0; i < theQueue.length; i++)
            sum += (double) theQueue[i];
        return sum / theQueue.length;
    }

    public static void main(String[] args) {
        try {
            new Main(Main.filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
