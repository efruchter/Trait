package efruchter.tp;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;


import efruchter.tp.entity.Entity;
import efruchter.tp.entity.EntityFactory;
import efruchter.tp.entity.EntityType;
import efruchter.tp.entity.Level;
import efruchter.tp.gui_broken.VectorEditorPopup_Crummy;
import efruchter.tp.learning.GeneVector.GeneWrapper;
import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.learning.server.comm.Client;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.SessionInfo;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.util.KeyUtil;
import efruchter.tp.util.RenderUtil;

@SuppressWarnings("serial")
public class TraitProjectClient extends Applet {

	private Canvas display_parent;

	/** Thread which runs the main game loop */
	private Thread gameThread;

	/** is the game loop running */
	private boolean running = false;

	/*
	 * GAME VARS
	 */
	public static final String VERSION = "00.00.00.04";
	private static Level level;
	private static long lastFrame;
	private static int fps;
	private static long lastFPS;

	/*
	 * Client Statistics
	 */

	public static float s_damage_player;
	public static float s_damage_enemies;
	public static float s_num_enemies;
	public static float s_hit_player;
	public static float s_hit_enemies;
	public static float s_remain_enemies;
	public static float s_fired_player;
	public static float s_fired_enemies;
	public static float s_killed_enemies;
	public static CHOICE c_choice = CHOICE.NONE;

	public static void resetMetrics() {
		System.out.println("player hits: " + s_hit_player);
		
		s_damage_player = 0;
		s_damage_enemies = 0;
		s_num_enemies = 0;
		s_hit_player = 0;
		s_hit_enemies = 0;
		s_remain_enemies = 0;
		s_fired_player = 0;
		s_fired_enemies = 0;
		s_killed_enemies = 0;
		c_choice = CHOICE.NONE;
	}
	
	public static void storeData(ServerIO v, long waveCount) {
		String username = System.getProperty("user.name");
        if (username == null) {
            username = "NO_NAME";
        }
        SessionInfo info = new SessionInfo();
        info.put("username", username);
        info.put("date", Long.toString(System.currentTimeMillis()));
        info.put("vector", v.getExplorationVector().toDataString());
//        System.out.println("saving exploration vector: " + v.getExplorationVector().toDataString());
        info.put("pID", ""+ClientDefaults.playerID());
        info.put("s_wave", Long.toString(waveCount));
        info.put("s_damage_player", Float.toString(TraitProjectClient.s_damage_player));
        info.put("s_damage_enemies", Float.toString(TraitProjectClient.s_damage_enemies));
        info.put("s_hit_player", Float.toString(TraitProjectClient.s_hit_player));
        info.put("s_hit_enemies", Float.toString(TraitProjectClient.s_hit_enemies));
        info.put("s_num_enemies", Float.toString(TraitProjectClient.s_num_enemies));
        info.put("s_fired_player", Float.toString(TraitProjectClient.s_fired_player));
        info.put("s_fired_enemies", Float.toString(TraitProjectClient.s_fired_enemies));
        info.put("s_killed_enemies", Float.toString(TraitProjectClient.s_killed_enemies));
        info.put("s_remain_enemies", Float.toString(TraitProjectClient.s_remain_enemies));
        info.put("c_choice", (TraitProjectClient.c_choice).toString());
        System.out.println("info: " + info.toDataString());
//        String info2 = info.toDataString().replace("#", "THAWKISBEST");
//        System.out.println("info2: " + info2);
        v.storeInfo(info);
	}

	private static String[] playerControlled = new String[0];

	public void startLWJGL() {
		gameThread = new Thread() {
			public void run() {
				running = true;
				try {
					Display.setParent(display_parent);
					Display.create();
					initGL();
				} catch (LWJGLException e) {
					e.printStackTrace();
					return;
				}
				gameLoop();
			}
		};
		gameThread.start();
	}

	/**
	 * Tell game loop to stop running, after which the LWJGL Display will be
	 * destoryed. The main thread will wait for the Display.destroy().
	 */
	private void stopLWJGL() {
		running = false;
		try {
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void start() {

		lastFPS = getTime();

		versionCheck();

		level = new Level();

		fetchPlayerControlled();

		resetSim();

		ClientStateManager.setPaused(true);
	}

	public void stop() {

	}

	/**
	 * Applet Destroy method will remove the canvas, before canvas is destroyed
	 * it will notify stopLWJGL() to stop the main game loop and to destroy the
	 * Display
	 */
	public void destroy() {
		remove(display_parent);
		super.destroy();
	}

	public void init() {
		
		ClientDefaults.init(this);
		System.out.println("player ID: " + ClientDefaults.playerID());
		
		setLayout(new BorderLayout());
		try {
			display_parent = new Canvas() {
				public final void addNotify() {
					super.addNotify();
					startLWJGL();
				}

				public final void removeNotify() {
					stopLWJGL();
					super.removeNotify();
				}
			};
			display_parent.setSize(getWidth(), getHeight());
			add(display_parent);
			display_parent.setFocusable(true);
			display_parent.requestFocus();
			display_parent.setIgnoreRepaint(true);
			setVisible(true);
		} catch (Exception e) {
			System.err.println(e);
			throw new RuntimeException("Unable to create display");
		}
	}

	protected void initGL() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 0, 600, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public void gameLoop() {
		while (running) {
			final long delta = getDelta();
			onUpdate(delta);
			renderGL(delta);
			Display.sync(60);
			Display.update();
		}

		Display.destroy();
	}

	public static void onUpdate(long delta) {
		
		try {
			if (ClientStateManager.getFlowState() == FlowState.FREE)
				ClientStateManager.setFlowState(FlowState.PLAYING);

			KeyUtil.update();

			level.onUpdate(ClientStateManager.isPaused() || VectorEditorPopup_Crummy.isBlocking() ? 0 : delta);

			if (KeyUtil.isKeyPressed(Keyboard.KEY_RETURN)
					|| KeyUtil.isKeyPressed(Keyboard.KEY_ESCAPE))
				ClientStateManager.togglePauseState();
			
			if (KeyUtil.isKeyPressed(Keyboard.KEY_F1) && ClientDefaults.devMode()) {
				VectorEditorPopup_Crummy.show(ClientDefaults.server().getExplorationVector().getGenes(), true, "Adjust allowable values");
			}
			
			if (!ClientStateManager.isPaused() && VectorEditorPopup_Crummy.isVisible()) {
				VectorEditorPopup_Crummy.hide();
			}
			
		} catch (final Exception e) {
			e.printStackTrace();
		}
		updateFPS();

	}

	/**
	 * Build the level and entities from scratch. Update appropriate GUI
	 * components.
	 */
	public static void resetSim() {

		final Level level = new Level();

		final LevelGeneratorCore chainer;
		level.getBlankEntity(EntityType.GENERATOR).addTrait(
				chainer = new LevelGeneratorCore());
		level.setGeneratorCore(chainer);

		for (int i = 0; i < 200; i++) {
			Entity e = level.getBlankEntity(EntityType.BG);
			EntityFactory.buildBackgroundStar(e);
		}

		level.addRenderBehavior(new Behavior() {
			public void onStart(Entity self, Level level) {
			}

			public void onUpdate(final Entity self, final Level level,
					final long delta) {
				RenderUtil.setColor(Color.CYAN);
				// final String playerHealth = level.getPlayer() == null ? "XX"
				// : Integer.toString((int) level.getPlayer().getHealth());
				RenderUtil
						.drawString(
								new StringBuffer().append("")
										// .append("health ").append(playerHealth)
										.append("\n\n\n\n")
										.append("wave ")
										.append(level.getGeneratorCore().getWaveCount()).toString(), 5, 45);
				RenderUtil.setColor(Color.GREEN);
				RenderUtil.drawString("Progress "
						+ level.getGeneratorCore().getPercentComplete()
						+ (ClientDefaults.devMode() ? "\n\nF1 : Edit Vector": "")
						, 5,
						Display.getHeight() - 15);
			}

			public void onDeath(Entity self, Level level) {
			}
		});

		level.onDeath();
		TraitProjectClient.level = level;

		ClientStateManager.setFlowState(FlowState.FREE);
	}

	public static void renderGL(final long delta) {
		// Clear The Screen And The Depth Buffer
		try {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			level.renderGL(delta);

			if (ClientStateManager.isPaused()) {
				RenderUtil.setColor(Color.WHITE);
				GL11.glPushMatrix();
				{
					GL11.glTranslatef(Display.getWidth() / 2,
							Display.getHeight() / 2, 0);
					RenderUtil.drawString("PAUSED", 5);
					GL11.glTranslatef(0, -Display.getHeight() / 8, 0);
					RenderUtil.drawString("Press <ENTER>", 3);
				}
				GL11.glPopMatrix();
			}
		} catch (final Exception e) {
		}
	}

	/**
	 * Calculate how many milliseconds have passed since last frame.
	 * 
	 * @return milliseconds passed since last frame
	 */
	public static int getDelta() {
		/*final long time = getTime();
		final int delta = (int) (time - lastFrame);
		lastFrame = time;

		return delta;*/
		return 16;
	}

	public static long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public static void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}

	public static void versionCheck() {
		final Client c = getClient();

		try {
			c.reconnect();
			c.send("versioncheck" + VERSION);
			boolean sameVersion = Boolean.parseBoolean(c.receive());
			if (!sameVersion) {
				JOptionPane.showMessageDialog(null,
								"Your client is out-of-date, please download the latest version.");
				System.exit(0);
			} else {
				System.out.println("Client and Server versions match.");
				return;
			}
		} catch (Exception e) {

		} finally {
			try {
				c.close();
			} catch (Exception e) {
			}
		}
		System.err.println("Cannot check server version.");
	}

	public static List<GeneWrapper> getPlayerControlledGenes() {
		final GeneVector geneVector = ClientDefaults.server().getExplorationVector();
		final List<GeneWrapper> genes = new ArrayList<GeneWrapper>();
		for (final String string : playerControlled) {
			genes.add(geneVector.getGeneWrapper(string));
		}
		return genes;
	}

	public static Client getClient() {

		if (ClientDefaults.localServer()) {
			return new Client();
		} else {
			return new Client(ClientDefaults.serverIp(), 8000);
		}
	}

	private static void fetchPlayerControlled() {
		ClientStateManager.setFlowState(FlowState.LOADING_VECT);
		try {
			final Client c = TraitProjectClient.getClient();
			try {
				c.reconnect();
				c.send("playerControlled");
				final String s = c.receive();
				if (s.trim().isEmpty())
					playerControlled = new String[0];
				else
					playerControlled = s.split(SessionInfo.SEPERATOR);
				System.out
						.println("Successfully read player-controlled gene list from server.");
				return;
			} catch (IOException e) {
			} finally {
				try {
					c.close();
					return;
				} catch (Exception e) {
				}
			}
			System.err
					.println("Could not fetch player-controlled gene list from server.");
			playerControlled = new String[0];
		} finally {
			ClientStateManager.setFlowState(FlowState.FREE);
		}
	}
}