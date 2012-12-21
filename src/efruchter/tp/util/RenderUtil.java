package efruchter.tp.util;

import org.lwjgl.opengl.GL11;

import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.trait.behavior.Behavior;

public class RenderUtil {

	private RenderUtil() {
	}

	/*
	 * Pre-compute sin/cos tables for circle.
	 */
	private final static int FAST_SUB = 100;

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
     * @param subs
     *            amount of subs < 100
	 */
	public static void drawCircleFast(final float radius, final int subs) {

        final int jump = FAST_SUB / subs;
        final int overflow = FAST_SUB % subs;

        GL11.glPushMatrix();
        {
            GL11.glScalef(radius, radius, 1);

			GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			{
				GL11.glVertex2f(0, 0);
				for (int i = 0; i <= FAST_SUB + overflow; i+= jump) {
					GL11.glVertex2f(COS_FAST[i % FAST_SUB], SIN_FAST[i % FAST_SUB]);
				}

			}
			GL11.glEnd();

		}
		GL11.glPopMatrix();
	}

	public static void drawCircle(final float radius, final float subdivisions) {
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

	public final static Behavior SHIP_RENDER = new Behavior() {

		@Override
		public void onStart(final Entity self, final Level l) {

		}

		@Override
		public void onUpdate(final Entity self, final Level l, final long delta) {
			if (self.isHurtAnimFrame())
				return;
			GL11.glPushMatrix();
			{
				GL11.glColor3f(self.baseColor.getRed() / 255f, self.baseColor.getGreen() / 255f,
						self.baseColor.getBlue() / 255f);
				GL11.glTranslatef(self.x, self.y, 0);
				RenderUtil.drawCircleFast(self.radius, 10);
			}
			GL11.glPopMatrix();
		}

		@Override
		public void onDeath(final Entity self, final Level l) {

		}

	};

    public final static Behavior STAR_RENDER = new Behavior() {

        @Override
        public void onStart(final Entity self, final Level l) {

        }

        float rotate= 0;

        @Override
        public void onUpdate(final Entity self, final Level l, final long delta) {
            GL11.glPushMatrix();
            {
                rotate += Math.random();
                GL11.glColor3f(self.baseColor.getRed() / 255f, self.baseColor.getGreen() / 255f,
                        self.baseColor.getBlue() / 255f);
                GL11.glTranslatef(self.x, self.y, 0);
                GL11.glRotatef(rotate, 0, 0, 1);
                RenderUtil.drawCircleFast(self.radius, 3);
            }
            GL11.glPopMatrix();
        }

        @Override
        public void onDeath(final Entity self, final Level l) {

        }

    };

    public final static Behavior PROJECTILE_RENDER = new Behavior() {

        float rotate= 0;

        @Override
        public void onStart(final Entity self, final Level l) {

        }

        @Override
        public void onUpdate(final Entity self, final Level l, final long delta) {
            rotate += .1f;
            GL11.glPushMatrix();
            {
                GL11.glColor3f(self.baseColor.getRed() / 255f, self.baseColor.getGreen() / 255f,
                        self.baseColor.getBlue() / 255f);
                GL11.glTranslatef(self.x, self.y, 0);
                GL11.glRotatef(rotate, 0, 0, 1);
                RenderUtil.drawCircleFast(self.radius, 4);
            }
            GL11.glPopMatrix();
        }

        @Override
        public void onDeath(final Entity self, final Level l) {

        }

    };

}
