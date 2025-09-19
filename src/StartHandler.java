

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class StartHandler extends Thread{
    private ObjectInputStream dIn;
    private ObjectOutputStream dOut;
    private ArrayList<StartHandler> clientsList;
    private int slno;

    public StartHandler(ArrayList<StartHandler> sList,int num,
                        ObjectOutputStream oos,ObjectInputStream ois) {
        clientsList=sList;
        slno=num;
        dOut=oos;
        dIn=ois;
    }
    @Override
    public void run() {

        String name;
        try {
            Socket s1=Server.listener1.accept();
            Server.socketList.add(s1);
            Server.canvasOut.add(s1.getOutputStream());
            name= (String) dIn.readObject();
            System.out.println("StartHandler: "+name+" has joined");
            Server.names.add(name);
            Server.scoreList.add(0);

            if(slno==Server.playerCount) outToAll();

            dIn.readBoolean();
        } catch (IOException | ClassNotFoundException e) {System.out.println(e);}
    }

    public void outToAll() throws IOException {
        for(StartHandler acl:clientsList){
            acl.dOut.writeInt(Server.names.size());
            dOut.flush();
            for(String name:Server.names) {
                acl.dOut.writeObject(name);
                dOut.flush();
            }
        }
    }
}

// to check if connection is lost
//            while(true){
//                if(client.getInputStream().read()==-1){
//                    System.out.println(name+" has left");
//                    client.close();
//                    break;
//                }
//                sleep(1000);
//            }
