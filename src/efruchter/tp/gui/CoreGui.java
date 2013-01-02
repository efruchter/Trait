package efruchter.tp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lwjgl.opengl.Display;

import efruchter.tp.state.ClientStateManager;
import efruchter.tp.state.ClientStateManager.FlowState;

public class CoreGui {

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

        if (frame == null) {
            buildGUI();
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
