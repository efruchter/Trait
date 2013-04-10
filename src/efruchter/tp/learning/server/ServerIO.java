package efruchter.tp.learning.server;

import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.SessionInfo;

/**
 * Here the system can request a new exploration vector to run, or store data.
 * 
 * @author toriscope
 * 
 */
public interface ServerIO {

	/**
	 * Get the current vector. Reload is necessary.
	 * 
	 * @return current vector
	 */
	public GeneVector getExplorationVector();

	/**
	 * Store a tuple in the database.
	 * 
	 * @param info
	 *            a huge tuple of info.
	 * @return true if successful
	 */
	public boolean storeInfo(final SessionInfo info);

	/**
	 * Reload it from server.
	 */
	public void reloadExplorationVector(String fname);

	public void runR(final long playerID, final String learningMode, final long isDebug);
}
