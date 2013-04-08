package efruchter.tp.learning.server;

import java.io.IOException;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.SessionInfo;
import efruchter.tp.learning.server.comm.Client;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;

/**
 * Interface with the server via sockets.
 * 
 * @author toriscope
 * 
 */
public class ServerIO_ServerImpl implements ServerIO {

    private static GeneVector exploration;

    /**
     * @return the current exploration vector.
     */
    public GeneVector getExplorationVector() {
        if (exploration == null)
            reloadExplorationVector("");
        return exploration;
    }

    public boolean storeInfo(final SessionInfo info) {
        ClientStateManager.setFlowState(FlowState.STORING_VECT);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();
                c.send("store" + SessionInfo.SEPERATOR + info.toDataString());
                boolean suc = Boolean.parseBoolean(c.receive());
                if (suc)
                    System.out.println("Successfully stored gene vector in database.");
                else
                	System.err.println("Cannot store vector on server.");
                return suc;
            } catch (IOException e) {

            } finally {
                try {
                    c.close();
                } catch (Exception e) {
                }
            }
            System.err.println("Cannot store vector on server.");
            return false;
        } finally {
            ClientStateManager.setFlowState(FlowState.FREE);
        }
    }

    /**
     * Request a new vector from the frontier.
     */
    public void reloadExplorationVector(String fname) {
    	// NOTE: fname is ignored in default implementation
        ClientStateManager.setFlowState(FlowState.LOADING_VECT);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();
                c.send("request");
                final GeneVector geneVector = new GeneVector();
                geneVector.fromDataString(c.receive().replace("EXPLORE" + SessionInfo.SEPERATOR, ""));
                exploration = geneVector;
                System.out.println("Successfully read gene vector from server.");
                return;
            } catch (IOException e) {
                exploration = new GeneVector();
            } finally {
                try {
                    c.close();
                    return;
                } catch (Exception e) {
                }
            }
            System.err.println("Cannot get Gene Vector from server, using defaults.");
        } finally {
            ClientStateManager.setFlowState(FlowState.FREE);
        }
    }

	@Override
	public void runR(long playerID, String string, int i) {

	}
}
