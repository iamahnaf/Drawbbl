package GameClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas; // NEW IMPORT
import javafx.scene.canvas.GraphicsContext; // NEW IMPORT
import javafx.scene.control.*;
import javafx.scene.paint.Color; // NEW IMPORT
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.scene.shape.StrokeLineCap; // NEW IMPORT
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import static java.lang.Thread.sleep;

public class CanvaviewController {
    // MODIFIED: Replaced ImageView with Canvas
    @FXML
    private Canvas canvas;
    @FXML
    private TextField message;
    //@FXML
    //private TextArea list;
    @FXML
    private Label displayTimer, playerDisplay, serverLabel;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private TextFlow chatTextFlow;

    private GraphicsContext g;
    private UserData player;
    private ObjectOutputStream dOut;
    private ObjectInputStream dIn;
    private ObjectInputStream drawingIn;
    boolean gameOver = false;

    private int myCurrentScore = 0;

    public void onSave() {
        System.out.println("Saving Image.... (feature needs update for canvas)");
    }

    public void onMessage() {
        message.setOnAction(e -> {
            String str = message.getText();
            str = str.trim();
            if (!str.isEmpty()) {
                try {
                    dOut.writeObject(str);
                    dOut.flush();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                message.clear();
            }
        });
    }

    public void tellServer(String str) {
        try {
            dOut.writeObject(str);
            dOut.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void onExit() {
        System.exit(0);
    }

    // ... (setTimer and setWaitTimer are unchanged) ...
    public Thread setTimer(int duration){
        Thread t=new Thread(() -> {
            try {
                for (int dur=duration;dur>=1;dur--) {
                    int finalDur = dur;
                    Platform.runLater(() -> displayTimer.setText("Timer: "+finalDur));
                    sleep(1000);
                }
                Platform.runLater(() -> displayTimer.setText("Timer: "+0));
            } catch (InterruptedException e) {
                // MODIFICATION: This is expected. Just print a clean message and exit the thread.
                System.out.println("Client timer interrupted.");
                Thread.currentThread().interrupt();
            }
        });
        t.start();
        return t;
    }

    public Thread setWaitTimer(int duration){
        Thread t=new Thread(() -> {
            try {
                for (int dur=duration;dur>=1;dur--){
                    int finalDur = dur;
                    Platform.runLater(() -> displayTimer.setText("Next Round in: "+finalDur));
                    sleep(1000);
                }
                Platform.runLater(() -> displayTimer.setText("Next Round in: "+0));
            } catch (InterruptedException e) {
                //MODIFICATION: This is expected. Just print a clean message and exit the thread.
                        System.out.println("Client wait timer interrupted.");
                Thread.currentThread().interrupt();
            }
        });
        t.start();
        return t;
    }

    // --- THIS IS THE METHOD THAT HANDLES IT ---
    // It accepts the UserData and the initialMessage from the LobbyController.
    public void setUserData(UserData u, String initialMessage) throws IOException {
        player = u;
        dIn = player.ois;
        dOut = player.oos;
        drawingIn = player.drawingIn;
        playerDisplay.setText("PLAYER: " + player.username);

        g = canvas.getGraphicsContext2D();
        g.setLineCap(StrokeLineCap.ROUND);

        chatTextFlow.heightProperty().addListener((observable, oldValue, newValue) -> chatScrollPane.setVvalue(1.0));

        dOut.writeBoolean(true); // Tell server the player is ready
        dOut.flush();

        drawingActionReceiver();
        // It passes the message to the allResponses method.
        allResponses(initialMessage);
    }

    // This version is kept for cases where there is no initial message (like reconnecting directly into a game).
    public void setUserData(UserData u) throws IOException {
        setUserData(u, null);
    }

    // In GameClient/CanvaviewController.java

    // Replace your existing allResponses method with this one.
    public void allResponses(String initialMessage) {
        Thread allres = new Thread(() -> {
            Thread timer = setTimer(0);
            try {
                if (initialMessage != null && initialMessage.startsWith("Round: ")) {
                    timer = setTimer(60);
                    String finalInitialMessage = initialMessage;
                    Platform.runLater(() -> serverLabel.setText(finalInitialMessage));
                    myCurrentScore = 0;
                }

                while (true) {
                    String res = (String) dIn.readObject();

                    // --- (Score tracking logic remains the same) ---
                    String trimmedUsername = player.username.trim();
                    if (res.toLowerCase().contains(trimmedUsername.toLowerCase() + " guessed the word!")) {
                        try {
                            int startIndex = res.indexOf("(+");
                            int endIndex = res.indexOf(" points)");
                            if (startIndex != -1 && endIndex != -1) {
                                String pointsStr = res.substring(startIndex + 2, endIndex);
                                myCurrentScore += Integer.parseInt(pointsStr);
                            }
                        } catch (Exception e) {
                            System.err.println("Could not parse points from message: " + res);
                        }
                    }

                    // --- THIS IS THE NEW LOGIC FOR HANDLING HINTS ---
                    if (res.startsWith("HINT_UPDATE:")) {
                        // Extract the hint (e.g., "a _ p p _ _")
                        String hint = res.substring("HINT_UPDATE:".length());

                        // Update the serverLabel with the new hint
                        Platform.runLater(() -> {
                            String currentText = serverLabel.getText();
                            // Rebuild the label text, replacing the old blanks with the new hint
                            String newText = currentText.substring(0, currentText.indexOf("word: ") + 6) + hint;
                            serverLabel.setText(newText);
                        });
                    }
                    // --- END OF NEW HINT LOGIC ---
                    else if (res.startsWith("Round: ")) {
                        if (timer.isAlive()) timer.interrupt();
                        timer.join();
                        timer = setTimer(60);
                        Platform.runLater(() -> serverLabel.setText(res));
                    } else if (res.equals("ROUND OVER")) {
                        if (timer.isAlive()) timer.interrupt();
                        timer.join();
                        tellServer("IM_DONE_GUESSING");
                        String ans = (String) dIn.readObject();
                        Platform.runLater(() -> serverLabel.setText(ans));
                        timer = setWaitTimer(10);
                    } else if (res.equals("GAME OVER")) {
                        if (timer.isAlive()) timer.interrupt();
                        timer.join();
                        Platform.runLater(() -> loadGameOverScreen(myCurrentScore));
                        break;
                    } else {
                        // All other messages go to the chat
                        Platform.runLater(() -> appendStyledText(res));
                    }
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.err.println("Disconnected from server.");
            }
        });
        allres.start();
    }
    // --- NEW HELPER METHOD FOR CLEANLINESS ---
    private void loadGameOverScreen(int score) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameOver.fxml"));
            Parent root = loader.load();
            GameOverController controller = loader.getController();
            controller.setFinalScore(score);
            Stage stage = (Stage) serverLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(0);
        }
    }

private void appendStyledText(String rawMessage) {
    String message = rawMessage;
    Color messageColor = Color.BLACK;


    int pointsIndex = message.indexOf(" (+");
    if (pointsIndex != -1 && message.contains(" guessed the word!")) {
        message = message.substring(0, pointsIndex);
    }

    if (message.startsWith("STYLE_YELLOW:")) {
        message = message.substring("STYLE_YELLOW:".length());
        messageColor = Color.ORANGE;
    } else if (message.startsWith("STYLE_GREEN:")) {
        message = message.substring("STYLE_GREEN:".length());
        messageColor = Color.LIMEGREEN;
    }

    int colonIndex = message.indexOf(":");

    if (colonIndex > 0) {
        String speaker = message.substring(0, colonIndex);
        String content = message.substring(colonIndex); // Includes the ":"

        Text speakerText = new Text(speaker);
        speakerText.setFont(Font.font("Tlwg Mono", FontWeight.BOLD, 15));
        speakerText.setFill(messageColor);

        Text contentText = new Text(content);
        contentText.setFont(Font.font("Tlwg Mono", FontWeight.NORMAL, 15));
        contentText.setFill(messageColor);

        chatTextFlow.getChildren().addAll(speakerText, contentText, new Text("\n"));
    }
    // Case 2: It's a system message without a colon
    else {
        Text systemText = new Text(message);
        systemText.setFont(Font.font("Tlwg Mono", FontWeight.BOLD, 15));
        systemText.setFill(messageColor);

        if (messageColor == Color.BLACK) {
            systemText.setFill(Color.GRAY);
        }

        chatTextFlow.getChildren().addAll(systemText, new Text("\n"));
    }
}

    // REWRITTEN: This method now receives and processes DrawingAction objects
    public void drawingActionReceiver() {
        new Thread(() -> {
            try {
                while (!gameOver) {
                    // Read the DrawingAction object from the stream
                    DrawingAction action = (DrawingAction) drawingIn.readObject();

                    if (action.getType() == DrawingAction.ActionType.SHUTDOWN) {
                        System.out.println("Received shutdown command from server. Closing drawing receiver.");
                        gameOver = true; // This will cause the loop to terminate
                        continue; // Skip processing and let the loop exit
                    }


                    // Use Platform.runLater to update the UI from this thread
                    Platform.runLater(() -> processDrawingAction(action));
                }
                drawingIn.close();
                player.drawingSocket.close();
            } catch (IOException | ClassNotFoundException e) {

                if (!gameOver) { // Only print if we weren't expecting the game to end.
                    System.err.println("Connection to server lost.");
                    e.printStackTrace();
                }
            } finally {
                // Cleanly close the sockets when the loop is done.
                try {
                    if (drawingIn != null) drawingIn.close();
                    if (player.drawingSocket != null) player.drawingSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void processDrawingAction(DrawingAction action) {
        double fromX = action.getFromX();
        double fromY = action.getFromY();
        double toX = action.getToX();
        double toY = action.getToY();
        double size = action.getSize();

        switch (action.getType()) {
            case BEGIN_PATH:
                // For a new path, we just set the line properties
                g.setLineWidth(size);
                DrawingAction.SerializableColor sColor = action.getColor();
                g.setStroke(new Color(sColor.getRed(), sColor.getGreen(), sColor.getBlue(), sColor.getOpacity()));
                break;
            case MOVE:
                // For a move, we draw a line from the last point to the new one
                g.setLineWidth(size);
                DrawingAction.SerializableColor moveColor = action.getColor();
                g.setStroke(new Color(moveColor.getRed(), moveColor.getGreen(), moveColor.getBlue(), moveColor.getOpacity()));
                g.strokeLine(fromX, fromY, toX, toY);
                break;
            case ERASE:
                // Erasing still works by clearing small rectangles
                g.clearRect(toX - size / 2, toY - size / 2, size, size);
                break;
            case CLEAR:
                g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                break;
        }
    }


}