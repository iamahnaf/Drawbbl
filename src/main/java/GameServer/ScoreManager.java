package GameServer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreManager {
    private static final String SCORE_FILE = "scores.txt";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");


    public static synchronized void saveScore(String username, int score, String word) { // MODIFIED
        try (FileWriter fw = new FileWriter(SCORE_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw))
        {
            String now = dtf.format(LocalDateTime.now());
            // MODIFIED: Added the word to the output
            out.println(username + "," + score + "," + word + "," + now);
        } catch (IOException e) {
            System.err.println("Could not write to score file: " + e.getMessage());
        }
    }
}