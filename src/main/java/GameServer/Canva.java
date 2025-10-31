package GameServer;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Canva  {
    CanvasController controller;
    public void start(Stage stage) throws Exception {
        Server.getWords();
        Server.getStartHandler();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("canva.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        stage.setTitle("Drawbbl");
        Scene scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

}
