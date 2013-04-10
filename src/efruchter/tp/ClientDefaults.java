package efruchter.tp;

import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import efruchter.tp.gui.Console;
import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.learning.server.ServerIO_ServerImpl;
import efruchter.tp.learning.server.ZookServer;

public class ClientDefaults {

	public static void init(Applet applet) {
		try {
			// GATHER THE DATA FROM APPLET PARAMS OR CONFIG FILE
			String levelLength, localServer, vector, devMode, serverIp, console, port, learn_mode;

			if (applet.getParameter("canary") != null) {
				System.out.println("running from applet");
				levelLength = applet.getParameter("level_length");
				localServer = applet.getParameter("local_server");
				vector = applet.getParameter("server_class");
				devMode = applet.getParameter("dev_mode");
				serverIp = applet.getParameter("server_ip");
				console = applet.getParameter("console");
				learn_mode = applet.getParameter("learn_mode");
				port = applet.getParameter("server_port");
			} else {
				System.out.println("running from console");
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
				learn_mode = prop.getProperty("learn_mode");
				port = prop.getProperty("server_port");
			}

			//Build the actual config options
			LEVEL_LENGTH = Long.parseLong(levelLength);
			LOCAL_SERVER = Boolean.parseBoolean(localServer);
			Class server = ServerIO.class.getClassLoader().loadClass(vector);
			VECTOR = (ServerIO) server.newInstance();
			DEV_MODE = Boolean.parseBoolean(devMode);
			SERVER_IP = serverIp;
			LEARN_MODE = learn_mode;
			SERVER_PORT = Integer.parseInt(port);

			if (Boolean.parseBoolean(console)) {
				Console.init();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Config file clientSettings.config not parsed/found. Assuming web client defaults!");

			// Defaults
			LEVEL_LENGTH = 60000;
			LOCAL_SERVER = true;
			VECTOR = new ServerIO_ServerImpl();
			DEV_MODE = false;
			SERVER_IP = "trait.ericfruchter.com";
			SERVER_PORT = 8000;
			LEARN_MODE = "regression";
		}
	}

	private static long LEVEL_LENGTH;
	private static boolean LOCAL_SERVER;
	private static ServerIO VECTOR;
	private static boolean DEV_MODE;
	private static String SERVER_IP;
	private static int SERVER_PORT;
	private static String LEARN_MODE;


	/**
	 * Mode for learning algorithm
	 * 	currently supports: regression, preference
	 */
	public static String learnMode() {
		return LEARN_MODE;
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
