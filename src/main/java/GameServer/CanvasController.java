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
import java.util.ArrayList;
import java.util.List;

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


    private double lastX, lastY;

    private final List<Integer> revealedIndices = new ArrayList<>();

    File newFile = new File("cursor.png");
    private final Image penCursor = new Image(newFile.toURI().toString());



    @FXML
    public void initialize() {
        new Thread(this::gameHandler).start();

        brushSize.setMax(100);
        brushSize.setValue(8);
        g = canvas.getGraphicsContext2D();
        g.setLineCap(StrokeLineCap.ROUND);  // Makes lines look smoother
        canvas.setCursor(new ImageCursor(penCursor, 0, penCursor.getHeight()));
        list.setWrapText(true);


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
        int pc = Server.playerCount;
        Server.refreshWords();
        Server.isGameRunning = true;

        for (int round = 1; round <= Server.rounds; round++) {
            revealedIndices.clear(); // Clear hints for the new round
            Server.correctGuessCount = 0;
            Platform.runLater(this::ClearCanvas);

            String word = Server.words.get((int) (Math.random() * Server.words.size()));
            System.out.println("GameHandler round: " + round + ", Word chosen: " + word);
            String wordLength = getWordLength(word);
            try {
                sendResOut("Round: " + round + "   word: " + wordLength);
            } catch (IOException e) { e.printStackTrace(); }

            Platform.runLater(() -> setWord(word));

            long roundStartTime = System.currentTimeMillis();
            Thread[] play = new Thread[pc];
            for (int pnum = 0; pnum < pc; pnum++) {

                int finalPnum = pnum;
                play[pnum] = new Thread(() -> gamePlay(finalPnum, word, play, roundStartTime));
                play[pnum].start();

            }

            try {
                // --- TIMER AND HINT LOGIC ---
                boolean hintGiven = false;
                for (int timeLeft = 60; timeLeft > 0; timeLeft--) {
                    if (Server.correctGuessCount >= pc) {
                        break;
                    }
                    if (timeLeft == 30 && !hintGiven) {
                        generateAndSendHint(word);
                        hintGiven = true;
                    }
                    int finalTime = timeLeft;
                    Platform.runLater(() -> displayTimer.setText("Timer: " + finalTime));
                    sleep(1000);
                }
                Platform.runLater(() -> displayTimer.setText("Timer: 0"));


                sendScores(round);
                if (round < Server.rounds) {
                    Thread waitTimer = setWaitTimer(10);
                    sendResOut("ROUND OVER");
                    for (int pnum = 0; pnum < pc; pnum++) {
                        if(play[pnum].isAlive()) play[pnum].interrupt();
                        play[pnum].join();
                    }
                    sendResOut("Round: " + round + "   word: " + word);
                    waitTimer.join();
                } else {
                    sendResOut("Winner:\n" + Server.getWinners());
                    sendResOut("GAME OVER");
                    sendResOut("Round: " + round + "   word: " + word);
                    sendDrawingAction(new DrawingAction(DrawingAction.ActionType.SHUTDOWN, 0, 0, 0, 0, 0, null));
                    try { sleep(100); } catch (InterruptedException ignored) {}
                    System.exit(0);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void gamePlay(int pnum, String word, Thread[] playerThreads, long roundStartTime) {
        String pname = Server.names.get(pnum);
        try {
            ObjectInputStream ois = Server.oisList.get(pnum);
            boolean answered = false;
            // The loop condition now checks if the thread has been interrupted
            while (!Thread.currentThread().isInterrupted()) {
                String guess = (String) ois.readObject();
                if (guess.equals("IM_DONE_GUESSING")) break;

                Platform.runLater(() -> list.appendText(pname + ": " + guess + "\n"));
                String lowerCaseGuess = guess.toLowerCase();

                if (word.equals(lowerCaseGuess)) {
                    int score = Server.scoreList.get(pnum);
                    if (!answered) {
                        long guessTime = System.currentTimeMillis();
                        long elapsedSeconds = (guessTime - roundStartTime) / 1000;
                        int scoreToAdd = Math.max(1, 60 - (int) elapsedSeconds);

                        Server.scoreList.set(pnum, score + scoreToAdd);
                        ScoreManager.saveScore(pname, scoreToAdd, word);
                        sendResOut("STYLE_GREEN:" + pname + " guessed the word! (+" + scoreToAdd + " points)");

                        answered = true;

                        Server.correctGuessCount++;

                    } else {
                        sendPrivateMessage(pnum, "You already answered correctly!");
                    }
                } else if (LevenshteinDistance.calculate(word, lowerCaseGuess) == 1) {
                    if (!answered) sendPrivateMessage(pnum, "STYLE_YELLOW:" + lowerCaseGuess + " is close!");
                    sendResOut(pname + ": " + guess);
                } else {
                    sendResOut(pname + ": " + guess);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // This is expected when the main game loop interrupts the thread at the end of a round.
            // We can safely ignore it or just print a confirmation message.
            System.out.println("Player thread for " + pname + " is finishing.");
        }
    }

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

    // Replace your existing generateAndSendHint method with this one.
    private void generateAndSendHint(String word) {
        // Find all possible indices that haven't been revealed yet
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            if (!revealedIndices.contains(i) && word.charAt(i) != ' ') {
                availableIndices.add(i);
            }
        }

        if (availableIndices.isEmpty()) {
            return;
        }

        int randomIndexToReveal = availableIndices.get((int) (Math.random() * availableIndices.size()));
        revealedIndices.add(randomIndexToReveal);

        // Build the hint string (e.g., "a _ p p _ _")
        StringBuilder hintBuilder = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (revealedIndices.contains(i)) {
                hintBuilder.append(word.charAt(i)).append(" ");
            } else if (word.charAt(i) == ' ') {
                hintBuilder.append("  "); // Handle spaces in words
            } else {
                hintBuilder.append("_ ");
            }
        }

        // --- THIS IS THE CHANGE ---
        // Send a special message that the client can use to update the label directly.
        try {
            sendResOut("HINT_UPDATE:" + hintBuilder.toString().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // --- END OF CHANGE ---
    }

}