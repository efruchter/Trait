package efruchter.tp.gui;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.behavior.BehaviorChain;

public class LevelTreeModel implements TreeModel {
	
	final private Level level;
	
	public LevelTreeModel(final Level level) {
		this.level = level;
	}
	
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		
	}
	
	@Override
	public Object getChild(Object parent, int index) {
		if (parent instanceof BehaviorChain) {
			return ((BehaviorChain) parent).getBehaviors().get(index);
		} else if (parent instanceof Level) {
			return ((Level) parent).getShips().get(index);
		} else if (parent instanceof Entity) {
			return ((Entity) parent).getTraits().get(index);
		} else if (parent instanceof Trait) {
			return ((Trait) parent).getGenes().get(index);
		}
		return null;
	}
	
	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof BehaviorChain) {
			return ((BehaviorChain) parent).getBehaviors().size();
		} else if (parent instanceof Level) {
			return ((Level) parent).getShips().size();
		} else if (parent instanceof Entity) {
			return ((Entity) parent).getTraits().size();
		} else if (parent instanceof Trait) {
			return ((Trait) parent).getGenes().size();
		}
		
		return 0;
	}
	
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof BehaviorChain) {
			return ((BehaviorChain) parent).getBehaviors().indexOf(child);
		} else if (parent instanceof Level) {
			return ((Level) parent).getShips().indexOf(child);
		} else if (parent instanceof Entity) {
			return ((Entity) parent).getTraits().indexOf(child);
		} else if (parent instanceof Trait) {
			return ((Trait) parent).getGenes().indexOf(child);
		}
		return -1;
	}
	
	@Override
	public Object getRoot() {
		return level;
	}
	
	@Override
	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}
	
	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		
	}
	
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		
	}
}