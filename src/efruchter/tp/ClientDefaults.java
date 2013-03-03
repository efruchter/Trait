package efruchter.tp;

import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import efruchter.tp.gui.Console;
import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.learning.server.ServerIO_ServerImpl;
import efruchter.tp.learning.server.ZookServer;

public class ClientDefaults {

	public static void init(Applet applet) {
		try {
			// GATHER THE DATA FROM APPLET PARAMS OR CONFIG FILE
			String levelLength, localServer, vector, devMode, serverIp, console;
			if (applet.getParameter("canary") != null) {
				levelLength = applet.getParameter("level_length");
				localServer = applet.getParameter("local_server");
				vector = applet.getParameter("server_class");
				devMode = applet.getParameter("dev_mode");
				serverIp = applet.getParameter("server_ip");
				console = applet.getParameter("console");
			} else {
				Properties prop = new Properties();
				String fileName = "clientSettings.config";
				if (!new File(fileName).exists()) {
					// Assuming the client runs out of the /bin folder
					fileName = "../clientSettings.config";
				}
				InputStream is = new FileInputStream(fileName);
				prop.load(is);
				levelLength = prop.getProperty("level_length");
				localServer = prop.getProperty("local_server");
				vector = prop.getProperty("server_class");
				devMode = prop.getProperty("dev_mode");
				serverIp = prop.getProperty("server_ip");
				console = prop.getProperty("console");
			}

			//Build the actual config options
			LEVEL_LENGTH = Long.parseLong(levelLength);
			LOCAL_SERVER = Boolean.parseBoolean(localServer);
			Class server = ServerIO.class.getClassLoader().loadClass(vector);
			VECTOR = (ServerIO) server.newInstance();
			DEV_MODE = Boolean.parseBoolean(devMode);
			SERVER_IP = serverIp;
			if (Boolean.parseBoolean(console)) {
				Console.init();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Config file clientSettings.config not parsed/found. Assuming web client defaults!");

			// Defaults
			LEVEL_LENGTH = 60000;
			LOCAL_SERVER = false;
			VECTOR = new ServerIO_ServerImpl();
			DEV_MODE = false;
			SERVER_IP = "trait.ericfruchter.com";
		}
	}

	private static long LEVEL_LENGTH;
	private static boolean LOCAL_SERVER;
	private static ServerIO VECTOR;
	private static boolean DEV_MODE;
	private static String SERVER_IP;

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
}
