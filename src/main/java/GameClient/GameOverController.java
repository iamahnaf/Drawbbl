package GameClient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameOverController {

    @FXML
    private Label scoreLabel;

    @FXML
    private Button exitButton;

    /**
     * This method will be called by the CanvaviewController to pass the final score.
     * @param score The final score of the player.
     */
    public void setFinalScore(int score) {
        scoreLabel.setText("Your final score: " + score);
    }

    @FXML
    void handleExitClick(ActionEvent event) {
        // Exit the application cleanly.
        Platform.exit();
        System.exit(0);
    }
}