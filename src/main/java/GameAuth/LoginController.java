package GameAuth;

import GameClient.ModeSelectionController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserManager userManager = new UserManager();

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (userManager.authenticate(username, password)) {
            // On successful login, navigate to the ModeSelection screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameClient/ModeSelection.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the username to it
            ModeSelectionController controller = loader.getController();
            controller.initialize(username);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Drawbbl - Main Menu");
            stage.show();
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void switchToRegister(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/GameAuth/Register.fxml"));
        stage.setScene(new Scene(root));
    }
}