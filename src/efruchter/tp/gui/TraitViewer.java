package efruchter.tp.gui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import efruchter.tp.entities.Ship;
import efruchter.tp.traits.Trait;
import efruchter.tp.traits.genes.Gene;

@SuppressWarnings("serial")
public class TraitViewer extends JPanel {

	public TraitViewer(final Ship entity) {

		setBorder(BorderFactory.createTitledBorder(entity.name));

		for (Trait trait : entity.getTraits()) {
			JPanel center = new JPanel();
			center.setToolTipText(trait.getInfo());
			center.setBorder(BorderFactory.createTitledBorder(trait.getName()));

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

			add(center);
		}

		JFrame frame = new JFrame("Trait Viewer");
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
	}
}
