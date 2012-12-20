package efruchter.tp;

import java.io.IOException;

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
	final private static Database db;
	
	static {
        db = new CSVDatabase();
        current = new GeneVector();
		current.fromDataString("player.attack.dy#dy travel per step#-1.0#1.0#1.0#player.attack.wiggle#Maximum wiggle magnitude.#0.0#1.0#0.5#player.attack.cooldown#The projectile cooldown.#0.0#1000.0#64.0#player.attack.dx#dx travel per step#-1.0#1.0#0.0#player.move.drag#Amount of air drag.#0.0#1.0#0.5#player.radius.radius#The radius value#3.0#20.0#10.0#player.attack.damage#Amount of damage per bullet.#0.0#10.0#5.0#player.move.thrust#Control the acceleration of movement.#0.0#0.09#0.04#player.attack.amount#Amount of bullets per salvo.#0.0#100.0#1.0#player.attack.spread#Bullet spread.#0.0#1.0#0.0");
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
	public String messageReceived(String message) {
		
		System.out.println("Request received.");
		
		if ("request".equals(message)) {
			return current.toDataString();
		}
		
		//username | score | date | vector
		if (message.startsWith("store" + GeneVectorIO.SEPARATOR)) {
			String[] data = message.replaceFirst("store" + GeneVectorIO.SEPARATOR, "").split(GeneVectorIO.SEPARATOR);
			return "" + store(new SessionInfo(data[0], data[1], data[2]), new GeneVector(data[3]));
		}
		
		return null;
		
	}
	
	public synchronized boolean store(SessionInfo userInfo, GeneVector vector) {
		return db.storeVector(userInfo, vector);
	}
}
