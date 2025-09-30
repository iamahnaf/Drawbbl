package GameClient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("room.fxml"));
        Scene scene = new Scene(root, 1152, 768);
        Image appIcon = new Image(getClass().getResourceAsStream("icon.png"));
        stage.getIcons().add(appIcon);
        stage.setResizable(false);          // optional

       // stage.setMaxWidth(1152);
        //stage.setMaxHeight(768);
        stage.setTitle("Drawbbl");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}