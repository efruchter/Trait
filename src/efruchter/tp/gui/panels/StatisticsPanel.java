package efruchter.tp.gui.panels;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import efruchter.tp.TraitProjectClient;

@SuppressWarnings("serial")
public class StatisticsPanel extends JPanel {
	
	private final JLabel fpsLabel, entitesLabel;

	public StatisticsPanel() {
		setLayout(new BorderLayout());

        final JPanel current = new JPanel();
		current.setBorder(BorderFactory.createTitledBorder("Session/ Current"));
		current.setLayout(new BoxLayout(current, BoxLayout.Y_AXIS));
		add(current, BorderLayout.NORTH);
		current.add(fpsLabel = new JLabel(""));
		current.add(entitesLabel = new JLabel(""));

        final JPanel overll = new JPanel();
		overll.setBorder(BorderFactory.createTitledBorder("Global Statistics"));
		overll.setLayout(new BoxLayout(overll, BoxLayout.Y_AXIS));
		add(overll, BorderLayout.CENTER);
		overll.add(new JLabel("Client Version: " + TraitProjectClient.VERSION));
	}
	
	private long fps, entities;
	
	public void setFPS(long fps) {
		if (this.fps != fps)
			fpsLabel.setText("FPS: " + (this.fps = fps));
	}
	
	public void setEntityCount(long entities) {
		if (this.entities != entities)
			entitesLabel.setText("Entities: " + (this.entities = entities));
	}
}
