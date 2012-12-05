package efruchter.tp.gui.panels.level;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.learning.GeneVector.GeneWrapper;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.behavior.Behavior;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.gene.Gene;

@SuppressWarnings("serial")
public class GeneralEditor extends JPanel implements ChangeListener, ActionListener {
	
	private Object editingEntity = null, parent = null;
	private final JLabel name, info, value;
	private final JButton removeButton;
	private final JSlider slider;
	private final JCheckBox checkBox;
	private int detail = 1000;
	private JTree tree;
	
	public void refreshView() {
		name.setText("Nothing Selected");
		info.setText("");
		value.setText("");
		slider.setVisible(false);
		removeButton.setVisible(false);
		checkBox.setVisible(false);
		
		if (editingEntity instanceof Gene) {
			name.setText(((Gene) editingEntity).getName());
			info.setText(((Gene) editingEntity).getInfo());
			value.setText("Value: " + ((Gene) editingEntity).getValue());
			slider.setVisible(true);
			slider.setValue((int) (((Gene) editingEntity).getExpression() * detail));
		}
		if (editingEntity instanceof Gene) {
			name.setText(((Gene) editingEntity).getName());
			info.setText(((Gene) editingEntity).getInfo());
			value.setText("Value: " + ((Gene) editingEntity).getValue());
			slider.setVisible(true);
			slider.setValue((int) (((Gene) editingEntity).getExpression() * detail));
		} else if (editingEntity instanceof Entity) {
			name.setText("Entity Selected:");
			info.setText(((Entity) editingEntity).name);
			removeButton.setVisible(true);
		} else if (editingEntity instanceof Trait) {
			name.setText(((Trait) editingEntity).getName());
			info.setText(((Trait) editingEntity).getInfo());
			if (((Trait) editingEntity).getGenes().isEmpty()) {
				value.setText("No Genes for this Trait");
			}
			removeButton.setVisible(true);
			checkBox.setVisible(true);
			checkBox.setSelected(((Trait) editingEntity).isActive());
		} else if (editingEntity instanceof Behavior) {
			name.setText("Behavior (No info available)");
			info.setText("");
			value.setText("");
			removeButton.setVisible(true);
		}
	}
	
	public void setEditing(TreePath editme) {
		if (editme != null) {
			editingEntity = editme.getLastPathComponent();
			if (editingEntity instanceof GeneWrapper)
				editingEntity = ((GeneWrapper) editingEntity).gene;
			try {
				parent = editme.getParentPath().getLastPathComponent();
			} catch (NullPointerException e) {
				parent = null;
			}
		}
		refreshView();
	}
	
	public GeneralEditor(final JTree tree) {
		this.tree = tree;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder("Inspector"));
		add(name = new JLabel());
		add(info = new JLabel());
		add(value = new JLabel());
		add(slider = new JSlider(JSlider.HORIZONTAL, 0, detail, 0));
		add(checkBox = new JCheckBox("Active"));
		add(removeButton = new JButton("Delete"));
		removeButton.addActionListener(this);
		slider.addChangeListener(this);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				((Trait) editingEntity).setActive(checkBox.isSelected() ? true : false);
				tree.repaint();
			}
		});
		refreshView();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (editingEntity instanceof Gene) {
			((Gene) editingEntity).setExpression((float) slider.getValue() / detail);
			value.setText("Value: " + ((Gene) editingEntity).getValue());
			tree.repaint();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (editingEntity instanceof Entity) {
			((Level) tree.getModel().getRoot()).removeEntity((Entity) editingEntity);
		} else if (editingEntity instanceof Trait && parent instanceof Entity) {
			((Entity) parent).removeTrait((Trait) editingEntity);
		} else if (editingEntity instanceof Behavior && parent instanceof BehaviorChain) {
			((BehaviorChain) parent).removeBehavior((Behavior) editingEntity);
			
		}
		tree.setModel(new LevelTreeModel(((Level) tree.getModel().getRoot())));
	}
}
