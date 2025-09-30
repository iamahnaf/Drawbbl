package GameClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label; // Import Label
import javafx.scene.control.TextField;
import javafx.stage.Stage;

//import javax.swing.*;
import java.io.IOException;

public class IntroController {
    @FXML
    private TextField name, ip;
    @FXML
    private Button go;
    @FXML
    private Label title; // Add fx:id="title" to your title Label in intro.fxml if not present

    UserData player;

    public void initialize() {}

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
        go.setDisable(true);
        name.setDisable(true);
        ip.setDisable(true);
        title.setText("Connecting...");

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
                        Stage stage = (Stage) go.getScene().getWindow();
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

    public void onExit() {
        Platform.exit();
    }
}