package efruchter.tp.learning.server;

import java.io.IOException;

import efruchter.tp.ClientDefaults;
import efruchter.tp.GeneFileBuilder;
import efruchter.tp.TraitProjectClient;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.SessionInfo;
import efruchter.tp.learning.server.comm.Client;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.trait.gene.GeneCurve;

public class ZookServer implements ServerIO {

	private static GeneVector exploration;
	
	@Override
	public GeneVector getExplorationVector() {
		
		// returns loaded vector

		if (exploration == null)
			reloadExplorationVector("../geneText.txt");

		return exploration;
	}

	@Override
	public boolean storeInfo(SessionInfo info) {
		ClientStateManager.setFlowState(FlowState.STORING_VECT);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();
                c.send("store" + SessionInfo.SEPERATOR + info.toDataString());
//                String info2 = info.toDataString().replace("#", "THAWKISBEST");
//                c.send("store" + SessionInfo.SEPERATOR + info2);
                boolean suc = Boolean.parseBoolean(c.receive());
                if (suc) {
//                	System.out.println("storing: " + info.toDataString());
                    System.out.println("Successfully stored gene vector in database.");
                }
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

	@Override
	public void reloadExplorationVector(String fname) {
		ClientStateManager.setFlowState(FlowState.LOADING_VECT);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();
                c.send("geneText");
                final GeneVector geneVector = new GeneVector();
                geneVector.fromDataString(c.receive());
//                geneVector.storeGene("player.move.drag", new Gene("Air Drag", "Amount of air drag."), false);
//                geneVector.storeGene("player.radius.radius", new Gene("Radius", "Player ship radius", 2, 50, 30), false);
//                geneVector.storeGeneCurve("spawner.enemy.radius", new GeneCurve("baseRadius", "Base enemy radius.", 2, 50, 15), false);
                
                //geneVector.fromDataString(c.receive().replace("EXPLORE" + SessionInfo.SEPERATOR, ""));
                exploration = geneVector;
//                System.out.println("exploration vector:\n" + exploration.toDataString());
//                System.out.println("ship thrust: " + exploration.getGene("player.move.thrust").getValue());
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
	public void runR(final long playerID, final String learningMode, final long iteration) {
		ClientStateManager.setFlowState(FlowState.STORING_VECT);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();

                SessionInfo data = new SessionInfo();
                data.put("playerID", "" + playerID);
                data.put("learningMode", learningMode);
                data.put("iteration", Long.toString(iteration));

                System.out.println("sending to R: " + data.toDataString());
                c.send("runR" + SessionInfo.SEPERATOR + data.toDataString());
//                c.receive();
            } catch (IOException e) {
            	System.err.println("Cannot run r code on server!");
            } finally {
                try {
                    c.close();
                } catch (Exception e) {
                }
            }
            
        } finally {
            ClientStateManager.setFlowState(FlowState.FREE);
        }
	}
	

	
}
