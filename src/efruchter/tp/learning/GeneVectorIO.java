package efruchter.tp.learning;

import java.io.IOException;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.learning.database.Database.SessionInfo;
import efruchter.tp.networking.Client;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;

/**
 * Interface with the server.
 * 
 * @author toriscope
 * 
 */
public class GeneVectorIO {

    private static GeneVector exploration;
    public static final String SEPARATOR = "@";

    /**
     * @return the current exploration vector.
     */
    public static GeneVector getExplorationVector() {
        if (exploration == null)
            reloadExplorationVector();
        return exploration;
    }

    public static boolean storeVector(final SessionInfo info, final GeneVector vector) {
        ClientStateManager.setFlowState(FlowState.STORING_VECT);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();
                c.send("store" + SEPARATOR + info.username + SEPARATOR + info.score + SEPARATOR + info.date + SEPARATOR
                        + vector.toDataString());
                boolean suc = Boolean.parseBoolean(c.receive());
                if (suc)
                    System.out.println("Successfully stored gene vector in database.");
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
    public static void reloadExplorationVector() {
        ClientStateManager.setFlowState(FlowState.LOADING_VECT);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();
                c.send("request");
                final GeneVector geneVector = new GeneVector();
                geneVector.fromDataString(c.receive().replace("EXPLORE" + GeneVectorIO.SEPARATOR, ""));
                exploration = geneVector;
                System.out.println("Successfully read gene vector from server.");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                exploration = new GeneVector();
            } finally {
                try {
                    c.close();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.err.println("Cannot get Gene Vector from server, using defaults.");
        } finally {
            ClientStateManager.setFlowState(FlowState.FREE);
        }
    }
}
