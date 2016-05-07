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

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GLViewMac extends GLJPanel implements GLView {
    private Timer timer;
    private float targetFrameRate;

    public GLViewMac() {
        this(60);
    }

    private static GLCapabilities getDefaultCapabilities() {
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCapabilities.setAlphaBits(8);
        glCapabilities.setSampleBuffers(true);
        glCapabilities.setNumSamples(1);
        return glCapabilities;
    }

    public GLViewMac(float frameRate) {
        this(getDefaultCapabilities(), frameRate);
    }

    public GLViewMac(GLCapabilities glCapabilities) {
        this(glCapabilities, 60);
    }

    public GLViewMac(GLCapabilities glCapabilities, float initialFrameRate) {
        super(glCapabilities);
        initializerTimer(initialFrameRate);
    }

    private void initializerTimer(float initialFrameRate) {
        this.targetFrameRate = initialFrameRate;
        if (targetFrameRate <= 0)
            throw new RuntimeException("invalid frame rate!");

        timer = new Timer((int)(1000 / targetFrameRate), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
    }

    public void addGLController(GLController controller) {
        addGLEventListener(controller);
        addMouseListener(controller);
        addMouseMotionListener(controller);
        addKeyListener(controller);
    }

    public void removeGLController(GLController controller) {
        removeGLEventListener(controller);
        removeMouseListener(controller);
        removeMouseMotionListener(controller);
        removeKeyListener(controller);
    }

    public void startAnimation() {
        timer.start();
    }

    public void stopAnimation() {
        timer.stop();
    }

    public float getTargetFrameRate() {
        return targetFrameRate;
    }
}

