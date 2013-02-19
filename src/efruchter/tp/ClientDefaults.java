package efruchter.tp;

import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.learning.server.ServerIO_ServerImpl;
import efruchter.tp.learning.server.ZookServer;

public class ClientDefaults {
	/**
	 * Wave Length in milliseconds
	 */
//	public static final long LEVEL_LENGTH = 60000;
	public static final long LEVEL_LENGTH = 12000;

	/**
	 * True to look for a server at "localhost"
	 */
	public final static boolean LOCAL_SERVER = true;

	/**
	 * The mechanism for getting a new GeneVector.
	 */
	public final static ServerIO VECTOR = new ZookServer();
//	public final static ServerIO VECTOR = new ServerIO_ServerImpl();

	/**
	 * True to allow the devmode stuff. Only changed to false prior to version
	 * ship.
	 */
	public final static boolean DEV_MODE = true;
}
