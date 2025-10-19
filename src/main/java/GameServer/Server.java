package GameServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;


public class Server {
    public static ServerSocket listener,listener1;
    static ArrayList<StartHandler> clients=new ArrayList<>();
    static ArrayList<Socket> socketList=new ArrayList<>();
    public static ArrayList<ObjectOutputStream> oosList=new ArrayList<>();
    public static ArrayList<ObjectInputStream> oisList=new ArrayList<>();
    // ObjectOutputStreams for sending DrawingAction objects.
    public static ArrayList<ObjectOutputStream> drawingOut=new ArrayList<>();

    public static int playerCount=2;
    public static int rounds=3;
    public static volatile int correctGuessCount = 0;

    public static ArrayList<String> names=new ArrayList<>();
    public static ArrayList<Integer> scoreList=new ArrayList<>();
    private static final ExecutorService pool=Executors.newFixedThreadPool(playerCount);

    public static ArrayList<String> words=new ArrayList<>();

    Stage stage;
    /*
    @Override
    public void start(Stage primaryStage) throws Exception{
        stage=primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("canvas.fxml"));
        stage.setTitle("Pictionary SERVER");
        Scene scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    */

   /*
    public static void main(String[] args) throws IOException, InterruptedException {
        getWords();
        getStartHandler();
        System.out.println("GameHandler Started..");
        launch(args);
    }
   */
    public static void getWords() {
        try{
            File wordsFile=new File("csword.txt");
            System.out.println("file found: "+wordsFile.exists());
            Scanner sc=new Scanner(wordsFile);
            sc.useDelimiter(", ");
            while(sc.hasNext()) words.add(sc.next().toLowerCase(Locale.ROOT));
        }catch (FileNotFoundException e){
            System.out.println("File not found");
        }
    }

    public static void getStartHandler() throws InterruptedException, IOException {
        System.out.println("Player count set to: "+playerCount);
        listener=new ServerSocket(6666);
        listener1=new ServerSocket(6677); // This socket is now for drawing actions
        System.out.println("Waiting for clients...");
        Socket client;
        for(int i=1;i<=playerCount;i++){
            client=listener.accept();
            ObjectOutputStream dOut=new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream dIn=new ObjectInputStream(client.getInputStream());
            System.out.println("MAIN: Connected: "+i);
            StartHandler sThread=new StartHandler(clients,i,dOut,dIn);
            oosList.add(dOut);
            oisList.add(dIn);
            clients.add(sThread);
            pool.execute(sThread);
        }
        pool.shutdown();
        // Replace pool.awaitTermination(10, TimeUnit.SECONDS); with a manual wait loop.
        long timeout = 5000; // 10 seconds in milliseconds
        long end_time = System.currentTimeMillis() + timeout;

        while(!pool.isTerminated()) {
            if (System.currentTimeMillis() >= end_time) {
                System.err.println("Timeout elapsed while waiting for pool to terminate. Forcing shutdown.");
                pool.shutdownNow(); // Force shutdown of running tasks
                break;
            }
            try {
                // Wait for a short interval before checking again to avoid busy-waiting
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for pool to terminate.");
                pool.shutdownNow();
                Thread.currentThread().interrupt(); // Preserve the interrupted status
                break;
            }
        }
        System.out.println("StartHandler Closed..");
    }

    static String getWinners(){
        int i=0,ind=0,max=0;
        for(int score:scoreList){
            if(score>max){ind=i; max=score;}
            i++;
        }
        if(max==0) return "No winners";
        String wins="";
        for(i=0;i<names.size();i++){
            if(scoreList.get(i)==max){
                wins+=names.get(i)+"\n";
            }
        }
        return wins;
    }
}