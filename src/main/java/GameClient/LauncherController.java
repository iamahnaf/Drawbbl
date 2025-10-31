package GameClient;

import GameServer.Canva;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class LauncherController {

    @FXML
    void handleDrawerClick(ActionEvent event) {
        try {
            // Create an instance of the server application
            Canva serverApp = new Canva();
            Stage serverStage = new Stage();
            // Start the server UI
            serverApp.start(serverStage);

            // Close the launcher window
            closeLauncher(event);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle potential exceptions during server startup
        }
    }

    @FXML
    void handleGuesserClick(ActionEvent event) {
        try {

            App clientApp = new App();
            Stage clientStage = new Stage();
            // Start the client UI
            clientApp.start(clientStage);

            // Close the launcher window
            closeLauncher(event);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * A helper method to close the current launcher window.
     */
    private void closeLauncher(ActionEvent event) {
        // Get the source of the event (the button) and find its parent stage
        Stage launcherStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        launcherStage.close();
    }
}