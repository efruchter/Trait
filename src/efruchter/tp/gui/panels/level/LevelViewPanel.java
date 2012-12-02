package efruchter.tp.gui.panels.level;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import efruchter.tp.entity.Level;

/**
 * A slider-based editor for the traits of a single entity.
 * 
 * @author toriscope
 * 
 */
@SuppressWarnings("serial")
public class LevelViewPanel extends JPanel implements TreeSelectionListener {
	
	private JTree tree;
	private GeneralEditor genEditor;
	
	public LevelViewPanel(final Level level) {
		
		String info = "(E) Entity  (T) Trait  (G) Gene  (B) Behavior  (C) Chain";
		setLayout(new BorderLayout());
		//Make tree
		tree = new JTree();
		tree.setExpandsSelectedPaths(true);
		tree.setEditable(false);
		tree.setModel(new LevelTreeModel(level));
		
		//Add to panel
		JScrollPane pane = new JScrollPane(tree);
		pane.setBorder(BorderFactory.createTitledBorder("Hierarchy"));
		pane.setPreferredSize(new Dimension(500, 500));
		add(pane, BorderLayout.CENTER);
		
		//General editor
		add(genEditor = new GeneralEditor(tree), BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panel.add(new JLabel(info));
		panel.setBorder(BorderFactory.createTitledBorder("Key:"));
		add(panel, BorderLayout.NORTH);
		
		tree.addTreeSelectionListener(this);
	}
	
	public void setLevel(Level level) {
		tree.setModel(new LevelTreeModel(level));
		genEditor.setEditing(null);
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		if (tree.getSelectionCount() > 0) {
			genEditor.setEditing(tree.getSelectionPath());
		}
		
	}
}
