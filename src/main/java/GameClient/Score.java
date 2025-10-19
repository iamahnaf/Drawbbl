package GameClient;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Score {
    private final SimpleStringProperty username;
    private final SimpleIntegerProperty score;
    private final SimpleStringProperty word; // NEW FIELD
    private final SimpleStringProperty date;

    // MODIFIED CONSTRUCTOR
    public Score(String username, int score, String word, String date) {
        this.username = new SimpleStringProperty(username);
        this.score = new SimpleIntegerProperty(score);
        this.word = new SimpleStringProperty(word); // NEW
        this.date = new SimpleStringProperty(date);
    }

    public String getUsername() { return username.get(); }
    public int getScore() { return score.get(); }
    public String getWord() { return word.get(); } // NEW GETTER
    public String getDate() { return date.get(); }
}