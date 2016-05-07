/*
 * This file is part of Wakame, a Java reimplementation of Nori, an educational ray tracer by Wenzel Jakob.
 *
 * Copyright (c) 2015 by Pramook Khungurn
 *
 * Wakame is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 3
 * as published by the Free Software Foundation.
 *
 * Wakame is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yumyai.jogl.ui;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax_.vecmath.Tuple2f;

public class Camera2dController {
    private float baseScalingFactor;
    private float scalingFactor;
    private float centerX, centerY;
    private float windowWidth, windowHeight, windowAspect;
    private float startX, startY;
    private float startCenterX, startCenterY;
    private boolean dragged = false;

    public Camera2dController(float initialScalingFactor, float initialCenterX, float initialCenterY) {
        this.baseScalingFactor = initialScalingFactor;
        this.centerX = initialCenterX;
        this.centerY = initialCenterY;
        this.dragged = false;
    }

    public void setBaseScalingFactor(float baseScalingFactor) {
        this.baseScalingFactor = baseScalingFactor;
    }

    public void setCenter(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public float getCenterX() {
        return this.centerX;
    }

    public float getCenterY() {
        return this.centerY;
    }

    public void updateScalingFactor(float scalingExponent) {
        scalingFactor = (float) (baseScalingFactor * Math.exp(scalingExponent * Math.log(2.0)));
    }

    public float getScalingFactor() {
        return scalingFactor;
    }

    public void doModelView(GL2 gl) {
        gl.glScaled(scalingFactor, scalingFactor, 1);
        gl.glTranslated(-centerX, -centerY, 0);
    }

    public void doProjection(GL2 gl) {
        GLU glu = new GLU();
        if (windowAspect > 1) {
            glu.gluOrtho2D(-windowAspect, windowAspect, -1, 1);
        } else {
            glu.gluOrtho2D(-1, 1, -1.0f / windowAspect, 1.0f / windowAspect);
        }
    }

    public void reshape(int x, int y, int width, int height) {
        if (width < 0) {
            width = 1;
        }
        if (height < 0) {
            height = 1;
        }

        windowWidth = width;
        windowHeight = height;

        float aspect = width * 1.0f / height;
        windowAspect = aspect;
    }

    public void mousePressed(int x, int y) {
        dragged = true;
        startX = x;
        startY = y;
        startCenterX = centerX;
        startCenterY = centerY;
    }

    public void mouseReleased() {
        dragged = false;
    }

    public void convertToCameraCoord(int x, int y, Tuple2f result) {
        float dx = 2.0f * x / windowWidth - 1.0f;
        float dy = 2.0f * y / windowHeight - 1.0f;

        if (windowAspect >= 1) {
            dx *= windowAspect;
        } else {
            dy /= windowAspect;
        }

        dx /= scalingFactor;
        dy /= scalingFactor;

        result.x = dx + centerX;
        result.y = centerY - dy;
    }

    public void mouseDragged(int x, int y) {
        if (dragged) {
            float currentX = x;
            float currentY = y;

            float dx = currentX - startX;
            float dy = currentY - startY;

            if (windowAspect >= 1) {
                dx *= windowAspect / (windowWidth / 2.0f);
                dy /= (windowHeight / 2.0f);
            } else {
                dx /= (windowWidth / 2.0f);
                dy /= (windowHeight / 2.0f) * windowAspect;
            }

            dx /= scalingFactor;
            dy /= scalingFactor;

            centerX = startCenterX - dx;
            centerY = startCenterY + dy;
        }
    }
}
