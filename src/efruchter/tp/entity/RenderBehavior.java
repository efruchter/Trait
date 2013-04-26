package efruchter.tp.entity;

import java.awt.Graphics2D;

/**
 * A standard rendering behavior for a 2D entity.
 * 
 * @author toriscope
 * 
 */
public interface RenderBehavior {

    /**
     * The entity should display itself on the context. Please be sure to pop
     * any changes to the context.
     * 
     * @param entity
     *            the entity to draw
     * @param g
     *            the context
     */
    public void render(Entity entity, Graphics2D g);
}
