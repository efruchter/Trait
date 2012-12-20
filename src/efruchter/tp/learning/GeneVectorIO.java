package efruchter.tp.learning;

import efruchter.tp.networking.Client;

import java.io.IOException;

/**
 * Interface with the server.
 *
 * @author toriscope
 *
 */
public class GeneVectorIO {

    private static GeneVector exploration;

    /**
     * @return the current exploration vector.
     */
    public static GeneVector getExplorationVector() {
        if (exploration == null) {
            reloadExplorationVector();
        }

        return exploration;
    }

    /**
     * Request a new vector from the frontier.
     */
    public static void reloadExplorationVector() {
        Client c = new Client();
        try {
            c.reconnect();
            c.send("request");
            GeneVector geneVector = new GeneVector();
            geneVector.fromDataString(c.receive());
            exploration = geneVector;
            return;
        } catch (IOException e) {
            //e.printStackTrace();
            exploration = new GeneVector();
        } finally {
            try {
                c.close();
                return;
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        System.err.println("Cannot get Gene Vector from server, using defaults.");
    }
}
