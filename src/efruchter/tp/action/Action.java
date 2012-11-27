package efruchter.tp.action;

import efruchter.tp.entity.Behavior;

/**
 * A type of Behavior with a time component. Designed as an intermediate for an
 * ActionChain.
 * 
 * @author toriscope
 * 
 */
public interface Action extends Behavior {
	long timeSpan();
}
