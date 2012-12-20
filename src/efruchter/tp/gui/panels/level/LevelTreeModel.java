package efruchter.tp.gui.panels.level;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import efruchter.tp.defaults.EntityType;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.Trait;
import efruchter.tp.trait.behavior.BehaviorChain;
import efruchter.tp.trait.gene.Gene;

public class LevelTreeModel implements TreeModel {
	
	final private Level level;
	
	public LevelTreeModel(final Level level) {
		this.level = level;
	}
	
	@Override
	public void addTreeModelListener(final TreeModelListener l) {
		
	}
	
	@Override
	public Object getChild(final Object parent, final int index) {
		if (parent instanceof BehaviorChain) {
			return ((BehaviorChain) parent).getBehaviors().get(index);
		} else if (parent instanceof Level) {
			return ((Level) parent).getEntities(EntityType.SHIP).get(index);
		} else if (parent instanceof Entity) {
			return ((Entity) parent).getTraits().get(index);
		} else if (parent instanceof Trait) {
			return ((Trait) parent).getGenes().get(index);
		}
		return null;
	}
	
	@Override
	public int getChildCount(final Object parent) {
		if (parent instanceof BehaviorChain) {
			return ((BehaviorChain) parent).getBehaviors().size();
		} else if (parent instanceof Level) {
			return ((Level) parent).getEntities(EntityType.SHIP).size();
		} else if (parent instanceof Entity) {
			return ((Entity) parent).getTraits().size();
		} else if (parent instanceof Trait) {
			return ((Trait) parent).getGenes().size();
		}
		
		return 0;
	}
	
	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		if (parent instanceof BehaviorChain) {
			return ((BehaviorChain) parent).getBehaviors().indexOf(child);
		} else if (parent instanceof Level) {
			return ((Level) parent).getEntities(EntityType.SHIP).indexOf(child);
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
	public boolean isLeaf(final Object node) {
		return node instanceof Gene || (node instanceof Trait && getChildCount(node) == 0);
	}
	
	@Override
	public void removeTreeModelListener(final TreeModelListener l) {
		
	}
	
	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
		
	}
}