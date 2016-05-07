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

package wakame.app;

import layout.TableLayout;
import wakame.block.ImageBlock;
import yondoko.util.VectorUtil;
import yumyai.jogl.ui.GLFrameWithCamera2d;
import yumyai.ui.JSpinnerSlider;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax_.vecmath.Vector3d;
import javax_.vecmath.Vector4d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RenderingProgressFrame extends GLFrameWithCamera2d implements ActionListener, ChangeListener {
    /**
     * Commands
     */
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    /**
     * Inputs
     */
    ImageBlock image;
    int imageWidth = 10;
    int imageHeight = 10;
    /**
     * Controls
     */
    JSpinnerSlider exposureSlider;
    JSpinnerSlider gammaSpinnerSlider;
    JCheckBox srgbCheckbox;

    public RenderingProgressFrame(ImageBlock image) {
        super(1.0f / 200, 90, 90);
        this.image = image;
        setTitle("Rendering Progress");
        initExtraControls();
        initializeImage();
    }

    private void initializeImage() {
        imageWidth = this.image.getSizeX();
        imageHeight = this.image.getSizeY();

        float xFactor = 2.0f / (imageWidth * 1.1f);
        float yFactor = 2.0f / (imageHeight * 1.1f);
        float factor = Math.min(xFactor, yFactor);
        camera2dController.setBaseScalingFactor(factor);
        camera2dController.setCenter(imageWidth / 2.0f, imageHeight / 2.0f);
        updateComponents();
    }

    private void updateComponents() {
        if (srgbCheckbox.isSelected()) {
            gammaSpinnerSlider.setEnabled(false);
        } else {
            gammaSpinnerSlider.setEnabled(true);
        }
    }

    @Override
    protected LayoutManager createControlPanelLayout() {
        double tableLayoutSizes[][] =
                {
                        {
                                5, TableLayout.MINIMUM,
                                5, TableLayout.FILL,
                                5, TableLayout.MINIMUM,
                                5
                        },
                        {
                                5, TableLayout.MINIMUM,
                                5, TableLayout.MINIMUM,
                                5, TableLayout.MINIMUM,
                                5
                        }
                };
        TableLayout tableLayout = new TableLayout(tableLayoutSizes);
        return tableLayout;
    }

    protected void createZoomController() {
        JLabel zoomLabel = new JLabel("Zoom:");
        controlPanel.add(zoomLabel, "1, 1, 1, 1");
        zoomController = new JSpinnerSlider(-6, 6, 100, 0);
        controlPanel.add(zoomController, "3, 1, 5, 1");
    }

    private void initExtraControls() {
        createExposureControls(3);
        createGammaControls(5);
    }

    private void createExposureControls(int row) {
        JLabel maxLabel = new JLabel("Exposure:");
        controlPanel.add(maxLabel, String.format("1, %d, 1, %d", row, row));

        exposureSlider = new JSpinnerSlider(-10, 10, 1000, 0);
        controlPanel.add(exposureSlider, String.format("3, %d, 5, %d", row, row));
    }

    private void createGammaControls(int row) {
        JLabel gammaLabel = new JLabel("Gamma value:");
        controlPanel.add(gammaLabel, String.format("1, %d, 1, %d", row, row));

        gammaSpinnerSlider = new JSpinnerSlider(0.5, 3.0, 250, 1);
        controlPanel.add(gammaSpinnerSlider, String.format("3, %d, 3, %d", row, row));

        srgbCheckbox = new JCheckBox("sRGB");
        controlPanel.add(srgbCheckbox, String.format("5, %d, 5, %d", row, row));
        srgbCheckbox.addActionListener(this);
        srgbCheckbox.setSelected(true);
    }

    @Override
    public void init(GLAutoDrawable glad) {
        super.init(glad);
    }

    Vector3d color = new Vector3d();

    @Override
    public void display(GLAutoDrawable glad) {
        final GL2 gl = glad.getGL().getGL2();

        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        gl.glClearDepth(1);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);

        setupCamera(gl);

        synchronized (image) {
            double exposure = Math.pow(2, exposureSlider.getValue());
            double gamma = gammaSpinnerSlider.getValue();

            Vector4d[][] data = image.getData();

            gl.glBegin(GL2.GL_QUADS);
            for (int iy = 0; iy < imageHeight; iy++) {
                for (int ix = 0; ix < imageWidth; ix++) {
                    Vector4d d = data[imageHeight-iy-1 + image.getBorderSize()][ix + image.getBorderSize()];

                    for (int i = 0; i < 3; i++) {
                        double v = VectorUtil.getComponent(d, i);
                        if (d.w != 0) {
                            v /= d.w;
                        }
                        v *= exposure;
                        if (v < 0) v = 0;
                        if (v > 1) v = 1;
                        if (srgbCheckbox.isSelected()) {
                            if (v < 0.0031308) {
                                v = 12.92*v;
                            } else {
                                v = (1+0.055)*Math.pow(v, 1.0/2.4) - 0.055;
                            }
                        } else {
                            v = Math.pow(v, 1.0/gamma);
                        }
                        VectorUtil.setComponent(color, i, v);
                    }

                    gl.glColor3d(color.x, color.y, color.z);
                    gl.glVertex2f(ix, iy);
                    gl.glVertex2f(ix + 1, iy);
                    gl.glVertex2f(ix + 1, iy + 1);
                    gl.glVertex2f(ix, iy + 1);
                }
            }
            gl.glEnd();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == srgbCheckbox) {
            updateComponents();
        }
    }

    /**
     * Displays an exception in a window.
     */
    protected void showExceptionDialog(Exception e) {
        String str = "The following exception was thrown: " + e.toString() + ".\n\n" + "Would you like to see the stack trace?";
        int choice = JOptionPane.showConfirmDialog(this, str, "Exception Thrown", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            e.printStackTrace();
        }
    }
}
