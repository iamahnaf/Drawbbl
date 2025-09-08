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
import java.util.concurrent.TimeUnit;

public class ServerMain extends Application {

    public static ServerSocket listener, listener2;
    static ArrayList<StartHandler> clients = new ArrayList<>();
    static ArrayList<Socket> socketList = new ArrayList<>();
    public static ArrayList<ObjectOutputStream> oosList = new ArrayList<>();
    public static ArrayList<ObjectInputStream> oisList = new ArrayList<>();
    public static ArrayList<OutputStream> canvasOut = new ArrayList<>();

    public static int playerCount = 2;
    public static int rounds=3;

    public static ArrayList<String> name = new ArrayList<>();
    public static ArrayList<Integer> scoreList = new ArrayList<>();
    public static final ExecutorService pool = Executors.newFixedThreadPool(playerCount);

    public static ArrayList<String> words = new ArrayList<>();


    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(ClassLoader.getSystemClassLoader().getResource("canva.fxml"));
        stage.setTitle("Game  Server");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        getwords();
        launch(args);
    }
    private static void getwords() {
        try{
            File wordsFile=new File("words.txt");
            Scanner sc = new Scanner(wordsFile);
            sc.useDelimiter(", ");
            while(sc.hasNext()){
                words.add(sc.next().toLowerCase(Locale.ROOT));
            }

        }catch (Exception e){
            System.out.println("words.txt not found");
            e.printStackTrace();
        }
    }
    private static void getStartHandler() throws IOException, InterruptedException {
        System.out.println("Player count: "+playerCount);
        listener = new ServerSocket(6666);
        listener2 = new ServerSocket(6677);
        System.out.println("waiting for clients..........");
        Socket client;

        for(int i=1;i<=playerCount;i++){
            client = listener.accept();
            ObjectOutputStream dOut = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream dIn = new ObjectInputStream(client.getInputStream());
            System.out.println("Main: Connected");
            StartHandler sThread = new StartHandler(clients,i,dOut,dIn);
            oosList.add(dOut);
            oisList.add(dIn);
            clients.add(sThread);
            pool.execute(sThread);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Start Handler closed...");
    }
   static String getWinners(){
        int i=0,ind=0,max=0;
        for(int score : scoreList){
            if(score>max){
                ind=i; max=score;
            }
            i++;
        }
        if(max==0) return "No winner";
        String wins="";
        for(i=0;i<name.size();i++){
            if(scoreList.get(i)==max){
                wins+=name.get(i)+"\n";
            }
        }
        return wins;
   }
}
