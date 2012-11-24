package efruchter.tp.gui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import efruchter.tp.entities.Entity;
import efruchter.tp.traits.Gene;
import efruchter.tp.traits.Trait;

/**
 * A slider-based editor for the traits of a single entity.
 * 
 * @author toriscope
 * 
 */
@SuppressWarnings("serial")
public class TraitViewer extends JPanel {
	
	private int detail = 1000;
	
	public TraitViewer(final Entity entity) {
		
		setBorder(BorderFactory.createTitledBorder(entity.name));
		
		for (Trait trait : entity.getTraits()) {
			JPanel center = new JPanel();
			center.setToolTipText(trait.getInfo());
			center.setBorder(BorderFactory.createTitledBorder(trait.getName()));
			
			for (final Gene gene : trait.getGenes()) {
				JPanel p = new JPanel();
				p.setBorder(BorderFactory.createTitledBorder(gene.getName()));
				
				final JSlider c = new JSlider(JSlider.VERTICAL, 0, detail, (int) (gene.getExpression() * detail));
				c.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						gene.setExpression((float) c.getValue() / detail);
					}
				});
				c.setToolTipText(gene.getInfo());
				p.add(c);
				center.add(p);
			}
			
			add(center);
		}
		
		JFrame frame = new JFrame("Trait Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		frame.setVisible(true);
	}
}
