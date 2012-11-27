package efruchter.tp.gui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import efruchter.tp.entity.Entity;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.gene.Gene;

@SuppressWarnings("serial")
public class GeneralEditor extends JPanel implements ChangeListener {
	
	private Object editingEntity = null;
	private final JLabel name, info, value;
	private final JSlider slider;
	private int detail = 1000;
	
	public void refreshView() {
		
		name.setText("Nothing Selected");
		info.setText("");
		value.setText("");
		slider.setVisible(false);
		
		if (editingEntity instanceof Gene) {
			name.setText(((Gene) editingEntity).getName());
			info.setText(((Gene) editingEntity).getInfo());
			value.setText("Value: " + ((Gene) editingEntity).getValue());
			slider.setVisible(true);
			slider.setValue((int) (((Gene) editingEntity).getExpression() * detail));
		} else if (editingEntity instanceof Entity) {
			name.setText("Entity Selected:");
			info.setText(((Entity) editingEntity).name);
		} else if (editingEntity instanceof Trait) {
			name.setText(((Trait) editingEntity).getName());
			info.setText(((Trait) editingEntity).getInfo());
			if (((Trait) editingEntity).getGenes().isEmpty()) {
				value.setText("No Genes for this Trait");
			}
		} else if (editingEntity instanceof Behavior) {
			name.setText("Behavior (No info available)");
			info.setText("");
			value.setText("");
		}
	}
	
	public void setEditing(Object editme) {
		this.editingEntity = editme;
		refreshView();
		System.out.println(editme.toString());
	}
	
	public GeneralEditor() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder("Inspector"));
		add(name = new JLabel());
		add(info = new JLabel());
		add(value = new JLabel());
		add(slider = new JSlider(JSlider.HORIZONTAL, 0, detail, 0));
		slider.addChangeListener(this);
		refreshView();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (editingEntity instanceof Gene) {
			((Gene) editingEntity).setExpression((float) slider.getValue() / detail);
			value.setText("Value: " + ((Gene) editingEntity).getValue());
		}
	}
}
