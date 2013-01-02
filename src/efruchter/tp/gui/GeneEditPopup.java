package efruchter.tp.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.trait.gene.Gene;
import org.lwjgl.opengl.Display;

import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;

public class GeneEditPopup {

    private static JFrame frame;

    @SuppressWarnings("serial")
    private static void buildGUI() {
        frame = new JFrame("Control Panel");
        frame.setBackground(Color.BLACK);

        /*
         * Build core GUI
         */
        {
            frame.add(new JPanel(){{add(new JLabel(
                    "A new wave is about to attack! Customize your ship's traits!"
                    ){{setForeground(Color.WHITE); setBackground(Color.BLACK);}});}},
                    BorderLayout.NORTH);

            final Gene[] genes = TraitProjectClient.getPlayerControlledGenes();

            final JPanel traitPanel = new JPanel();
            traitPanel.setLayout(new GridLayout(genes.length, 1));

            for (final Gene gene : genes) {
                final JPanel subPanel = new JPanel();
                traitPanel.add(subPanel);

                subPanel.add(new JLabel(gene.getInfo()){{setForeground(Color.WHITE);}});

                final JSlider slider = new JSlider();
                subPanel.add(slider);
            }

            frame.add(traitPanel, BorderLayout.CENTER);

            final JButton goButton = new JButton("Start!");
            goButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    hide();
                }
            });
            frame.add(goButton, BorderLayout.SOUTH);
        }

        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowLostFocus(WindowEvent e) {
                hide();
            }
        });

        frame.setFocusable(true);
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hide();
                }
            }
        });

        frame.setUndecorated(true);
        frame.pack();
        frame.setResizable(false);
    }

    public static void show() {

        ClientStateManager.setPaused(true);
        ClientStateManager.setFlowState(FlowState.EDITING);

        if (frame == null)
            buildGUI();

        if (TraitProjectClient.getPlayerControlledGenes().length == 0) {
            hide();
            return;
        }

        frame.setVisible(true);
        frame.setLocation(Display.getX() + Display.getWidth() / 2 - frame.getWidth() / 2,
                Display.getY() + Display.getHeight() / 2 - frame.getHeight() / 2);
    }

    public static void hide() {
        frame.setVisible(false);
        ClientStateManager.setFlowState(FlowState.FREE);
        ClientStateManager.setPaused(false);
    }
}
