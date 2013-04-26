package efruchter.tp;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

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
import efruchter.tp.util.KeyHolder;
import efruchter.tp.util.RepeatingTimer;
import efruchter.tp.util.RepeatingTimer.RepeatingTimerAction;

@SuppressWarnings("serial")
public class TraitProjectClient extends Applet {

    public final static Dimension SIZE = new Dimension(800, 600);

    private BufferStrategy bufferStrategy;
    private Canvas drawArea;/* Drawing Canvas */
    private final boolean fixedFrameRate = false;

    //Fonts
    public static final Font PAUSE_FONT = new Font("SansSerif", Font.BOLD, 32);
    public static final Font GUI_FONT = new Font("SansSerif", Font.BOLD, 20);
    public static final Font NEW_WAVE_FONT = new Font("SansSerif", Font.PLAIN, 50);

    /*
     * GAME VARS
     */
    public static final String VERSION = "00.00.00.04";
    private static Level level;

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
	public static long display_score;
	public static long playerID;

	public static void resetMetrics() {
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
		display_score = 0;
		System.out.println("resetting metrics");
	}

	private static String[] playerControlled = new String[0];

    @Override
    public void init() {

        drawArea = new Canvas();
        setIgnoreRepaint(true);

        ClientDefaults.init(this);

        setLayout(new BorderLayout());

        versionCheck();

        playerID = getUniqueID();

        level = new Level();

        fetchPlayerControlled();

        resetSim();

        ClientStateManager.setPaused(true);

        drawArea.addKeyListener(KeyHolder.get());
        drawArea.setFocusable(true);

        drawArea.setSize(new Dimension(getWidth(), getHeight()));
        add(drawArea);
        drawArea.createBufferStrategy(2);
        bufferStrategy = drawArea.getBufferStrategy();

        RepeatingTimer a = new RepeatingTimer(new RepeatingTimerAction(){
            @Override
            public void update(long lastFrameDelta) {

                // Update any sprites or other graphical objects
                onUpdate(fixedFrameRate ? lastFrameDelta : 16);

                // Handle Drawing
                try {
                    Graphics2D g = getGraphics();
                    render(g);

                    // Dispose of graphics context
                    g.dispose();
                } catch (final NullPointerException exc) {
                    //It's cool, the graphics can crash.
                }
            }
        }, 1000/60);
        a.start();
    }

    public static void onUpdate(long delta) {

        try {
            if (ClientStateManager.getFlowState() == FlowState.FREE)
                ClientStateManager.setFlowState(FlowState.PLAYING);

            KeyHolder holder = KeyHolder.get();

            level.onUpdate(ClientStateManager.isPaused() || VectorEditorPopup_Crummy.isBlocking() ? 0 : delta);

            if (holder.isPressedThenRelease(KeyEvent.VK_ENTER) || holder.isPressedThenRelease(KeyEvent.VK_ESCAPE))
                ClientStateManager.togglePauseState();

            if (holder.isPressedThenRelease(KeyEvent.VK_F1) && ClientDefaults.devMode()) {
                VectorEditorPopup_Crummy.show(ClientDefaults.server().getExplorationVector().getGenes(), true, "Adjust allowable values");
            }

            if (!ClientStateManager.isPaused() && VectorEditorPopup_Crummy.isVisible()) {
                VectorEditorPopup_Crummy.hide();
            }

            KeyHolder.get().freeQueuedKeys();

        } catch (final Exception e) {
            e.printStackTrace();
        }

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

        level.onDeath();
        TraitProjectClient.level = level;

        ClientStateManager.setFlowState(FlowState.FREE);
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

    private static long getUniqueID() {
        ClientStateManager.setFlowState(FlowState.FETCHING_ID);
        try {
            final Client c = TraitProjectClient.getClient();
            try {
                c.reconnect();
                c.send("getID");

                System.out.println("Successfully ID fetched from server.");

                return Long.parseLong(c.receive());
            } catch (Exception e) {
            	System.err.println("failed to get ID");
            	e.printStackTrace();
            } finally {
                try {
                    c.close();
                } catch (Exception e) {
                }
            }
            System.err.println("Cannot fetch ID!.");
        } finally {
            ClientStateManager.setFlowState(FlowState.FREE);
        }
		return -1;
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
			return new Client(ClientDefaults.serverPort());
		} else {
			return new Client(ClientDefaults.serverIp(), ClientDefaults.serverPort());
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

    public void render(Graphics2D backg) {

        if (!bufferStrategy.contentsLost()) {
            // Show bufferStrategy
            bufferStrategy.show();
        }

        backg.setColor(Color.BLACK);
        backg.fillRect(0, 0, SIZE.width, SIZE.height);

        backg.scale(1, -1);
        backg.translate(0, -600);
        level.render(backg);

        backg.translate(0, 600);
        backg.scale(1, -1);

        backg.setColor(Color.WHITE);

        if (ClientStateManager.isPaused()) {
            backg.setFont(PAUSE_FONT);
            backg.drawString("PAUSED", SIZE.width / 2, SIZE.height / 2);
        }

        backg.setFont(GUI_FONT);

        backg.drawString("Score: " + TraitProjectClient.display_score, 0, SIZE.height - 25);
        backg.drawString("Wave: " + level.getGeneratorCore().getWaveCount(), 0, SIZE.height - 5);

        backg.drawString("Progress: " + level.getGeneratorCore().getPercentComplete(), 0, 25);
    }

    public TraitProjectClient() {
        if (System.getProperty("os.name").contains("Windows")) {
            System.setProperty("sun.java2d.d3d", "True");
        } else {
            System.setProperty("sun.java2d.opengl", "True");
        }
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Graphics2D getGraphics() {
        return (Graphics2D) bufferStrategy.getDrawGraphics();
    }
    
    @Override
    public void start() {
        requestFocusInWindow();
    }

    public static void storeData(ServerIO v, long waveCount, boolean isRandom, String learnMode) {
        String username = System.getProperty("user.name");
        if (username == null) {
            username = "NO_NAME";
        }
        SessionInfo info = new SessionInfo();
        info.put("username", username);
        info.put("date", Long.toString(System.currentTimeMillis()));
        info.put("vector", v.getExplorationVector().toDataString());
//        System.out.println("saving exploration vector: " + v.getExplorationVector().toDataString());
        info.put("pID", Long.toString(TraitProjectClient.playerID));
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
        info.put("display_score", Long.toString(TraitProjectClient.display_score));
        info.put("is_random", Boolean.toString(isRandom));
        info.put("learn_mode", learnMode);
        info.put("c_choice", (TraitProjectClient.c_choice).toString());
        v.storeInfo(info);

        TraitProjectClient.resetMetrics();
    }
}