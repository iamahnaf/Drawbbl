package GameClient;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class LeaderboardEntry {
    private final SimpleStringProperty username;
    private final SimpleIntegerProperty totalScore;

    public LeaderboardEntry(String username, int totalScore) {
        this.username = new SimpleStringProperty(username);
        this.totalScore = new SimpleIntegerProperty(totalScore);
    }

    public String getUsername() {
        return username.get();
    }

    public int getTotalScore() {
        return totalScore.get();
    }
}