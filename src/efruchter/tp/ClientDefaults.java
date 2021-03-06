package efruchter.tp;

import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import efruchter.tp.gui.Console;
import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.learning.server.ServerIO_ServerImpl;

public class ClientDefaults {

    private static long LEVEL_LENGTH;
    private static boolean LOCAL_SERVER;
    private static ServerIO VECTOR;
    private static boolean DEV_MODE;
    private static String SERVER_IP;
    private static int SERVER_PORT;

	public static void init(Applet applet) {
		// GATHER THE DATA FROM APPLET PARAMS OR CONFIG FILE
		String levelLength, localServer, vector, devMode, serverIp, console, port;
		if (applet.getParameter("canary") != null) {
			levelLength = applet.getParameter("level_length");
			localServer = applet.getParameter("local_server");
			vector = applet.getParameter("server_class");
			devMode = applet.getParameter("dev_mode");
			serverIp = applet.getParameter("server_ip");
			console = applet.getParameter("console");
			port = applet.getParameter("server_port");
		} else {
			Properties prop = new Properties();
			String fileName = "clientSettings.config";
			if (!new File(fileName).exists()) {
				// Assuming the client runs out of the /bin folder
				fileName = "../clientSettings.config";
			}
			InputStream is;
            try {
                is = new FileInputStream(fileName);
                prop.load(is);
            } catch (final Exception e) {
                e.printStackTrace();
            }
			levelLength = prop.getProperty("level_length");
			localServer = prop.getProperty("local_server");
			vector = prop.getProperty("server_class");
			devMode = prop.getProperty("dev_mode");
			serverIp = prop.getProperty("server_ip");
			console = prop.getProperty("console");
			port = prop.getProperty("server_port");
		}

		//Build the actual config options
		try {
		    LEVEL_LENGTH = Long.parseLong(levelLength);
		} catch (final Exception e) {
		    LEVEL_LENGTH = 60000;
		}
		try {
		    LOCAL_SERVER = Boolean.parseBoolean(localServer);
		    if (localServer == null) {
		        throw new Exception();
		    }
        } catch (final Exception e) {
            LOCAL_SERVER = true;
        }
		try {
		    Class<?> server = ServerIO.class.getClassLoader().loadClass(vector);
            VECTOR = (ServerIO) server.newInstance();
	    } catch (final Exception e) {
	        VECTOR = new ServerIO_ServerImpl();
        }
	    try {
	        DEV_MODE = Boolean.parseBoolean(devMode);
	    } catch (final Exception e) {
	        DEV_MODE = false;
	    }
	    try {
	        SERVER_IP = serverIp;
	        if (SERVER_IP == null) {
	            throw new Exception();
	        }
        } catch (final Exception e) {
            SERVER_IP = "trait.ericfruchter.com";
        }
	    try {
	        SERVER_PORT = Integer.parseInt(port);
        } catch (final Exception e) {
            SERVER_PORT = 8000;
        }
	    try {
	        if (Boolean.parseBoolean(console)) {
                Console.init();
            }
        } catch (final Exception e) {
            //Nothing to be done
        }
	}

	/**
	 * Wave Length in milliseconds
	 */
	public static long levelLength() {
		return LEVEL_LENGTH;
	}

	/**
	 * True to look for a server at "localhost"
	 */
	public static boolean localServer() {
		return LOCAL_SERVER;
	}

	/**
	 * The mechanism for IO with genevectors.
	 */
	public static ServerIO server() {
		return VECTOR;
	}

	/**
	 * True to allow the devmode stuff. Only changed to false prior to version
	 * ship.
	 */
	public static boolean devMode() {
		return DEV_MODE;
	}

	public static String serverIp() {
		return SERVER_IP;
	}

	public static int serverPort() {
		return SERVER_PORT;
	}
}
