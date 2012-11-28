package efruchter.tp.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
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
public class LevelViewer implements TreeSelectionListener {
	
	private JFrame frame;
	private JTree tree;
	private GeneralEditor genEditor;
	
	public LevelViewer(final Level level) {
		
		String info = "(E) Entity  (T) Trait  (G) Gene  (B) Behavior  (C) Chain";
		
		frame = new JFrame("Level Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Make tree
		tree = new JTree();
		tree.setExpandsSelectedPaths(true);
		tree.setEditable(false);
		tree.setModel(new LevelTreeModel(level));
		
		//Add to panel
		JScrollPane pane = new JScrollPane(tree);
		pane.setBorder(BorderFactory.createTitledBorder("Hierarchy"));
		pane.setPreferredSize(new Dimension(500, 500));
		frame.add(pane, BorderLayout.CENTER);
		
		//General editor
		frame.add(genEditor = new GeneralEditor(tree), BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panel.add(new JLabel(info));
		panel.setBorder(BorderFactory.createTitledBorder("Key:"));
		frame.add(panel, BorderLayout.NORTH);
		
		frame.pack();
		frame.setVisible(true);
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
