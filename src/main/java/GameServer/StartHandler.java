package GameServer;

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
            // This socket is now for drawing actions
            Socket drawingSocket = Server.listener1.accept();
            Server.socketList.add(drawingSocket);
            // MODIFIED: Create and store an ObjectOutputStream for this socket.
            Server.drawingOut.add(new ObjectOutputStream(drawingSocket.getOutputStream()));

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