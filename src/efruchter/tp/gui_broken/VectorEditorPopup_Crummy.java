package efruchter.tp.gui_broken;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lwjgl.opengl.Display;

import efruchter.tp.learning.GeneVector.GeneWrapper;
import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;
import efruchter.tp.util.KeyUtil;

public class VectorEditorPopup_Crummy {

    private final static int RESOLUTION = 1000;

    private static JFrame frame;

    @SuppressWarnings("serial")
    private static void rebuildGui(final List<GeneWrapper> genes, final boolean useName, final String headerText) {

        frame = new JFrame("Control Panel");

        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowLostFocus(WindowEvent e) {
                hide();
            }
        });
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hide();
                }
            }
        });

        frame.setBackground(Color.BLACK);

        frame.add(new JPanel() {
            {
                add(new JLabel(headerText) {
                    {
                        setForeground(Color.WHITE);
                        setBackground(Color.BLACK);
                    }
                });
                this.requestFocus();
            }
        }, BorderLayout.NORTH);

        final JPanel traitPanel = new JPanel();
        traitPanel.setLayout(new GridLayout(genes.size(), 1));

        for (final GeneWrapper gene : genes) {
            final JPanel subPanel = new JPanel();
            traitPanel.add(subPanel);

            subPanel.add(new JLabel(useName ? gene.path : gene.gene.getInfo()) {
                {
                    setForeground(Color.WHITE);
                }
            });

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

        final JButton goButton = new JButton("Go!");
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                hide();
            }
        });
        frame.add(goButton, BorderLayout.SOUTH);

        frame.pack();
    }

    public static void show(final List<GeneWrapper> genes, final boolean useName, final String headerText) {

        hide();

        ClientStateManager.setPaused(true);
        ClientStateManager.setFlowState(FlowState.EDITING);

        Collections.sort(genes);
        
        rebuildGui(genes, useName, headerText);

        if (genes.isEmpty()) {
            hide();
        } else {
            KeyUtil.clearKeys();
            frame.setVisible(true);
            frame.setLocation(Display.getX() + Display.getWidth() / 2 - frame.getWidth() / 2, Display.getY() + Display.getHeight() / 2
                    - frame.getHeight() / 2);
        }
    }

    public static void hide() {
        if (frame != null) {
            frame.setVisible(false);
            ClientStateManager.setFlowState(FlowState.FREE);
            ClientStateManager.setPaused(false);
            frame.dispose();
        }
        frame = null;
    }
}