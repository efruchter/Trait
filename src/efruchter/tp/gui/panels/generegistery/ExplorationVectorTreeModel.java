package efruchter.tp.gui.panels.generegistery;

import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import efruchter.tp.entity.Level;
import efruchter.tp.learning.GeneVector.GeneWrapper;

public class ExplorationVectorTreeModel implements TreeModel {
	
	final private List<GeneWrapper> register;
	final private String root = "Exploration Gene Vector";
	
	public ExplorationVectorTreeModel(final Level level) {
		this.register = level.getExplorationVector().getGenes();
		System.out.println("dsdsds");
		
	}
	
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		
	}
	
	@Override
	public Object getChild(Object parent, int index) {
		if (parent == root) {
			return register.get(index);
		}
		
		return null;
	}
	
	@Override
	public int getChildCount(Object parent) {
		
		if (parent == root) {
			return register.size();
		}
		
		return 0;
	}
	
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == root && child instanceof GeneWrapper) {
			return register.indexOf(child);
		}
		
		return -1;
	}
	
	@Override
	public Object getRoot() {
		return root;
	}
	
	@Override
	public boolean isLeaf(Object node) {
		return node instanceof GeneWrapper;
	}
	
	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		
	}
	
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		
	}
}