package GameClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas; // NEW IMPORT
import javafx.scene.canvas.GraphicsContext; // NEW IMPORT
import javafx.scene.control.*;
import javafx.scene.paint.Color; // NEW IMPORT
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.scene.shape.StrokeLineCap; // NEW IMPORT

import static java.lang.Thread.sleep;

public class CanvaviewController {
    // MODIFIED: Replaced ImageView with Canvas
    @FXML
    private Canvas canvas;
    @FXML
    private TextField message;
    @FXML
    private TextArea list;
    @FXML
    private Label displayTimer, playerDisplay, serverLabel;

    private GraphicsContext g; // NEW: GraphicsContext for the canvas
    private UserData player;
    private ObjectOutputStream dOut;
    private ObjectInputStream dIn; // For game state strings
    private ObjectInputStream drawingIn; // For DrawingAction objects
    boolean gameOver = false;

    // The 'onSave' method will no longer work as easily without an ImageView,
    // but you can snapshot the canvas if you need to re-implement it.
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
            } catch (InterruptedException e) { e.printStackTrace(); }
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
            } catch (InterruptedException e) { e.printStackTrace(); }
        });
        t.start();
        return t;
    }

    public void setUserData(UserData u) throws IOException {
        player = u;
        dIn = player.ois;
        dOut = player.oos;
        drawingIn = player.drawingIn; // Get the drawing input stream
        playerDisplay.setText("PLAYER: " + player.username);

        // MODIFIED: Initialize GraphicsContext with rounded lines
        g = canvas.getGraphicsContext2D();
        g.setLineCap(StrokeLineCap.ROUND);


        dOut.writeBoolean(true);
        dOut.flush();   //player ready==true

        drawingActionReceiver(); // Start the new receiver
        allResponses();
    }

    // REWRITTEN: This method now receives and processes DrawingAction objects
    public void drawingActionReceiver() {
        new Thread(() -> {
            try {
                while (!gameOver) {
                    // Read the DrawingAction object from the stream
                    DrawingAction action = (DrawingAction) drawingIn.readObject();

                    // Use Platform.runLater to update the UI from this thread
                    Platform.runLater(() -> processDrawingAction(action));
                }
                drawingIn.close();
                player.drawingSocket.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // MODIFIED: This method now handles line drawing
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

    // DELETED: The old imageReceiver method is no longer needed.
    // public void imageReceiver() { ... }

    // ... (allResponses method is unchanged) ...
    public void allResponses(){
        Thread allres=new Thread(() -> {
            Thread timer=setTimer(0);
            try {
                while(true){
                    String res = (String) dIn.readObject();
                    if(res.startsWith("Round: ")){
                        timer.join();
                        timer = setTimer(90);
                        Platform.runLater(()->serverLabel.setText(res));
                    } else if(res.equals("ROUND OVER")) {
                        timer.join();
                        tellServer("IM_DONE_GUESSING");
                        String ans=(String) dIn.readObject();
                        Platform.runLater(()->serverLabel.setText(ans));
                        timer = setWaitTimer(15);
                    } else if(res.equals("GAME OVER")) {
                        timer.join();
                        String ans=(String) dIn.readObject();
                        Platform.runLater(()->serverLabel.setText(ans));
                        timer=setTimer(10);
                        timer.join();
                        Platform.runLater(() -> serverLabel.setText(res));
                        timer=setTimer(10);
                        timer.join();
                        player.server.close();
                        System.exit(0);
                    } else Platform.runLater(()->list.appendText(res+"\n"));
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) { e.printStackTrace(); }
        });
        allres.start();
    }
}