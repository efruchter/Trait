package efruchter.tp.gui.panels;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class StatisticsPanel extends JPanel {
	
	private JLabel fpsLabel, entitesLabel;
	
	public StatisticsPanel() {
		setLayout(new BorderLayout());
		
		JPanel current = new JPanel();
		current.setBorder(BorderFactory.createTitledBorder("Session/ Current"));
		current.setLayout(new BoxLayout(current, BoxLayout.Y_AXIS));
		add(current, BorderLayout.NORTH);
		current.add(fpsLabel = new JLabel(""));
		current.add(entitesLabel = new JLabel(""));
		
		JPanel overll = new JPanel();
		overll.setBorder(BorderFactory.createTitledBorder("Global Statistics"));
		overll.setLayout(new BoxLayout(overll, BoxLayout.Y_AXIS));
		add(overll, BorderLayout.CENTER);
		overll.add(new JLabel("No Session Statistics Available."));
	}
	
	public void setFPS(long fps) {
		fpsLabel.setText("FPS: " + fps);
	}
	
	public void setEntityCount(long entities) {
		entitesLabel.setText("Entity Count: " + entities);
	}
}
