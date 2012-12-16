package efruchter.tp.gui.panels.generegistery;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import efruchter.tp.entity.Level;
import efruchter.tp.gui.panels.level.GeneralEditor;

/**
 * INCOMING
 * 
 * @author toriscope
 * 
 */
@SuppressWarnings("serial")
public class VectorViewPanel extends JPanel implements TreeSelectionListener {
	
	private JTree tree;
	private GeneralEditor genEditor;
	
	public VectorViewPanel(final Level level) {
		
		setLayout(new BorderLayout());
		//Make tree
		tree = new JTree();
		tree.setExpandsSelectedPaths(true);
		tree.setEditable(false);
		tree.setModel(new ExplorationVectorTreeModel(level));
		
		//Add to panel
		JScrollPane pane = new JScrollPane(tree);
		pane.setBorder(BorderFactory.createTitledBorder("Genes"));
		pane.setPreferredSize(new Dimension(500, 500));
		add(pane, BorderLayout.CENTER);
		
		//General editor
		add(genEditor = new GeneralEditor(tree), BorderLayout.SOUTH);
		
		tree.addTreeSelectionListener(this);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	
	public void setLevel(Level level) {
		tree.setModel(new ExplorationVectorTreeModel(level));
		genEditor.setEditing(null);
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		if (tree.getSelectionCount() > 0) {
			genEditor.setEditing(tree.getSelectionPath());
		}
		
	}
}