package efruchter.tp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lwjgl.opengl.Display;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;
import efruchter.tp.trait.gene.Gene;
import efruchter.tp.util.KeyUtil;

public class GeneEditPopup {

    private static JFrame frame;

    private static JSlider[] sliders;
    private static Gene[] genes;

    private final static int RESOLUTION = 1000;

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
                    ){{setForeground(Color.WHITE); setBackground(Color.BLACK);}});
                    this.requestFocus(); }},
                    BorderLayout.NORTH);

            sliders = new JSlider[genes.length];

            final JPanel traitPanel = new JPanel();
            traitPanel.setLayout(new GridLayout(genes.length, 1));

            for (int i = 0; i < genes.length; i++) {
                final JPanel subPanel = new JPanel();
                traitPanel.add(subPanel);

                subPanel.add(new JLabel(genes[i].getInfo()){{setForeground(Color.WHITE);}});

                final int sl = i;
                sliders[sl] = new JSlider(JSlider.HORIZONTAL, 0, RESOLUTION, 0);
                sliders[sl].addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent changeEvent) {
                        genes[sl].setExpression((float) sliders[sl].getValue() / RESOLUTION);
                    }
                });
                subPanel.add(sliders[sl]);
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

        genes = TraitProjectClient.getPlayerControlledGenes();

        if (frame == null)
            buildGUI();

        if (genes.length == 0) {
            hide();
        } else {
            KeyUtil.clearKeys();
            for (int i = 0; i < genes.length; i++)
                sliders[i].setValue((int) (genes[i].getExpression() * 1000));
            frame.setVisible(true);
            frame.setLocation(Display.getX() + Display.getWidth() / 2 - frame.getWidth() / 2,
                    Display.getY() + Display.getHeight() / 2 - frame.getHeight() / 2);
        }
    }

    public static void hide() {
        frame.setVisible(false);
        ClientStateManager.setFlowState(FlowState.FREE);
        ClientStateManager.setPaused(false);
    }
}
