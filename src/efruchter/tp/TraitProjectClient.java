package efruchter.tp;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.Timer;


import efruchter.tp.entity.Entity;
import efruchter.tp.entity.EntityFactory;
import efruchter.tp.entity.EntityType;
import efruchter.tp.entity.Level;
import efruchter.tp.gui_broken.VectorEditorPopup_Crummy;
import efruchter.tp.learning.GeneVector.GeneWrapper;
import efruchter.tp.learning.server.comm.Client;
import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.SessionInfo;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.util.KeyHolder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Graphics2D;

@SuppressWarnings("serial")
public class TraitProjectClient extends JApplet {

    public static Dimension SIZE = new Dimension(800, 600);    

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
	public static float s_fired_player;
	public static float s_fired_enemies;
	public static float s_killed_enemies;
	public static long displayScore;
	public static long playerID;

	public static void resetMetrics() {
		s_damage_player = 0;
		s_damage_enemies = 0;
		s_num_enemies = 0;
		s_fired_player = 0;
		s_fired_enemies = 0;
		s_killed_enemies = 0;
		displayScore = 0;
	}

	private static String[] playerControlled = new String[0];


	public void init() {
		
		ClientDefaults.init(this);
		
		setLayout(new BorderLayout());
		
		versionCheck();
        
        playerID = getUniqueID();

        level = new Level();

        fetchPlayerControlled();

        resetSim();

        ClientStateManager.setPaused(true);
        
        addKeyListener(KeyHolder.get());
        
        setBackground(Color.BLACK);
		
		new Timer(16, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                level.onUpdate(16);
                repaint();
            }
		    
		}).start();
		
	}

	public static void onUpdate(long delta) {
		
		try {
			if (ClientStateManager.getFlowState() == FlowState.FREE)
				ClientStateManager.setFlowState(FlowState.PLAYING);

			KeyHolder holder = KeyHolder.get();

			level.onUpdate(ClientStateManager.isPaused() || VectorEditorPopup_Crummy.isBlocking() ? 0 : delta);

			if (holder.isPressedThenRelease(KeyEvent.VK_ENTER)
					|| holder.isPressedThenRelease(KeyEvent.VK_ESCAPE))
				ClientStateManager.togglePauseState();
			
			if (holder.isPressedThenRelease(KeyEvent.VK_F1) && ClientDefaults.devMode()) {
				VectorEditorPopup_Crummy.show(ClientDefaults.server().getExplorationVector().getGenes(), true, "Adjust allowable values");
			}
			
			if (!ClientStateManager.isPaused() && VectorEditorPopup_Crummy.isVisible()) {
				VectorEditorPopup_Crummy.hide();
			}
			
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

		level.addRenderBehavior(new Behavior() {
			public void onStart(Entity self, Level level) {
			}

			public void onUpdate(final Entity self, final Level level,
					final long delta) {
				/*RenderUtil.setColor(Color.CYAN);
				// final String playerHealth = level.getPlayer() == null ? "XX"
				// : Integer.toString((int) level.getPlayer().getHealth());
				RenderUtil
						.drawString(
								new StringBuffer().append("")
										// .append("health ").append(playerHealth)
										.append("\n")
										.append("score ").append(displayScore)
										.append("\n\n")
										.append("wave ")
										.append(level.getGeneratorCore().getWaveCount()).toString(), 5, 45);
				RenderUtil.setColor(Color.GREEN);
				RenderUtil.drawString("Progress "
						+ level.getGeneratorCore().getPercentComplete()
						+ (ClientDefaults.devMode() ? "\n\nF1 : Edit Vector": "")
						, 5,
						TraitProjectClient.SIZE.height - 15);*/
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
		/*try {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			level.renderGL(delta);

			if (ClientStateManager.isPaused()) {
				RenderUtil.setColor(Color.WHITE);
				GL11.glPushMatrix();
				{
					GL11.glTranslatef(TraitProjectClient.SIZE.width / 2,
							TraitProjectClient.SIZE.height / 2, 0);
					RenderUtil.drawString("PAUSED", 5);
					GL11.glTranslatef(0, -TraitProjectClient.SIZE.height / 8, 0);
					RenderUtil.drawString("Press <ENTER>", 3);
				}
				GL11.glPopMatrix();
			}
		} catch (final Exception e) {
		}*/
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
	
	@Override
	public void paint(Graphics g) {
	    g.clearRect(0, 0, SIZE.width, SIZE.height);
	    ((Graphics2D)g).scale(1, -1);
	    ((Graphics2D)g).translate(0, -600);
	    level.render(g);
	}
}