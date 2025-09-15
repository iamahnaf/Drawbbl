package GameServer;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.ByteBuffer;

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

    File newFile= new File("cursor.png");
    private final Image penCursor = new Image(newFile.toURI().toString());
    private CheckStatus checker = new CheckStatus();

    @FXML
    public void initialize() {
        new Thread(this::gameHandler).start();
        brushSize.setMax(100);
        brushSize.setValue(8);
        g = canvas.getGraphicsContext2D();
        canvas.setCursor(new ImageCursor(penCursor,0, penCursor.getHeight()));
        list.setWrapText(true);

        canvas.setOnMouseDragged(e -> {
            double size =Double.parseDouble(String.valueOf(brushSize.getValue()));
            double x = e.getX() - size/2;
            double y = e.getY() -size/2;
            if(eraser.isSelected()) {
                g.clearRect(x,y,size,size);
            }else {
                g.setFill(colorPicker.getValue());
                g.fillOval(x,y,size,size);
            }
        });

        canvas.setOnMouseClicked(e -> {
            Image snapshot = canvas.snapshot(null, null);
            sendImage(snapshot);
        });

        clear.setOnAction(e -> ClearCanvas());
    }

    public void ClearCanvas(){
        g.clearRect(0,1,canvas.getWidth(), canvas.getHeight());
        Image snapshot = canvas.snapshot(null, null);
        sendImage(snapshot);
    }
    public void setWord(String word){
        wordLabel.setText("DRAW THIS: "+word);
    }
    public void onSave(){
        try{
            String path = "C:\\Users\\Ahnaf\\Desktop";
            Image snapshot = canvas.snapshot(null, null);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(path));
        }catch (Exception e){
            //for saving image on pc
            System.out.println("Saved image on this pc");
        }
    }

    public void onMessage(){
        message.setOnAction(e -> {
            String str= message.getText();
            if(!str.isEmpty()){
                list.appendText("SERVER "+ str +"\n");
                try{
                    sendResOut("Server : "+str);
                }catch (Exception event){
                    event.printStackTrace();
                }
                message.clear();
            }
        });
    }

    public void onExit(){ System.exit(0); }

    public Thread setTimer(int duration){
      Thread t= new Thread(() -> {
          try{
              for(int dur=duration; dur>=1; dur--){
                  int finalDur = dur;
                  Platform.runLater(()-> displayTimer.setText("Timer: "+0));
                  sleep(1000);
              }
              Platform.runLater(()-> displayTimer.setText("Timer: "+0));
          }catch (Exception e){
              e.printStackTrace();
          }
      });
      return t;
    }
    public Thread setWaitTimer(int duration){
        Thread t= new Thread(() -> {
            try{
                for(int dur=duration; dur>=1; dur--){
                    int finalDur = dur;
                    Platform.runLater(()-> displayTimer.setText("Next Round starting in: "+0));
                    sleep(1000);
                }
                Platform.runLater(()-> displayTimer.setText("Next Round starting in: "+0));
            }catch (Exception e){
                System.out.println("Error in setWaitTimer");
                e.printStackTrace();
            }
        });
        t.start();
        return t;
    }
  public void sendImage(Image image){
        new Thread(()->{
            try{
                byte [] imsize;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", byteArrayOutputStream);
                imsize=ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();

                for(OutputStream os: ServerMain.canvasOut){
                    
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        })
  }

}
