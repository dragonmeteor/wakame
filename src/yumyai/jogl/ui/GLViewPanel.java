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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.swing.JPanel;
import javax.swing.Timer;

public abstract class GLViewPanel extends JPanel
        implements GLController, ActionListener {
    private static final long serialVersionUID = 1L;
    protected int initialFrameRate;
    protected GLView glView;
    protected Timer timer;

    public GLViewPanel(int frameRate) {
        super(new BorderLayout());

        initialFrameRate = frameRate;

        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCapabilities.setAlphaBits(8);
        glCapabilities.setSampleBuffers(true);
        glCapabilities.setNumSamples(8);

        glView = GLViewUtil.create();
        glView.addGLController(this);

        timer = new Timer(1000 / initialFrameRate, this);

        add((Component)glView, BorderLayout.CENTER);
    }

    public GLView getGlView() {
        return glView;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        timer.start();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // NO-OP
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
        // NO-OP
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        ((Component) drawable).setMinimumSize(new Dimension(0, 0));
    }

    @Override
    public void keyTyped(KeyEvent key) {
        // NO-OP
    }

    @Override
    public void keyPressed(KeyEvent key) {
        // NO-OP
    }

    @Override
    public void keyReleased(KeyEvent key) {
        // NO-OP
    }

    @Override
    public void mouseClicked(MouseEvent mouse) {
        // NO-OP
    }

    @Override
    public void mousePressed(MouseEvent mouse) {
        // NO-OP
    }

    @Override
    public void mouseReleased(MouseEvent mouse) {
        // NO-OP
    }

    @Override
    public void mouseEntered(MouseEvent mouse) {
        // NO-OP
    }

    @Override
    public void mouseExited(MouseEvent mouse) {
        // NO-OP
    }

    @Override
    public void mouseDragged(MouseEvent mouse) {
        // NO-OP
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // NO-OP
    }

    public void startAnimation() {
        timer.start();
    }

    public void stopAnimation() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            glView.repaint();
        }
    }

    public void immediatelyRepaint() {
        glView.repaint();
    }

    public void addGLController(GLController controller) {
        glView.addGLController(controller);
    }
}