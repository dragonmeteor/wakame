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

import layout.TableLayout;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class GLFrameWithControlPanel extends JFrame implements GLController,
        ChangeListener, ActionListener {
    protected GLViewPanel glViewPanel;
    protected JPanel controlPanel;

    public GLFrameWithControlPanel() {
        initGui();
    }

    protected void initGui() {
        getContentPane().setLayout(new BorderLayout());

        glViewPanel = new GLViewPanel(60) {
        };
        glViewPanel.addGLController(this);
        glViewPanel.setMinimumSize(new Dimension(800, 600));
        glViewPanel.setPreferredSize(new Dimension(1200, 800));
        getContentPane().add(glViewPanel, BorderLayout.CENTER);

        initControlPanel();

        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                glViewPanel.requestFocusInWindow();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    protected void initControlPanel() {
        controlPanel = new JPanel();
        LayoutManager controlPanelLayout = createControlPanelLayout();
        controlPanel.setLayout(controlPanelLayout);
    }

    protected LayoutManager createControlPanelLayout() {
        double tableLayoutSizes[][] =
                {
                        {
                                5, TableLayout.MINIMUM, 5, TableLayout.FILL,
                                5, TableLayout.MINIMUM, 5, TableLayout.FILL,
                                5,
                        },
                        {
                                5, TableLayout.MINIMUM,
                                5
                        }
                };
        TableLayout tableLayout = new TableLayout(tableLayoutSizes);
        return tableLayout;
    }

    @Override
    public void init(GLAutoDrawable glad) {
        // NO-OP
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        // NO-OP
    }

    @Override
    public void display(GLAutoDrawable glad) {
        final GL2 gl = glad.getGL().getGL2();

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {
        // NO-OP
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // NO-OP
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // NO-OP
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // NO-OP
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // NO-OP
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // NO-OP
    }
}

