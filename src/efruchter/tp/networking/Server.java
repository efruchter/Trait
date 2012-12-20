package efruchter.tp.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class Server implements Runnable {
    private final ServerSocket serverSocket;
    private final NetworkingListener listener;    
    private final Map<String, ServerThread> clients;
    private final Thread thread;
    
    /**
     * Creates a new Server and beings listening on the specified port.
     * 
     * @param portNumber the port number to listen on
     * @param listener an object that will be notified when a message is received
     * @throws IOException if the server fails to open a socket
     */
    public Server(int portNumber, NetworkingListener listener) throws IOException {
        serverSocket = new ServerSocket(portNumber);
        this.listener = listener;
        clients = new HashMap<String, ServerThread>();
        thread = new Thread(this);
        thread.start();
    }
    
    /**
     * Disconnects all clients and closes down the server.
     * 
     * @throws IOException if an error occurs when closing the server
     */
    public synchronized void close() throws IOException {
        for (String clientName : clients.keySet())
            disconnect(clientName);
        
        serverSocket.close();
    }

    synchronized void handle(String clientName, String message) {
        String response = listener.messageReceived(message);
        
        if (response != null && !response.isEmpty())
            clients.get(clientName).send(response);
    }

    synchronized void disconnect(String clientName) {
         ServerThread client = clients.remove(clientName);
         client.close();
    }
    
    @Override
    public void run() {
        while (true) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                break;
            }
            
            ServerThread newClient;
            try {
                newClient = new ServerThread(this, clientSocket);
                clients.put(newClient.getClientName(), newClient);
                newClient.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            new Server(8000, new NetworkingListener() {                
                @Override
                public String messageReceived(String message) {
                    System.out.println("Received: " + message);
                    return message + message;
                }
            });
            while (true) {
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
