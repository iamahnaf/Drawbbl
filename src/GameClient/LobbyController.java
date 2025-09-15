package GameClient;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.ObjectInputStream;

public class LobbyController {
    @FXML
    private TextArea playerList;
    @FXML
    private Label displayTimer;

    private ObjectInputStream dIn;
    private UserData player;

    public void initialize() {
        playerList.appendText("1. SERVER [Artist]\n");
    }
}
