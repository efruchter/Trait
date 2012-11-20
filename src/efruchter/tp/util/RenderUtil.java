package efruchter.tp.util;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import efruchter.tp.entities.Behavior;
import efruchter.tp.entities.Entity;
import efruchter.tp.entities.Level;

public class RenderUtil {

	private RenderUtil() {
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
					GL11.glVertex2f((float) Math.cos(angle),
							(float) Math.sin(angle));
				}
			}
			GL11.glEnd();

		}
		GL11.glPopMatrix();
	}

	public static Behavior getShipRenderer(final Color color) {
		return new Behavior() {

			@Override
			public void onStart(Entity self, Level l) {

			}

			@Override
			public void onUpdate(Entity self, Level l, long delta) {
				GL11.glPushMatrix();
				{
					GL11.glColor3f(color.getRed(), color.getGreen(),
							color.getBlue());
					GL11.glTranslatef(self.x, self.y, 0);
					RenderUtil.drawCircle(self.radius, 10);
				}
				GL11.glPopMatrix();
			}

			@Override
			public void onDeath(Entity self, Level l) {

			}

		};
	}
}
