package efruchter.tp.gui.panels.generegistery;

import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import efruchter.tp.learning.GeneVector;
import efruchter.tp.learning.GeneVector.GeneWrapper;

public class ExplorationVectorTreeModel implements TreeModel {
	
	final private List<GeneWrapper> register;
	final private String root = "Current Gene Vector";
	
	public ExplorationVectorTreeModel(final GeneVector vector) {
		this.register = vector.getGenes();
	}
	
	@Override
	public void addTreeModelListener(final TreeModelListener l) {
		
	}
	
	@Override
	public Object getChild(final Object parent, final int index) {
		if (parent == root) {
			return register.get(index);
		}
		
		return null;
	}
	
	@Override
	public int getChildCount(final Object parent) {
		
		if (parent == root) {
			return register.size();
		}
		
		return 0;
	}
	
	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
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
	public boolean isLeaf(final Object node) {
		return node instanceof GeneWrapper;
	}
	
	@Override
	public void removeTreeModelListener(final TreeModelListener l) {
		
	}
	
	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		
	}
}