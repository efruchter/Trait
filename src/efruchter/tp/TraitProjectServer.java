package efruchter.tp;

import java.io.IOException;

import efruchter.tp.learning.GeneVector;
import efruchter.tp.networking.NetworkingListener;
import efruchter.tp.networking.Server;

/**
 *
 */
public class TraitProjectServer implements NetworkingListener {

    public static void main(String[] args) {
        try {
            new TraitProjectServer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TraitProjectServer() throws IOException, InterruptedException {

        int port = 8000;

        System.out.println("Trait Server Started on port " + port + ".");
        new Server(port, this);
        while (true) {
            Thread.sleep(1000L);
        }
    }

    @Override
    public String messageReceived(String message) {

        if ("request".equals(message)) {
            System.out.println("Request handled.");
            return current.toDataString();
        }

        return null;

    }

    private static GeneVector current = new GeneVector();

    static {
        current.fromDataString("player.attack.dy#dy travel per step#-1.0#1.0#1.0#player.attack.wiggle#Maximum wiggle magnitude.#0.0#1.0#0.5#player.attack.cooldown#The projectile cooldown.#0.0#1000.0#64.0#player.attack.dx#dx travel per step#-1.0#1.0#0.0#player.move.drag#Amount of air drag.#0.0#1.0#0.5#player.radius.radius#The radius value#3.0#20.0#10.0#player.attack.damage#Amount of damage per bullet.#0.0#10.0#5.0#player.move.thrust#Control the acceleration of movement.#0.0#0.09#0.04#player.attack.amount#Amount of bullets per salvo.#0.0#100.0#1.0#player.attack.spread#Bullet spread.#0.0#1.0#0.0");
    }
}
