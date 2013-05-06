package efruchter.tp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import com.csvreader.CsvReader;

import efruchter.tp.gui.Console;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.RThread;
import efruchter.tp.learning.SessionInfo;
import efruchter.tp.learning.database.CSVDatabase;
import efruchter.tp.learning.database.Database;
import efruchter.tp.learning.server.ZookServer;
import efruchter.tp.learning.server.comm.NetworkingListener;
import efruchter.tp.learning.server.comm.Server;
import efruchter.tp.state.ClientStateManager;

public class ZookProjectServer implements NetworkingListener {

	// cf TraitProjectServer
	private static GeneVector current;
	private static final Database db;
	private static RThread learnThread = null;

	static {
		db = new CSVDatabase("database.csv");
		current = new GeneVector();
	}

	public static void main(String[] args) {
		boolean headless = false;
		int port = 8000;
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				final String arg = args[i];
				if (arg.equalsIgnoreCase("-nogui")) {
					headless = true;
				} else if (arg.equalsIgnoreCase("-h")
						|| arg.equalsIgnoreCase("-help")) {
					System.out.println("-nogui: Run in headless mode.");
					System.out.println("-server_port: specify a port.");
				} else if (arg.equalsIgnoreCase("-server_port")) {
					port = Integer.parseInt(args[i + 1]);
				}
			}
		}

		if (!headless) {
			Console.init();
			Console.setTitle("Zook Server Console");
		}

		System.out.println("Server Activated!");

		try {
			db.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			new ZookProjectServer(port);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ZookProjectServer(final int port) throws IOException,
			InterruptedException {
		System.out.println("Zook Server Started on port " + port + ".");
		new Server(port, this);
	}

	public String messageReceived(String message, final String clientName) {
		String result = " ";

		System.out.println("called messageReceived: " + message);

		try {
			if (message.startsWith("versioncheck")) {
				result = ""
						+ TraitProjectClient.VERSION.equals(message
								.replaceFirst("versioncheck", ""));
				// System.out.println("version check");
			} else if ("request".equals(message)) {
				result = "EXPLORE" + SessionInfo.SEPERATOR
						+ current.toDataString();
				// System.out.println("explore-vector sent");
			} else if ("geneText".equals(message)) {
				final String geneText = GeneFileBuilder
						.getVectorFromFile("geneText.txt"); // "../geneText.txt");
				result = geneText;
			} else if ("playerControlled".equals(message)) {
				CsvReader r = null;
				try {
					r = new CsvReader("playerControlled.csv");
					if (r.readHeaders()) {
						final String[] headers = r.getHeaders();
						final StringBuffer b = new StringBuffer();
						for (final String str : headers) {
							b.append(SessionInfo.SEPERATOR).append(str);
						}
						result = b.toString().replaceFirst(
								SessionInfo.SEPERATOR, "");
						// System.out.println("player-controlled sent");
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
			} else if (message.startsWith("store" + SessionInfo.SEPERATOR)) {
				System.out.println("calling store");
				final String data = message.replaceFirst("store"
						+ SessionInfo.SEPERATOR, "");
				result = "" + store(new SessionInfo(data));
				System.out.println("server is trying to store: "
						+ new SessionInfo(data));
				System.out.println("store");
			} else if (message.startsWith("runR" + SessionInfo.SEPERATOR)) {
				SessionInfo data = new SessionInfo(message.replaceFirst("runR"
						+ SessionInfo.SEPERATOR, ""));
				result = runR(Long.parseLong(data.get("playerID")),
						data.get("learningMode"),
						Long.parseLong(data.get("iteration")));
				System.out.println("runR");
			} else if ("getID".equals(message)) {
				result = "" + getID();
				System.out.println(System.currentTimeMillis() + ": Fetched id "
						+ result);
			} else if (message.startsWith("getRand:")) {
				String learnMode = message.replaceFirst("getRand:", "");
				result = "" + getRand(learnMode);
				System.out.println(System.currentTimeMillis() + ": Fetched random toggle "+ result);
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public synchronized boolean store(SessionInfo userInfo) {
		return db.storeVector(userInfo);
	}

	/**
	 * Calls R code for learning process
	 */
	// private synchronized void runR(long playerID, String learningMode, long
	// iteration) {
	// ClientStateManager.togglePauseState();
	// try {
	// Runtime rt = Runtime.getRuntime();
	// Process pr;
	// if (System.getProperty("os.name").startsWith("Windows")) {
	// pr = rt.exec("cmd /C \"Rscript r_script.R " + playerID + " " +
	// learningMode + " " + iteration + "\""); // change directory, then call
	// the r script
	// } else {
	// pr = rt.exec("Rscript r_script.R " + playerID + " " + learningMode + " "
	// + iteration); //call the r script
	// }
	// BufferedReader input = new BufferedReader(new
	// InputStreamReader(pr.getInputStream()));
	//
	// String line = null;
	// while((line = input.readLine()) != null) {
	// System.out.println(line);
	// }
	// int exitVal = pr.waitFor();
	// System.out.println("exited w/error code: " + exitVal);
	//
	// } catch (Exception e) {
	// System.out.println(e.toString());
	// e.printStackTrace();
	// } finally {
	// ClientStateManager.togglePauseState();
	// }
	// }
	private String runR(long playerID, String learnMode, long waveCount) {
		System.out.println("learnThread: " + learnThread);
		if (learnThread == null || learnThread.isDone()) {

			System.out.println("runR called w/pID: " + playerID);
			learnThread = new RThread(playerID, waveCount, learnMode);
			learnThread.execute();
			System.out.println("R thread on wave: " + learnThread.getWave());
			return "true";
		} else {
			return "false";
		}
	}

	public synchronized long getID() throws IOException {
		final File idFile = new File("playerID.txt");
		if (!idFile.exists()) {
			idFile.createNewFile();
			final FileWriter f = new FileWriter(idFile);
			f.write("0");
			f.close();
		}
		final Scanner scanner = new Scanner(idFile);
		final long id = Long.parseLong(scanner.next());
		final FileWriter f = new FileWriter(idFile);
		f.write((id + 1) + "");
		f.close();
		return id;
	}
	
	public synchronized boolean getRand(String learnMode) throws IOException {
		final File randFile = new File(learnMode + "Rand.txt");
		
		// if no file for this learning mode, create one and set to 0 AL, 0 RND
		if (!randFile.exists()) {
			randFile.createNewFile();
			final FileWriter f = new FileWriter(randFile);
			f.write("0\n0");
			f.close();
		}
		final Scanner scan = new Scanner(randFile);
		// read in number of players with active learning or random
		// AL is first line, RND is second line
		final long al = Long.parseLong(scan.next());
		final long rnd = Long.parseLong(scan.next());
		System.out.println("al: " + al + ", rnd: " + rnd);
		
		final FileWriter f = new FileWriter(randFile);
		if (al < rnd) {
			// fewer active learning samples than random samples
			// increment active learning iterations and return isRandom=false
			f.write((al+1) + "\n" + rnd);
			f.close();
			return false;
		} else {
			// more active learning samples than random samples
			// increment random iterations and return isRandom=true
			f.write(al + "\n" + (rnd+1));
			f.close();
			return true;
		}
	}
}
