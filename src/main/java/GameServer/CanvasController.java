package GameServer;

import GameClient.DrawingAction;
import javafx.application.Platform;
//import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color; // Import JavaFX Color


import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.scene.shape.StrokeLineCap; // NEW IMPORT

import static java.lang.Thread.sleep;

public class CanvasController {
    @FXML
    private Canvas canvas;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextField message;
    @FXML
    private CheckBox eraser;
    @FXML
    private TextArea list;
    @FXML
    private Slider brushSize;
    @FXML
    private Button clear;
    @FXML
    private Label displayTimer, wordLabel;
    private GraphicsContext g;

    // NEW: Variables to store the last mouse position
    private double lastX, lastY;

    File newFile = new File("cursor.png");
    private final Image penCursor = new Image(newFile.toURI().toString());
    private CheckStatus checker = new CheckStatus();

    @FXML
    public void initialize() {
        new Thread(this::gameHandler).start();

        brushSize.setMax(100);
        brushSize.setValue(8);
        g = canvas.getGraphicsContext2D();
        g.setLineCap(StrokeLineCap.ROUND);  // Makes lines look smoother
        canvas.setCursor(new ImageCursor(penCursor, 0, penCursor.getHeight()));
        list.setWrapText(true);

        // NEW: Handle the start of a drawing path
        canvas.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();

            double size = brushSize.getValue();
            DrawingAction action;

            if (eraser.isSelected()) {
                g.clearRect(lastX - size / 2, lastY - size / 2, size, size);
                action = new DrawingAction(DrawingAction.ActionType.ERASE, lastX, lastY, lastX, lastY, size, null);
            } else {
                g.setStroke(colorPicker.getValue());
                g.setLineWidth(size);
                // We don't draw anything here, just begin the path
                action = new DrawingAction(DrawingAction.ActionType.BEGIN_PATH, lastX, lastY, lastX, lastY, size, getSerializableColor());
            }
            sendDrawingAction(action);
        });

        // MODIFIED: This now draws lines to connect the dots
        canvas.setOnMouseDragged(e -> {
            double size = brushSize.getValue();
            double currentX = e.getX();
            double currentY = e.getY();
            DrawingAction action;

            if (eraser.isSelected()) {
                g.clearRect(currentX - size / 2, currentY - size / 2, size, size);
                action = new DrawingAction(DrawingAction.ActionType.ERASE, lastX, lastY, currentX, currentY, size, null);
            } else {
                g.setStroke(colorPicker.getValue());
                g.setLineWidth(size);
                g.strokeLine(lastX, lastY, currentX, currentY);
                action = new DrawingAction(DrawingAction.ActionType.MOVE, lastX, lastY, currentX, currentY, size, getSerializableColor());
            }

            // Update the last position
            lastX = currentX;
            lastY = currentY;

            sendDrawingAction(action);
        });

        clear.setOnAction(e -> ClearCanvas());
    }
    //Helper method to get the serializable color
    private DrawingAction.SerializableColor getSerializableColor() {
        Color color = colorPicker.getValue();
        return new DrawingAction.SerializableColor(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
    }

    public void ClearCanvas() {
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        sendDrawingAction(new DrawingAction());
    }


    public synchronized void sendDrawingAction(DrawingAction action) {
        try {
            for (ObjectOutputStream oos : Server.drawingOut) {
                oos.writeObject(action);
                oos.flush();
                // We can also reset the stream to prevent memory buildup from object caching
                oos.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setWord(String word){
        wordLabel.setText("Draw this : "+word);
    }

    public void onSave() {
        System.out.println("Saving image...");
    }

    public void onMessage() {
        message.setOnAction(e -> {
            String str = message.getText();
            if (!str.isEmpty()){
                list.appendText("Server: " + str + "\n");
                try {
                    sendResOut("Server: "+str);
                } catch (IOException ioe) { ioe.printStackTrace(); }
                message.clear();
            }
        });
    }

    public void onExit() { System.exit(0); }

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
                System.out.println("Server timer interrupted, finishing early.");
                Thread.currentThread().interrupt(); // Preserve the interrupted status
            }
        });
        t.start();
        return t;
    }

    public Thread setWaitTimer(int duration){
        Thread t=new Thread(() -> {
            try {
                for (int dur=duration;dur>=1;dur--) {
                    int finalDur = dur;
                    Platform.runLater(() -> displayTimer.setText("Next Round in: "+finalDur));
                    sleep(1000);
                }
                Platform.runLater(() -> displayTimer.setText("Next Round in: "+0));
            } catch (InterruptedException e) {
                // MODIFICATION: This is expected. Just print a clean message and exit the thread.
                System.out.println("Server wait timer interrupted.");
                Thread.currentThread().interrupt(); // Preserve the interrupted status
            }
        });
        t.start();
        return t;
    }

    public void gameHandler() {
        int pc=Server.playerCount;

        for(int round=1 ; round<=Server.rounds ; round++) {
           // RESET THE COUNTER AT THE START OF EACH ROUND ---
            Server.correctGuessCount = 0;
            Platform.runLater(this::ClearCanvas);
            System.out.print("GameHandler round: " + round);
            String word = Server.words.get((int) (Math.random() * Server.words.size()));
            System.out.println(", Word chosen: " + word);
            String wordLength=getWordLength(word);
            try {
                sendResOut("Round: "+round+"   word: "+wordLength);
            } catch (IOException e) { e.printStackTrace(); }

            Platform.runLater(() -> setWord(word));
            Thread timer=setTimer(60),waitTimer;
            Thread[] play=new Thread[pc];
            for(int pnum=0;pnum<pc;pnum++) {
                int finalPnum = pnum;
                // Pass the 'play' array to the gamePlay method
                play[pnum] = new Thread(() -> gamePlay(finalPnum, word, timer, play));
                play[pnum].start();
            }
            try {
                timer.join();
                sendScores(round);
                if(round<Server.rounds) {
                    waitTimer=setWaitTimer(15);
                    sendResOut("ROUND OVER");
                    for(int pnum=0;pnum<pc;pnum++) play[pnum].join();
                    sendResOut("Round: "+round+"   word: "+word);
                    waitTimer.join();
                }
                else {
                    sendResOut("Winner:\n" + Server.getWinners());
                    sendResOut("GAME OVER");
                    sendResOut("Round: " + round + "   word: " + word);

                    // --- MODIFICATION START ---
                    // Send a final shutdown command to all clients before exiting.
                    sendDrawingAction(new DrawingAction(DrawingAction.ActionType.SHUTDOWN, 0, 0, 0, 0, 0, null));
                    // Give a brief moment for the message to be sent.
                    try {
                        sleep(100);
                    } catch (InterruptedException ignored) {}
                    // --- MODIFICATION END ---

                    System.exit(0);
                }
            } catch (InterruptedException | IOException e) { e.printStackTrace(); }

        }
    }
    // Replace the existing gamePlay method in CanvasController.java with this one.
    public void gamePlay(int pnum, String word, Thread timer, Thread[] playerThreads) {
        String pname = Server.names.get(pnum);
        try {
            ObjectInputStream ois = Server.oisList.get(pnum);
            boolean answered = false;
            while (timer.isAlive()) {
                String guess = (String) ois.readObject();
                if (guess.equals("IM_DONE_GUESSING")) break;

                Platform.runLater(() -> list.appendText(pname + ": " + guess + "\n"));
                String lowerCaseGuess = guess.toLowerCase();

                if (word.equals(lowerCaseGuess)) {
                    int score = Server.scoreList.get(pnum);
                    if (!answered && timer.isAlive()) {
                        Server.scoreList.set(pnum, score + 10);
                        // ADD a prefix for correct answers
                        sendResOut("STYLE_GREEN:" + pname + " guessed the word!");
                        answered = true;
                        // --- 2. INCREMENT COUNTER AND CHECK IF ROUND IS OVER ---
                        handleCorrectGuess(timer, playerThreads);
                    } else {
                        sendPrivateMessage(pnum, "You already answered correctly!");
                    }
                } else if (LevenshteinDistance.calculate(word, lowerCaseGuess) == 1) {
                    if (!answered) {
                        // ADD a prefix for "close" hints
                        sendPrivateMessage(pnum, "STYLE_YELLOW:"+lowerCaseGuess+" is close!");
                    }
                   sendResOut(pname + ": " + guess);
                } else {
                    sendResOut(pname + ": " + guess);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
 /*
    public void gamePlay(int pnum,String word,Thread timer){
        String pname=Server.names.get(pnum);
        try {
            ObjectInputStream ois = Server.oisList.get(pnum);
            boolean answered=false;
            while (timer.isAlive()){
                String guess = (String) ois.readObject();
                if(guess.equals("IM_DONE_GUESSING")) break;

                Platform.runLater(()->list.appendText(pname+": "+guess+"\n"));
                if (word.equals(guess.toLowerCase())){
                    int score = Server.scoreList.get(pnum);
                    if(!answered && timer.isAlive()) {
                        Server.scoreList.set(pnum, score+10);
                        sendResOut(pname+": Got it Correct!");
                        answered=true;
                    }
                    else sendResOut(pname+": Already Answered!");
                }
                else sendResOut(pname+": "+guess);
            }
        } catch (IOException | ClassNotFoundException e) {
            try {
                checker.checkPresence();
            } catch (IOException ioException) { ioException.printStackTrace(); }
        }
    }
   */
    public synchronized void sendResOut(String res) throws IOException {
        for(ObjectOutputStream oos:Server.oosList){
            oos.writeObject(res);
            oos.flush();
        }
    }

    public void sendScores(int round) throws IOException {
        if(round==Server.rounds) sendResOut("\nSERVER:\nFINAL SCORES");
        else sendResOut("\nSERVER:\nScores after round:"+round);

        Platform.runLater(()->list.appendText("Scores:\n"));
        int l=Server.names.size();
        for(int i=0;i<l;i++){
            String res=Server.names.get(i)+" - "+Server.scoreList.get(i);
            Platform.runLater(()->list.appendText(res+"\n"));
            sendResOut(res);
        }
    }

    public String getWordLength(String word){
        StringBuilder wl= new StringBuilder();
        for(char c:word.toCharArray()){
            if(c!=' ') wl.append("_ ");
            else wl.append("+ ");
        }
        return wl.toString();
    }

    public synchronized void sendPrivateMessage(int playerIndex, String message) {
        try {
            if (playerIndex >= 0 && playerIndex < Server.oosList.size()) {
                ObjectOutputStream oos = Server.oosList.get(playerIndex);
                oos.writeObject(message);
                oos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add 'Thread[] playerThreads' as a parameter to this method
    private synchronized void handleCorrectGuess(Thread timer, Thread[] playerThreads) {
        Server.correctGuessCount++;
        if (Server.correctGuessCount == Server.playerCount) {
            System.out.println("\nAll players guessed correctly! Ending round early.");

            // Interrupt the main timer
            if (timer.isAlive()) {
                timer.interrupt();
            }

            // NEW: Interrupt all the player threads to break them out of their loops
            for (Thread playerThread : playerThreads) {
                if (playerThread != null && playerThread.isAlive()) {
                    playerThread.interrupt();
                }
            }
        }
    }

}