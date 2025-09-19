import java.io.IOException;
import java.net.Socket;

class CheckStatus{
    public void checkPresence() throws IOException {
        int i=0;
        for(Socket s:Server.socketList){
            if(s.getInputStream().read()==-1){
                playerExitHandler(i);
                break;
            }
            i++;
        }
    }
    public static void playerExitHandler(int ind){
        System.out.println(Server.names.get(ind)+" has left..");
        Server.names.remove(ind);
        Server.scoreList.remove(ind);
        Server.oosList.remove(ind);
        Server.oisList.remove(ind);
        Server.canvasOut.remove(ind);

        if(Server.names.size()==0){
            System.out.println("All players have Left...\nClosing SERVER..");
            System.exit(0);
        }
    }
}
