package efruchter.tp.learning.server.comm;


public interface NetworkingListener {
    /**
     * Invoked when a message is received from a client.
     * 
     * @param message the message
     * @return a response to the client, or null if no response is necessary
     */
    String messageReceived(String message, String address);
}
