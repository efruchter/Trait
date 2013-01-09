package efruchter.tp;

import java.io.IOException;

import com.csvreader.CsvReader;

import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.GeneVectorIO;
import efruchter.tp.learning.database.CSVDatabase;
import efruchter.tp.learning.database.Database;
import efruchter.tp.learning.database.Database.SessionInfo;
import efruchter.tp.networking.NetworkingListener;
import efruchter.tp.networking.Server;

/**
 *
 */
public class TraitProjectServer implements NetworkingListener {

	private static GeneVector current;
	private static final Database db;

	static {
		db = new CSVDatabase();
		current = new GeneVector();
	}

	public static void main(String[] args) {

		try {
			db.init();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			new TraitProjectServer();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public TraitProjectServer() throws IOException, InterruptedException {

		final int port = 8000;

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
			} else if ("request".equals(message)) {
				result = "EXPLORE" + GeneVectorIO.SEPARATOR + current.toDataString();
			}
            else if ("playerControlled".equals(message)) {
                try {
                    final CsvReader r = new CsvReader("playerControlled.csv");
                    if (r.readHeaders()) {
                        final String[] headers = r.getHeaders();
                        final StringBuffer b = new StringBuffer();
                        for(final String str : headers) {
                            b.append(GeneVectorIO.SEPARATOR).append(str);
                        }
                        result = b.toString().replaceFirst(GeneVectorIO.SEPARATOR, "");
                    } else {
                        result = " ";
                    }
                    r.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
			// username | score | date | vector
			else if (message.startsWith("store" + GeneVectorIO.SEPARATOR)) {
				String[] data = message.replaceFirst(
						"store" + GeneVectorIO.SEPARATOR, "").split(
						GeneVectorIO.SEPARATOR);
				result = "" + store(new SessionInfo(data[0], data[1], data[2]),
								new GeneVector(data[3]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public synchronized boolean store(SessionInfo userInfo, GeneVector vector) {
		return db.storeVector(userInfo, vector);
	}
}
