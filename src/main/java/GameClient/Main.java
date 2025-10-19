package GameClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // --- THIS IS THE FIX ---
        // Load the new login screen instead of the launcher.
        Parent root = FXMLLoader.load(getClass().getResource("/GameAuth/Login.fxml"));
        primaryStage.setTitle("Drawbbl Login");
        // --- END OF FIX ---

        try {
            Image appIcon = new Image(getClass().getResourceAsStream("icon.png"));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.out.println("Icon not found, skipping.");
        }

        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}