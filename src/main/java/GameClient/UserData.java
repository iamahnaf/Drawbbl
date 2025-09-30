package GameClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UserData {
    public String username, serverIP;
    public Socket server, drawingSocket;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    ObjectInputStream drawingIn;

    // MODIFIED: The constructor now handles the complete connection and stream setup sequence.
    public UserData(String name, String ip) throws IOException {
        this.username = name;
        this.serverIP = ip;

        // --- Step 1: Connect to the main communication port (6666) ---
        server = new Socket(ip, 6666);

        // --- Step 2: Connect to the drawing communication port (6677) ---
        drawingSocket = new Socket(ip, 6677);

        // --- Step 3: Initialize output streams first ---
        // This sends the stream headers to the server, unblocking its input streams.
        this.oos = new ObjectOutputStream(server.getOutputStream());
        // Send the username immediately after creating the stream
        this.oos.writeObject(username);
        this.oos.flush();

        // Note: The drawing stream is output-only on the server, so we don't create an output stream for it here.

        // --- Step 4: Initialize input streams ---
        // These will now unblock successfully because the server has created its output streams.
        this.ois = new ObjectInputStream(server.getInputStream());
        this.drawingIn = new ObjectInputStream(drawingSocket.getInputStream());
    }

    // DELETED: The setSocket method is no longer needed as its logic is moved into the constructor.
    // public void setSocket(Socket s) throws IOException { ... }
}