package GameClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class ModeSelectionController {

    @FXML
    private Label welcomeLabel;

    public void initialize(String username) {
        GameContext.setLoggedInUsername(username);
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    @FXML
    private void handleNormalMode(ActionEvent event) {
        GameContext.setGameMode("NORMAL");
        navigateToLauncher(event);
    }

    @FXML
    private void handleCsMode(ActionEvent event) {
        GameContext.setGameMode("CS");
        navigateToLauncher(event);
    }

    @FXML
    private void handleScoreboard(ActionEvent event) throws IOException {
        // --- THIS IS THE FIX ---
        // Load the Scoreboard.fxml screen
        Parent root = FXMLLoader.load(getClass().getResource("Scoreboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("My Scores");
        // --- END OF FIX ---
    }

    private void navigateToLauncher(ActionEvent event) {
        try {
            // We need to get the server to re-read the word file
            // This is a placeholder for a more robust client-server command system
            System.out.println("Selected mode: " + GameContext.getGameMode());

            Parent root = FXMLLoader.load(getClass().getResource("Launcher.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Drawbbl Launcher");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Add this new method to the class
    @FXML
    private void handleLeaderboard(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Leaderboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Global Leaderboard");
    }
}