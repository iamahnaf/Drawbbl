package GameServer;

import java.io.IOException;
import java.net.Socket;

public class CheckStatus {
    public void checkPresence() throws IOException {
        int i=0;
        for(Socket s: ServerMain.socketList){
            if(s.getInputStream().read()== -1){
                playerExitHandler(i);
                break;
            }
        }
    }
    public static void playerExitHandler(int i){
        System.out.println(ServerMain.name.get(i)+" has left");
        ServerMain.name.remove(i);
        ServerMain.socketList.remove(i);
        ServerMain.oosList.remove(i);
        ServerMain.oisList.remove(i);
        ServerMain.canvasOut.remove(i);

        if(ServerMain.name.size()==0){
            System.out.println("All player have left...\n Closing Server");
          System.exit(0);
        }
    }
}
