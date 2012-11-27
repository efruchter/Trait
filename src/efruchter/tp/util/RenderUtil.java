package efruchter.tp.util;

import org.lwjgl.opengl.GL11;

import efruchter.tp.entity.Behavior;
import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;

public class RenderUtil {
	
	private RenderUtil() {
	}
	
	/*
	 * Pre-compute sin/cos tables for circle.
	 */
	private final static int FAST_SUB = 15;
	private static float[] COS_FAST = new float[FAST_SUB + 1], SIN_FAST = new float[FAST_SUB + 1];
	static {
		for (int i = 0; i <= FAST_SUB; i++) {
			double angle = Math.PI * 2 * i / FAST_SUB;
			COS_FAST[i] = (float) Math.cos(angle);
			SIN_FAST[i] = (float) Math.sin(angle);
		}
	}
	
	/**
	 * Draw a circle with subdivisions pre-set. Pretty much a circle. No trig
	 * involved.
	 * 
	 * @param radius
	 *            radius of circle.
	 */
	public static void drawCircleFast(float radius) {
		GL11.glPushMatrix();
		{
			GL11.glScalef(radius, radius, 1);
			
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			{
				GL11.glVertex2f(0, 0);
				for (int i = 0; i <= FAST_SUB; i++) {
					GL11.glVertex2f(COS_FAST[i], SIN_FAST[i]);
				}
				
			}
			GL11.glEnd();
			
		}
		GL11.glPopMatrix();
	}
	
	public static void drawCircle(float radius, float subdivisions) {
		GL11.glPushMatrix();
		{
			GL11.glScalef(radius, radius, 1);
			
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			{
				GL11.glVertex2f(0, 0);
				for (int i = 0; i <= subdivisions; i++) {
					double angle = Math.PI * 2 * i / subdivisions;
					GL11.glVertex2f((float) Math.cos(angle), (float) Math.sin(angle));
				}
			}
			GL11.glEnd();
			
		}
		GL11.glPopMatrix();
	}
	
	public final static Behavior GENERIC_RENDER = new Behavior() {
		
		@Override
		public void onStart(Entity self, Level l) {
			
		}
		
		@Override
		public void onUpdate(Entity self, Level l, long delta) {
			if (self.isHurtAnimFrame())
				return;
			GL11.glPushMatrix();
			{
				GL11.glColor3f(self.baseColor.getRed(), self.baseColor.getGreen(), self.baseColor.getBlue());
				GL11.glTranslatef(self.x, self.y, 0);
				RenderUtil.drawCircleFast(self.radius);
			}
			GL11.glPopMatrix();
		}
		
		@Override
		public void onDeath(Entity self, Level l) {
			
		}
		
	};
}
