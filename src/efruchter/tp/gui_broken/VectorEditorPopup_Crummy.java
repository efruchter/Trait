package efruchter.tp.gui_broken;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

import org.lwjgl.opengl.Display;

import efruchter.tp.CHOICE;
import efruchter.tp.TraitProjectClient;
import efruchter.tp.learning.GeneVector.GeneWrapper;
import efruchter.tp.learning.server.ServerIO;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;
import efruchter.tp.util.KeyUtil;

public class VectorEditorPopup_Crummy {

    private final static int RESOLUTION = 1000;

    private static JFrame frame;
    private static Point frameLoc = null;

    @SuppressWarnings("serial")
    private static void rebuildGui(final List<GeneWrapper> genes, final boolean useName, final String headerText, final ServerIO v, final long waveCount) {

        frame = new JFrame("Control Panel");
        
        //frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                	if (v != null) {
                		// if called w/storage data, record preference information instead
                		TraitProjectClient.storeData(v, waveCount);
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

        String betterString = "this was better than last time";
        String worseString = "this was worse than last time";
        JRadioButton choiceBetter = new JRadioButton(betterString);
        choiceBetter.setActionCommand(betterString);
        JRadioButton choiceWorse = new JRadioButton(worseString);
        choiceWorse.setActionCommand(worseString);
        ButtonGroup classificationChoice = new ButtonGroup();
        classificationChoice.add(choiceBetter);
        classificationChoice.add(choiceWorse);
        
        choiceBetter.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent ae) {
        		System.out.println("picked better");
        		TraitProjectClient.c_choice = CHOICE.BETTER;
        	}
        });
        choiceWorse.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent ae) {
        		System.out.println("picked worse");
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
            		TraitProjectClient.storeData(v, waveCount);
            	}
                hide();
            }
        });
        frame.add(goButton, BorderLayout.SOUTH);

        frame.pack();
    }
    
    private static void rebuildGui(final List<GeneWrapper> genes, final boolean useName, final String headerText) {
    	rebuildGui(genes, useName, headerText, null, -1);
    }
    
    public static void show(final List<GeneWrapper> genes, final boolean useName, final String headerText) {
    	show(genes, useName, headerText, false);
    }
    
    public static void show(final List<GeneWrapper> genes, final boolean useName, final String headerText, final boolean forceShow) {
    	show(genes, useName, headerText, false, null, -1);
    }


    public static void show(final List<GeneWrapper> genes, final boolean useName, final String headerText, final boolean forceShow, ServerIO v, long waveCount) {

        hide();

        ClientStateManager.setPaused(true);
        ClientStateManager.setFlowState(FlowState.EDITING);

        Collections.sort(genes);
        
        rebuildGui(genes, useName, headerText, v, waveCount);

        if (genes.isEmpty() && !forceShow) {
            hide();
        } else {
            KeyUtil.clearKeys();
            if (frameLoc == null) {
            	frameLoc = new Point(Display.getX() + Display.getWidth() / 2 - frame.getWidth() / 2, Display.getY() + Display.getHeight() / 2
                        - frame.getHeight() / 2);
            }
            frame.setLocation(frameLoc);
            frame.setVisible(true);
        }
    }
    

    public static void hide() {
        if (frame != null) {
        	frameLoc = frame.getLocation();
            frame.setVisible(false);
            ClientStateManager.setFlowState(FlowState.FREE);
            frame.dispose();
        }
        frame = null;
        ClientStateManager.setPaused(false);
    }
    
    public static boolean isVisible() {
    	return frame != null;
    }
}