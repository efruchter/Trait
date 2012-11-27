package efruchter.tp.gui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import efruchter.tp.entity.Entity;
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
	private JFrame frame;
	private Entity entity;
	
	public TraitViewer(final Entity entity) {
		this.entity = entity;
		
		frame = new JFrame("Trait Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildGUI();
		frame.add(new JScrollPane(this));
		frame.pack();
		frame.setVisible(true);
	}
	
	public TraitViewer() {
		this(new Entity());
	}
	
	public void setEntity(Entity e) {
		this.entity = e;
		buildGUI();
	}
	
	private void buildGUI() {
		setBorder(BorderFactory.createTitledBorder(entity.name));
		this.removeAll();
		for (Trait trait : entity.getTraits()) {
			JPanel center = new JPanel();
			center.setToolTipText(trait.getInfo());
			center.setBorder(BorderFactory.createTitledBorder(trait.getName()));
			
			if (trait.getGenes().isEmpty()) {
				//Trait with no genes
				JPanel p = new JPanel();
				p.setBorder(BorderFactory.createTitledBorder("No Gene"));
				final JSlider c = new JSlider(JSlider.VERTICAL, 0, detail, detail);
				c.setEnabled(false);
				c.setToolTipText("This trait is always maximally expressed.");
				p.add(c);
				center.add(p);
			} else {
				// Normal Trait w/ genes
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
			}
			add(center);
		}
		frame.pack();
	}
}
