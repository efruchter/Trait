package trts.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import trts.genes.Gene;
import trts.traits.Trait;

public class TraitViewer extends JPanel {

	public TraitViewer(final Trait trait) {

		setLayout(new BorderLayout());
		add(new JLabel(trait.getName()), BorderLayout.NORTH);
		setToolTipText(trait.getInfo());

		JPanel center = new JPanel();

		for (final Gene gene : trait.getGenes()) {
			JPanel p = new JPanel();
			p.setBorder(BorderFactory.createTitledBorder(gene.getName()));

			final JSlider c = new JSlider(JSlider.VERTICAL, 0, 100,
					(int) (gene.getExpression() * 100));
			c.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					gene.setExpression(c.getValue() / 100f);
				}
			});
			c.setToolTipText(gene.getInfo());
			p.add(c);
			center.add(p);
		}

		add(center, BorderLayout.CENTER);
	}

	public void showTrait() {
		JFrame frame = new JFrame("Trait Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
	}
}
