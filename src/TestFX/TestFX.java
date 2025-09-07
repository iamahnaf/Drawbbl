package TestFX;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

//EDIT VM
//--module-path "D:\UIU CSE\Codes & Libs\JavaLib\javafx-sdk-24.0.2\lib" --add-modules javafx.controls,javafx.fxml
public class TestFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a button
        Button button = new Button("Click Me");

        button.setOnAction(e -> System.out.println("Button clicked!"));

        // Root layout
        StackPane root = new StackPane(button);

        // Scene
        Scene scene = new Scene(root, 400, 300);

        // Stage setup
        primaryStage.setTitle("JavaFX Test App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
