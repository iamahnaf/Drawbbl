module game.drawbblmaven {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    // Let other modules see your public types
    exports GameServer;
    exports GameClient;
    exports GameAuth;

    // If you use FXML controllers inside GameServer, also open it to javafx.fxml
    opens GameClient to javafx.fxml;
    opens GameServer to javafx.fxml;
    opens GameAuth to javafx.fxml;
}
