package efruchter.tp;

import java.io.File;
import java.io.IOException;

import com.csvreader.CsvReader;

import efruchter.tp.gui.Console;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.SessionInfo;
import efruchter.tp.learning.database.CSVDatabase;
import efruchter.tp.learning.database.Database;
import efruchter.tp.learning.server.comm.NetworkingListener;
import efruchter.tp.learning.server.comm.Server;

/**
 *
 */
public class TraitProjectServer implements NetworkingListener {

	private static GeneVector current;
	private static final Database db;
	private static final String databaseLocation = "database.csv";
	private static final String playerControlledPath = "playerControlled.csv";

	static {
		// Check for existing files
		fileCheck(databaseLocation);
		fileCheck(playerControlledPath);
		
		db = new CSVDatabase(databaseLocation);
		current = new GeneVector();
	}
	
	/**
	 * If file does not exist, create.
	 * @param filePath
	 */
	private static void fileCheck(String filePath) {
		if (!new File(filePath).exists()) {
			try {
				new File(filePath).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		boolean headless = false;
		int port = 8000;
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				final String arg = args[i];
				if (arg.equalsIgnoreCase("-nogui")) {
					headless = true;
				} else if(arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("-help")) {
					System.out.println("-nogui: Run in headless mode." );
					System.out.println("-server_port: specify a port." );
				} else if (arg.equalsIgnoreCase("-server_port")) {
					port = Integer.parseInt(args[i + 1]);
				}
			}
		}
		
		if (!headless) {
			Console.init();
			Console.setTitle("Trait Server Console");
		}
		
		System.out.println("Server Activated!");

		try {
			db.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			new TraitProjectServer(port);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public TraitProjectServer(final int port) throws IOException, InterruptedException {

		System.out.println("Trait Server Started on port " + port + ".");
		new Server(port, this);
	}

	@Override
	public String messageReceived(String message, final String clientName) {
		
		String result = " ";

		try {
			if (message.startsWith("versioncheck")) {
				result = "" + TraitProjectClient.VERSION.equals(message
								.replaceFirst("versioncheck", ""));
				System.out.println(System.currentTimeMillis() + ": VERSION CHECK");
			} else if ("request".equals(message)) {
				result = "EXPLORE" + SessionInfo.SEPERATOR + current.toDataString();
				System.out.println(System.currentTimeMillis() + ": EXPLORE");
			}
            else if ("playerControlled".equals(message)) {
                CsvReader r = null;
                try {
                    r = new CsvReader(playerControlledPath);
                    if (r.readHeaders()) {
                        final String[] headers = r.getHeaders();
                        final StringBuffer b = new StringBuffer();
                        for(final String str : headers) {
                            b.append(SessionInfo.SEPERATOR).append(str);
                        }
                        result = b.toString().replaceFirst(SessionInfo.SEPERATOR, "");
                        System.out.println(System.currentTimeMillis() + ": PLAYER CONTROLLED");
                    } else {
                        result = " ";
                    }
                    r.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                } finally {
                    if (r != null)
                        r.close();
                }
            }
			else if (message.startsWith("store" + SessionInfo.SEPERATOR)) {
				final String data = message.replaceFirst("store" + SessionInfo.SEPERATOR, "");
				result = "" + store(new SessionInfo(data));
				System.out.println(System.currentTimeMillis() + ": STORE");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public synchronized boolean store(SessionInfo userInfo) {
		return db.storeVector(userInfo);
	}
}
