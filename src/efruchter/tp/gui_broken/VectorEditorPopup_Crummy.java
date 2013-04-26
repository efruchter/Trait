package efruchter.tp.gui_broken;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



import efruchter.tp.CHOICE;
import efruchter.tp.TraitProjectClient;
import efruchter.tp.learning.GeneVector.GeneWrapper;
import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;
import efruchter.tp.util.KeyHolder;


public class VectorEditorPopup_Crummy {

    private final static int RESOLUTION = 1000;

    private static JFrame frame;
    //private static Point frameLoc = null;
    private static boolean blocking = false;
    private static ActionListener onHideAction;

    private static void rebuildGui(final List<GeneWrapper> genes, final boolean useName, final String headerText) {
    	rebuildGui(genes, useName, headerText, null, -1, false, "err");
    }
    
    @SuppressWarnings("serial")
    private static void rebuildGui(final List<GeneWrapper> genes, final boolean useName, final String headerText, 
    		final ServerIO v, final long waveCount, final boolean isRandom, final String learnMode) {

    	System.out.println("entering rebuildGui");
    	
    	ClientStateManager.setPaused(true);
    	
        frame = new JFrame("Control Panel");
        
        //frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                	if (v != null) {
                		// if called w/storage data, record preference information instead
                		TraitProjectClient.storeData(v, waveCount, isRandom, learnMode);
                	}
                    hide();
                }
            }
        });

        frame.add(new JPanel() {
            {
                add(new JLabel(headerText));
                this.requestFocus();
            }
        }, BorderLayout.NORTH);

        final JPanel traitPanel = new JPanel();
        traitPanel.setLayout(new GridLayout(genes.size(), 1));
        traitPanel.setLayout(new GridLayout(genes.size()+2, 1));

        for (final GeneWrapper gene : genes) {
            final JPanel subPanel = new JPanel();
            traitPanel.add(subPanel);

            subPanel.add(new JLabel(useName ? gene.path : gene.gene.getInfo()));

            final JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, RESOLUTION, (int) (gene.gene.getExpression() * RESOLUTION));
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent changeEvent) {
                    gene.gene.setExpression((float) slider.getValue() / RESOLUTION);
                }
            });
            subPanel.add(slider);
        }

        frame.add(new JScrollPane(traitPanel){{setPreferredSize(new Dimension(700, 300));}}, BorderLayout.CENTER);

        String betterString = "better than before";
        String worseString = "worse than before";
        final JRadioButton choiceBetter = new JRadioButton(betterString);
        choiceBetter.setActionCommand(betterString);
        JRadioButton choiceWorse = new JRadioButton(worseString);
        choiceWorse.setActionCommand(worseString);
        ButtonGroup classificationChoice = new ButtonGroup();
        classificationChoice.add(choiceBetter);
        classificationChoice.add(choiceWorse);
        
        choiceBetter.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent ae) {
//        		System.out.println("picked better");
        		TraitProjectClient.c_choice = CHOICE.BETTER;
        	}
        });
        choiceWorse.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent ae) {
//        		System.out.println("picked worse");
        		TraitProjectClient.c_choice = CHOICE.WORSE;
        	}
        });
        traitPanel.add(choiceBetter);
        traitPanel.add(choiceWorse);
                
        final JButton goButton = new JButton("Go!");
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	if (v != null) {
            		// if called w/storage data, record preference information instead
            		TraitProjectClient.storeData(v, waveCount, isRandom, learnMode);
//            		TraitProjectClient.resetMetrics();

            	}
                hide();
            }
        });
        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                choiceBetter.requestFocusInWindow();
            }
        });
        frame.add(goButton, BorderLayout.SOUTH);;
        frame.pack();
        frame.setLocation(new Point(TraitProjectClient.getClientInstance().getY() + TraitProjectClient.getClientInstance().getWidth() / 2 - frame.getWidth() / 2,
        		TraitProjectClient.getClientInstance().getY() + TraitProjectClient.getClientInstance().getHeight() / 2 - frame.getHeight() / 2));
    }

    public static void show(final List<GeneWrapper> genes, final boolean useName, final String headerText) {
    	show(genes, useName, headerText, null);
    }
    
    public static void show(final List<GeneWrapper> genes, final boolean useName, final String headerText, final ActionListener onHideAction) {
    	show(genes, useName, headerText, false, null, -1, false, "err");
    }


    public static void show(final List<GeneWrapper> genes, final boolean useName, final String headerText, final boolean forceShow, 
    		final ServerIO v, long waveCount, boolean isRandom, String learnMode) {

    	hide();
        
        // Wire an action for next hide
        VectorEditorPopup_Crummy.onHideAction = onHideAction;

        ClientStateManager.setPaused(true);
        ClientStateManager.setFlowState(FlowState.EDITING);

        Collections.sort(genes);
        
        rebuildGui(genes, useName, headerText, v, waveCount, isRandom, learnMode);

        if (genes.isEmpty() && !forceShow) {
            hide();
        } else {
            KeyHolder.get().clearKeys();
            frame.setVisible(true);
        }
    }

    public static void hide() {
        if (frame != null) {
        	//frameLoc = frame.getLocation();
            frame.setVisible(false);
            ClientStateManager.setFlowState(FlowState.FREE);
            frame.dispose();
        }
        if (onHideAction != null) {
        	onHideAction.actionPerformed(null);
        }
        frame = null;
        onHideAction = null;
        ClientStateManager.setPaused(false);
        TraitProjectClient.getClientInstance().requestFocusInWindow();
        KeyHolder.get().clearKeys();
    }
    
    public static void setEnabled(boolean isEnabled) {
    	if (frame != null) {
    		frame.setEnabled(isEnabled);
    	}
    }
    
    /**
     * Block the core game thread until the window is closed.
     */
    public static void blockWhileOpen() {
    	blocking = true;
    	System.out.println("starting blocking");
    	while (frame != null && frame.isVisible()) {
    		try {
    			System.out.println("starting sleeping");
				Thread.sleep(100);
				System.out.println("done sleeping");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch(Throwable t) {
			    System.err.println(""+t);
			}
    	}
    	blocking = false;
    	System.out.println("finished blocking");
    }
    
    public static boolean isVisible() {
    	return frame != null;
    }

	public static boolean isBlocking() {
		return blocking;
	}
}