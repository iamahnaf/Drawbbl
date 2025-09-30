package GameClient;

import GameClient.LobbyController;
import GameClient.UserData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.concurrent.Task;

//import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

public class AppController {

    @FXML private TextField name;
    @FXML private TextField ip;
    @FXML private Button joinButton;
    @FXML private Button exitButton;        // renamed
    @FXML private Hyperlink helpLink;
    @FXML private Hyperlink aboutLink;
    @FXML
    private Label title; // Add fx:id="title" to your title Label in intro.fxml if not present
    UserData player;

    @FXML
    private void initialize() {

    }

    public void nextScene() {
        String pname = name.getText();
        String address = ip.getText();

        if (pname.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation");
            alert.setHeaderText(null);
            alert.setContentText("Name cannot be Empty..");
            alert.showAndWait();
            return;
        }
        if (pname.equals("SERVER") || pname.equals("Round")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation");
            alert.setHeaderText(null);
            alert.setContentText("Name \"" + pname + "\" is not allowed, please change..");
            alert.showAndWait();
            name.clear();
            return;
        }

        // Disable UI elements while connecting
        joinButton.setDisable(true);
        name.setDisable(true);
        ip.setDisable(true);
       // title.setText("Connecting...");

        // --- Perform networking in a background thread ---
        new Thread(() -> {
            try {
                // Create the UserData object which handles all connections
                player = new UserData(pname, address);

                // If connection is successful, switch scenes on the JavaFX thread
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("lobby.fxml"));
                        Parent root = loader.load();
                        LobbyController controller = loader.getController();
                        controller.setUserData(player);
                        Stage stage = (Stage) joinButton.getScene().getWindow();
                        stage.setScene(new Scene(root));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            } catch (IOException e) {
                // If connection fails, re-enable UI and show an error on the JavaFX thread
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("CONNECTION ERROR");
                    alert.setHeaderText(null);
                    alert.setContentText("No server running at the entered IP address\nPlease recheck it and try again.");
                    alert.showAndWait();
                });
                System.out.println("Server not found..");

            }
        }).start();
    }

    public void handleExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Exit Drawbbl?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) Platform.exit();
        });
    }

    public void openHelp() {
        System.out.println("Help clicked");
    }

    public void openAbout() {
        System.out.println("About clicked");
    }

    public void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}