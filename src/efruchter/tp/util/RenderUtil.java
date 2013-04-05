package efruchter.tp.util;

import org.lwjgl.opengl.GL11;


import efruchter.tp.entity.Entity;
import efruchter.tp.entity.Level;
import efruchter.tp.entity.PolarityController;
import efruchter.tp.trait.behavior.Behavior;

import java.awt.*;

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
                for (int i = 0; i <= FAST_SUB + overflow; i += jump) {
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
                if (self.polarity != -1) {
                    setColor(PolarityController.COLORS[self.polarity]);
                } else {
                    setColor(self.baseColor);
                }
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

        float rotate = 0;

        @Override
        public void onUpdate(final Entity self, final Level l, final long delta) {
            GL11.glPushMatrix();
            {
                rotate += Math.random();
                setColor(self.baseColor);
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

        float rotate = 0;

        @Override
        public void onStart(final Entity self, final Level l) {

        }

        @Override
        public void onUpdate(final Entity self, final Level l, final long delta) {
            rotate += .1f;
            GL11.glPushMatrix();
            {
                if (self.polarity != -1) {
                    setColor(PolarityController.COLORS[self.polarity]);
                } else {
                    setColor(self.baseColor);
                }
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

    public static void setColor(final Color color) {
        GL11.glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }

    public static void drawString(final String string, final float fontSize) {
        GL11.glPushMatrix();
        {
            GL11.glScalef(fontSize, fontSize, 0);
            GL11.glPointSize(fontSize);
            RenderUtil.drawString(string, 0, 0);
            GL11.glPointSize(1);
        }
        GL11.glPopMatrix();
    }

    /**
     * Renders a string in all caps. Poor sub for billboarded font. Only renders
     * [a-z0-9].
     * 
     * @param s
     * @param x
     * @param y
     */
    public static void drawString(String s, int x, int y) {

        /*
         * This code is hilarious!
         */

        int startX = x;
        GL11.glBegin(GL11.GL_POINTS);
        for (char c : s.toLowerCase().toCharArray())
            switch (c) {
		        case '-': {
		            for (int i = 2; i <= 6; i++) {
		                GL11.glVertex2f(x + i, y + 4);
		            }
		            x += 8;
		            break;
		        }
                case 'a': {
                    for (int i = 0; i < 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    x += 8;
                    break;
                }
                case 'b': {
                    for (int i = 0; i < 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 1; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y);
                        GL11.glVertex2f(x + i, y + 4);
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    GL11.glVertex2f(x + 7, y + 5);
                    GL11.glVertex2f(x + 7, y + 7);
                    GL11.glVertex2f(x + 7, y + 6);
                    GL11.glVertex2f(x + 7, y + 1);
                    GL11.glVertex2f(x + 7, y + 2);
                    GL11.glVertex2f(x + 7, y + 3);
                    x += 8;
                    break;
                }
                case 'c': {
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y);
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    GL11.glVertex2f(x + 6, y + 1);
                    GL11.glVertex2f(x + 6, y + 2);

                    GL11.glVertex2f(x + 6, y + 6);
                    GL11.glVertex2f(x + 6, y + 7);

                    x += 8;
                    break;
                }
                case 'd': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y);
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    GL11.glVertex2f(x + 6, y + 1);
                    GL11.glVertex2f(x + 6, y + 2);
                    GL11.glVertex2f(x + 6, y + 3);
                    GL11.glVertex2f(x + 6, y + 4);
                    GL11.glVertex2f(x + 6, y + 5);
                    GL11.glVertex2f(x + 6, y + 6);
                    GL11.glVertex2f(x + 6, y + 7);

                    x += 8;
                    break;
                }
                case 'e': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 1; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 0);
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    x += 8;
                    break;
                }
                case 'f': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 1; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    x += 8;
                    break;
                }
                case 'g': {
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y);
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    GL11.glVertex2f(x + 6, y + 1);
                    GL11.glVertex2f(x + 6, y + 2);
                    GL11.glVertex2f(x + 6, y + 3);
                    GL11.glVertex2f(x + 5, y + 3);
                    GL11.glVertex2f(x + 7, y + 3);

                    GL11.glVertex2f(x + 6, y + 6);
                    GL11.glVertex2f(x + 6, y + 7);

                    x += 8;
                    break;
                }
                case 'h': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    x += 8;
                    break;
                }
                case 'i': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 3, y + i);
                    }
                    for (int i = 1; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 0);
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    x += 7;
                    break;
                }
                case 'j': {
                    for (int i = 1; i <= 8; i++) {
                        GL11.glVertex2f(x + 6, y + i);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 0);
                    }
                    GL11.glVertex2f(x + 1, y + 3);
                    GL11.glVertex2f(x + 1, y + 2);
                    GL11.glVertex2f(x + 1, y + 1);
                    x += 8;
                    break;
                }
                case 'k': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    GL11.glVertex2f(x + 6, y + 8);
                    GL11.glVertex2f(x + 5, y + 7);
                    GL11.glVertex2f(x + 4, y + 6);
                    GL11.glVertex2f(x + 3, y + 5);
                    GL11.glVertex2f(x + 2, y + 4);
                    GL11.glVertex2f(x + 2, y + 3);
                    GL11.glVertex2f(x + 3, y + 4);
                    GL11.glVertex2f(x + 4, y + 3);
                    GL11.glVertex2f(x + 5, y + 2);
                    GL11.glVertex2f(x + 6, y + 1);
                    GL11.glVertex2f(x + 7, y);
                    x += 8;
                    break;
                }
                case 'l': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 1; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y);
                    }
                    x += 7;
                    break;
                }
                case 'm': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    GL11.glVertex2f(x + 3, y + 6);
                    GL11.glVertex2f(x + 2, y + 7);
                    GL11.glVertex2f(x + 4, y + 5);

                    GL11.glVertex2f(x + 5, y + 6);
                    GL11.glVertex2f(x + 6, y + 7);
                    GL11.glVertex2f(x + 4, y + 5);
                    x += 8;
                    break;
                }
                case 'n': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    GL11.glVertex2f(x + 2, y + 7);
                    GL11.glVertex2f(x + 2, y + 6);
                    GL11.glVertex2f(x + 3, y + 5);
                    GL11.glVertex2f(x + 4, y + 4);
                    GL11.glVertex2f(x + 5, y + 3);
                    GL11.glVertex2f(x + 6, y + 2);
                    GL11.glVertex2f(x + 6, y + 1);
                    x += 8;
                    break;
                }
                case 'o':
                case '0': {
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                        GL11.glVertex2f(x + i, y + 0);
                    }
                    x += 8;
                    break;
                }
                case 'p': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    GL11.glVertex2f(x + 6, y + 7);
                    GL11.glVertex2f(x + 6, y + 5);
                    GL11.glVertex2f(x + 6, y + 6);
                    x += 8;
                    break;
                }
                case 'q': {
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        if (i != 1)
                            GL11.glVertex2f(x + 7, y + i);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                        if (i != 6)
                            GL11.glVertex2f(x + i, y + 0);
                    }
                    GL11.glVertex2f(x + 4, y + 3);
                    GL11.glVertex2f(x + 5, y + 2);
                    GL11.glVertex2f(x + 6, y + 1);
                    GL11.glVertex2f(x + 7, y);
                    x += 8;
                    break;
                }
                case 'r': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    GL11.glVertex2f(x + 6, y + 7);
                    GL11.glVertex2f(x + 6, y + 5);
                    GL11.glVertex2f(x + 6, y + 6);

                    GL11.glVertex2f(x + 4, y + 3);
                    GL11.glVertex2f(x + 5, y + 2);
                    GL11.glVertex2f(x + 6, y + 1);
                    GL11.glVertex2f(x + 7, y);
                    x += 8;
                    break;
                }
                case 's': {
                    for (int i = 2; i <= 7; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    GL11.glVertex2f(x + 1, y + 7);
                    GL11.glVertex2f(x + 1, y + 6);
                    GL11.glVertex2f(x + 1, y + 5);
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 4);
                        GL11.glVertex2f(x + i, y);
                    }
                    GL11.glVertex2f(x + 7, y + 3);
                    GL11.glVertex2f(x + 7, y + 2);
                    GL11.glVertex2f(x + 7, y + 1);
                    GL11.glVertex2f(x + 1, y + 1);
                    GL11.glVertex2f(x + 1, y + 2);
                    x += 8;
                    break;
                }
                case 't': {
                    for (int i = 0; i <= 8; i++) {
                        GL11.glVertex2f(x + 4, y + i);
                    }
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    x += 7;
                    break;
                }
                case 'u': {
                    for (int i = 1; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 0);
                    }
                    x += 8;
                    break;
                }
                case 'v': {
                    for (int i = 2; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 6, y + i);
                    }
                    GL11.glVertex2f(x + 2, y + 1);
                    GL11.glVertex2f(x + 5, y + 1);
                    GL11.glVertex2f(x + 3, y);
                    GL11.glVertex2f(x + 4, y);
                    x += 7;
                    break;
                }
                case 'w': {
                    for (int i = 1; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    GL11.glVertex2f(x + 2, y);
                    GL11.glVertex2f(x + 3, y);
                    GL11.glVertex2f(x + 5, y);
                    GL11.glVertex2f(x + 6, y);
                    for (int i = 1; i <= 6; i++) {
                        GL11.glVertex2f(x + 4, y + i);
                    }
                    x += 8;
                    break;
                }
                case 'x': {
                    for (int i = 1; i <= 7; i++)
                        GL11.glVertex2f(x + i, y + i);
                    for (int i = 7; i >= 1; i--)
                        GL11.glVertex2f(x + i, y + 8 - i);
                    x += 8;
                    break;
                }
                case 'y': {
                    GL11.glVertex2f(x + 4, y);
                    GL11.glVertex2f(x + 4, y + 1);
                    GL11.glVertex2f(x + 4, y + 2);
                    GL11.glVertex2f(x + 4, y + 3);
                    GL11.glVertex2f(x + 4, y + 4);

                    GL11.glVertex2f(x + 3, y + 5);
                    GL11.glVertex2f(x + 2, y + 6);
                    GL11.glVertex2f(x + 1, y + 7);
                    GL11.glVertex2f(x + 1, y + 8);

                    GL11.glVertex2f(x + 5, y + 5);
                    GL11.glVertex2f(x + 6, y + 6);
                    GL11.glVertex2f(x + 7, y + 7);
                    GL11.glVertex2f(x + 7, y + 8);
                    x += 8;
                    break;
                }
                case 'z': {
                    for (int i = 1; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y);
                        GL11.glVertex2f(x + i, y + 8);
                        GL11.glVertex2f(x + i, y + i);
                    }
                    GL11.glVertex2f(x + 6, y + 7);
                    x += 8;
                    break;
                }
                case '1': {
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y);
                    }
                    for (int i = 1; i <= 8; i++) {
                        GL11.glVertex2f(x + 4, y + i);
                    }
                    GL11.glVertex2f(x + 3, y + 7);
                    x += 8;
                    break;
                }
                case '2': {
                    for (int i = 1; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    GL11.glVertex2f(x + 1, y + 7);
                    GL11.glVertex2f(x + 1, y + 6);

                    GL11.glVertex2f(x + 6, y + 7);
                    GL11.glVertex2f(x + 6, y + 6);
                    GL11.glVertex2f(x + 6, y + 5);
                    GL11.glVertex2f(x + 5, y + 4);
                    GL11.glVertex2f(x + 4, y + 3);
                    GL11.glVertex2f(x + 3, y + 2);
                    GL11.glVertex2f(x + 2, y + 1);
                    x += 8;
                    break;
                }
                case '3': {
                    for (int i = 1; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                        GL11.glVertex2f(x + i, y);
                    }
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + 6, y + i);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    x += 8;
                    break;
                }
                case '4': {
                    for (int i = 2; i <= 8; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 2; i <= 7; i++) {
                        GL11.glVertex2f(x + i, y + 1);
                    }
                    for (int i = 0; i <= 4; i++) {
                        GL11.glVertex2f(x + 4, y + i);
                    }
                    x += 8;
                    break;
                }
                case '5': {
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    for (int i = 4; i <= 7; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    GL11.glVertex2f(x + 1, y + 1);
                    GL11.glVertex2f(x + 2, y);
                    GL11.glVertex2f(x + 3, y);
                    GL11.glVertex2f(x + 4, y);
                    GL11.glVertex2f(x + 5, y);
                    GL11.glVertex2f(x + 6, y);

                    GL11.glVertex2f(x + 7, y + 1);
                    GL11.glVertex2f(x + 7, y + 2);
                    GL11.glVertex2f(x + 7, y + 3);

                    GL11.glVertex2f(x + 6, y + 4);
                    GL11.glVertex2f(x + 5, y + 4);
                    GL11.glVertex2f(x + 4, y + 4);
                    GL11.glVertex2f(x + 3, y + 4);
                    GL11.glVertex2f(x + 2, y + 4);
                    x += 8;
                    break;
                }
                case '6': {
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y);
                    }
                    for (int i = 2; i <= 5; i++) {
                        GL11.glVertex2f(x + i, y + 4);
                        GL11.glVertex2f(x + i, y + 8);
                    }
                    GL11.glVertex2f(x + 7, y + 1);
                    GL11.glVertex2f(x + 7, y + 2);
                    GL11.glVertex2f(x + 7, y + 3);
                    GL11.glVertex2f(x + 6, y + 4);
                    x += 8;
                    break;
                }
                case '7': {
                    for (int i = 0; i <= 7; i++)
                        GL11.glVertex2f(x + i, y + 8);
                    GL11.glVertex2f(x + 7, y + 7);
                    GL11.glVertex2f(x + 7, y + 6);

                    GL11.glVertex2f(x + 6, y + 5);
                    GL11.glVertex2f(x + 5, y + 4);
                    GL11.glVertex2f(x + 4, y + 3);
                    GL11.glVertex2f(x + 3, y + 2);
                    GL11.glVertex2f(x + 2, y + 1);
                    GL11.glVertex2f(x + 1, y);
                    x += 8;
                    break;
                }
                case '8': {
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                        GL11.glVertex2f(x + i, y + 0);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    x += 8;
                    break;
                }
                case '9': {
                    for (int i = 1; i <= 7; i++) {
                        GL11.glVertex2f(x + 7, y + i);
                    }
                    for (int i = 5; i <= 7; i++) {
                        GL11.glVertex2f(x + 1, y + i);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 8);
                        GL11.glVertex2f(x + i, y + 0);
                    }
                    for (int i = 2; i <= 6; i++) {
                        GL11.glVertex2f(x + i, y + 4);
                    }
                    GL11.glVertex2f(x + 1, y + 0);
                    x += 8;
                    break;
                }
                case '.': {
                    GL11.glVertex2f(x + 1, y);
                    x += 2;
                    break;
                }
                case ',': {
                    GL11.glVertex2f(x + 1, y);
                    GL11.glVertex2f(x + 1, y + 1);
                    x += 2;
                    break;
                }
                case '\n': {
                    y -= 10;
                    x = startX;
                    break;
                }
                case ' ': {
                    x += 8;
                }
            }
        GL11.glEnd();
    }

}
