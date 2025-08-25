package GameServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class StartHandler extends Thread {
    private ObjectInputStream dIn;
    private ObjectOutputStream dOut;
    private ArrayList<StartHandler> clientsList;
    private int slno;

    public StartHandler(ArrayList<StartHandler> sList, int num,
                        ObjectOutputStream oos,ObjectInputStream ois) {
       clientsList = sList;
       slno = num;
       dOut = oos;
       dIn = ois;
    }
    @Override
    public void run() {
      String name;
      try{
           Socket s1 = ServerMain.listener.accept();
           ServerMain.socketList.add(s1);
           ServerMain.canvasOut.add(s1.getOutputStream());
           name = (String) dIn.readObject();
          System.out.println("StartHandler: "+name+" has joined");
          ServerMain.name.add(name);
          ServerMain.scoreList.add(0);

          if(slno == ServerMain.playerCount) outToALl();


      }catch (Exception e){
          System.out.println("Error in StartHandler void run func");
          e.printStackTrace();
      }
    }

    private void outToALl() throws IOException {
        for(StartHandler s : clientsList) {
            s.dOut.writeInt(ServerMain.name.size());
            dOut.flush();
            for(String name: ServerMain.name) {
                s.dOut.writeObject(name);
                dOut.flush();
            }
        }
    }
}
