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
        // Load the new launcher UI
        Parent root = FXMLLoader.load(getClass().getResource("Launcher.fxml"));
        primaryStage.setTitle("Drawbbl Launcher");

        // Optional: Add an icon to the launcher window
        try {
            Image appIcon = new Image(getClass().getResourceAsStream("icon.png"));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.out.println("Launcher icon not found, skipping.");
        }

        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}