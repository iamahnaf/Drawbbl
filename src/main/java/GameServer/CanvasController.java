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

//import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    File newFile = new File("cursor.png");
    private final Image penCursor = new Image(newFile.toURI().toString());
    private CheckStatus checker = new CheckStatus();

    @FXML
    public void initialize() {
        new Thread(this::gameHandler).start();

        brushSize.setMax(50);
        brushSize.setValue(8);
        g = canvas.getGraphicsContext2D();
        canvas.setCursor(new ImageCursor(penCursor, 0, penCursor.getHeight()));
        list.setWrapText(true);

        // MODIFIED: Send drawing actions on mouse drag
        canvas.setOnMouseDragged(e -> {
            double size = brushSize.getValue();
            double x = e.getX();
            double y = e.getY();

            DrawingAction action;
            if (eraser.isSelected()) {
                g.clearRect(x, y, size, size);
                action = new DrawingAction(DrawingAction.ActionType.ERASE, e.getX(), e.getY(), size, null);
            } else {
                g.setFill(colorPicker.getValue());
                g.fillOval(x, y, size, size);
                Color color = colorPicker.getValue();
                DrawingAction.SerializableColor serializableColor = new DrawingAction.SerializableColor(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity());
                action = new DrawingAction(DrawingAction.ActionType.DRAW, e.getX(), e.getY(), size, serializableColor);
            }
            sendDrawingAction(action);
        });

        // REMOVED: No longer need to send image on mouse click.
        // canvas.setOnMouseClicked(...)

        clear.setOnAction(e -> ClearCanvas());
    }

    public void ClearCanvas() {
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // MODIFIED: Send a CLEAR action instead of an image.
        sendDrawingAction(new DrawingAction());
    }


    // --- MODIFICATION START ---
    //
    // The `synchronized` keyword ensures only one thread can execute this method at a time,
    // preventing the output stream from being corrupted by multiple simultaneous writes.
    // Also removed the unnecessary creation of a new thread for every action.
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



    // ... (rest of the class remains the same)
    public void setWord(String word){
        wordLabel.setText("Draw this : "+word);
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

    public void onSave() {
        try {
            String path = "/Desktop/paint2.png";
            Image snapshot = canvas.snapshot(null, null);
          //   ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(path));
        } catch (Exception e) {
            System.out.println("Failed to save image: " + e);
        }
    }

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
                for (int dur=duration;dur>=1;dur--) {
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

    public void gameHandler() {
        int pc=Server.playerCount;

        for(int round=1 ; round<=Server.rounds ; round++) {

            Platform.runLater(this::ClearCanvas);
            System.out.print("GameHandler round: " + round);
            String word = Server.words.get((int) (Math.random() * Server.words.size()));
            System.out.println(", Word chosen: " + word);
            String wordLength=getWordLength(word);
            try {
                sendResOut("Round: "+round+"   word: "+wordLength);
            } catch (IOException e) { e.printStackTrace(); }

            Platform.runLater(() -> setWord(word));
            Thread timer=setTimer(90),waitTimer;
            Thread[] play=new Thread[pc];
            for(int pnum=0;pnum<pc;pnum++) {
                int finalPnum = pnum;
                play[pnum]=new Thread(()->gamePlay(finalPnum,word,timer));
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
                    sendResOut("Winner:\n"+Server.getWinners());
                    sendResOut("GAME OVER");
                    sendResOut("Round: "+round+"   word: "+word);
                    System.exit(0);
                }
            } catch (InterruptedException | IOException e) { e.printStackTrace(); }

        }
    }

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
}