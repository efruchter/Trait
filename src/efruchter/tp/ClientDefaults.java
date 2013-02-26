package efruchter.tp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.learning.server.ServerIO_ServerImpl;

public class ClientDefaults {

	static {
		try {
		Properties prop = new Properties();
		//Assuming the client runs out of the /bin folder
	    String fileName = "../clientSettings.config";
	    InputStream is = new FileInputStream(fileName);
	    prop.load(is);
	    
	    LEVEL_LENGTH = Long.parseLong(prop.getProperty("level_length"));
	    LOCAL_SERVER = Boolean.parseBoolean(prop.getProperty("local_server"));
	    
	    String vectorClass = prop.getProperty("server_class");
	    Class server = ServerIO.class.getClassLoader().loadClass(vectorClass);
	    VECTOR = (ServerIO) server.newInstance();
	    
	    DEV_MODE = Boolean.parseBoolean(prop.getProperty("dev_mode"));
	    
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Config file not parsed/found. Assuming web client defaults!");
			
			//Defaults
			LEVEL_LENGTH = 60000;
			LOCAL_SERVER = false;
			VECTOR = new ServerIO_ServerImpl();
			DEV_MODE = false;
		}
	}
	/**
	 * Wave Length in milliseconds
	 */
	private static long LEVEL_LENGTH;

	/**
	 * True to look for a server at "localhost"
	 */
	private static boolean LOCAL_SERVER;

	/**
	 * The mechanism for getting a new GeneVector.
	 */
	private static ServerIO VECTOR;

	/**
	 * True to allow the devmode stuff. Only changed to false prior to version
	 * ship.
	 */
	private static boolean DEV_MODE;
	
	public static long levelLength() {
		return LEVEL_LENGTH;
	}

	public static boolean localServer() {
		return LOCAL_SERVER;
	}

	public static ServerIO server() {
		return VECTOR;
	}

	public static boolean devMode() {
		return DEV_MODE;
	}
}
