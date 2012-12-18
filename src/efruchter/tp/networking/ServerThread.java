package efruchter.tp.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ServerThread extends Thread {
    private final Server server;
    private final Socket socket;
    private final String clientName;
    private final PrintWriter out;
    private final BufferedReader in;

    ServerThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        clientName = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    void send(String message) {
        out.println(message);
    }
    
    void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                server.handle(clientName, in.readLine());
            } catch (IOException e) {
                server.disconnect(clientName);
                break;
            }
        }
    }
    
    String getClientName() {
        return clientName;
    }
}
