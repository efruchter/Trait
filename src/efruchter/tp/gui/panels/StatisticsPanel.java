package efruchter.tp.gui.panels;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import efruchter.tp.TraitProjectClient;
import efruchter.tp.trait.generators.LevelGenerator_Chainer;

@SuppressWarnings("serial")
public class StatisticsPanel extends JPanel {

	private final JLabel fpsLabel, entitesLabel, genChainerInfo;

	public StatisticsPanel() {
		setLayout(new GridLayout(2, 1));

		final JPanel current = new JPanel();
		current.setBorder(BorderFactory.createTitledBorder("Session/ Current"));
		current.setLayout(new BoxLayout(current, BoxLayout.Y_AXIS));
		add(current);
		current.add(fpsLabel = new JLabel(""));
		current.add(entitesLabel = new JLabel(""));

		final JPanel overll = new JPanel();
		overll.setBorder(BorderFactory.createTitledBorder("Global Statistics"));
		overll.setLayout(new BoxLayout(overll, BoxLayout.Y_AXIS));
		add(overll);
		overll.add(new JLabel("Client Version: " + TraitProjectClient.VERSION));
		overll.add(genChainerInfo = new JLabel("Generator Info"));
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

	public void setGenInfo(final LevelGenerator_Chainer chainer) {
		StringBuffer b = new StringBuffer();
		//time
		b.append("Time: ").append(chainer.time).append("/").append(chainer.LEVEL_LENGTH);
		b.append(" | ");
		b.append("P(NC): ").append(chainer.probNewChain);
		
		genChainerInfo.setText(b.toString());
	}
}
