package GameClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class GameOverController {

    @FXML
    private Label finalScoreLabel;

    // This method receives the score from CanvaviewController
    public void setFinalScore(int score) {
        finalScoreLabel.setText(String.valueOf(score));
    }

    // This method handles the "Back to Main Menu" button click
    @FXML
    private void handleMainMenuButton(ActionEvent event) throws IOException {
        // Load the ModeSelection screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ModeSelection.fxml"));
        Parent root = loader.load();

        // Get its controller and re-initialize it with the logged-in user's name
        ModeSelectionController controller = loader.getController();
        controller.initialize(GameContext.getLoggedInUsername());

        // Get the current stage and set the new scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Drawbbl - Main Menu");
    }
}