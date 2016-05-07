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
import yondoko.image.Pfm;
import yondoko.util.VectorUtil;
import yumyai.jogl.ui.GLFrameWithCamera2d;
import yumyai.ui.BasicAction;
import yumyai.ui.JSpinnerSlider;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax_.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class ViewPfm extends GLFrameWithCamera2d implements ActionListener, ChangeListener {
    /**
     * Commands
     */
    private static final String LOAD_PFM = "Load PFM ...";
    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;
    /**
     * Inputs
     */
    Pfm pfm;
    int imageWidth = 10;
    int imageHeight = 10;
    String pfmFileName = "";
    /**
     * Controls
     */
    JTextField pfmFileNameTextField;
    JButton loadPfmButton;
    JSpinnerSlider exposureSlider;
    JSpinnerSlider gammaSpinnerSlider;
    JFileChooser pfmFileChooser;
    JCheckBox srgbCheckbox;

    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("Usage: java wakame.app.ViewPfm <file.pfm>");
            System.exit(0);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ViewPfm frame = new ViewPfm();
                    frame.processCommandLineArguments(args);
                    frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ViewPfm() {
        super(1.0f / 200, 90, 90);
        setTitle("View PFM File");
        initExtraControls();
    }

    private void processCommandLineArguments(String[] args) {
        if (args.length > 0) {
            try {
                loadPfm(args[0]);
            } catch (Exception e) {
                showExceptionDialog(e);
            }
        }
    }

    private void loadPfm(String fileName) throws IOException {
        try {
            System.out.print("Loading " + fileName + " ... ");
            pfm = Pfm.load(fileName);
            System.out.println("DONE!");

            imageWidth = pfm.width;
            imageHeight = pfm.height;

            pfmFileName = new File(fileName).getAbsolutePath();
        } catch (Exception e) {
            pfm = null;
            pfmFileName = "";
            imageWidth = 100;
            imageHeight = 100;
        }

        float xFactor = 2.0f / (imageWidth * 1.1f);
        float yFactor = 2.0f / (imageHeight * 1.1f);
        float factor = Math.min(xFactor, yFactor);
        camera2dController.setBaseScalingFactor(factor);
        camera2dController.setCenter(imageWidth / 2.0f, imageHeight / 2.0f);

        updateComponents();
    }

    private void updateComponents() {
        pfmFileNameTextField.setText(pfmFileName);

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
        createPfmFileControls(3);
        createExposureControls(5);
        createGammaControls(7);
        initializeFileChooser();
    }

    private void createPfmFileControls(int row) {
        JLabel pfmLabel = new JLabel("Pfm:");
        controlPanel.add(pfmLabel, String.format("1, %d, 1, %d", row, row));

        pfmFileNameTextField = new JTextField();
        pfmFileNameTextField.setText("");
        controlPanel.add(pfmFileNameTextField, String.format("3, %d, 3, %d", row, row));

        BasicAction loadButtonAction = new BasicAction(LOAD_PFM, this);
        loadButtonAction.setAcceleratorKey(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
        loadPfmButton = new JButton(loadButtonAction);
        controlPanel.add(loadPfmButton, String.format("5, %d, 5, %d", row, row));
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

    private void initializeFileChooser() {
        pfmFileChooser = new JFileChooser(new File("../togpaper/images").getAbsolutePath());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PFM files", "pfm");
        pfmFileChooser.setFileFilter(filter);
    }

    @Override
    public void init(GLAutoDrawable glad) {
        super.init(glad);
    }

    Vector3d color = new Vector3d();

    @Override
    public void display(GLAutoDrawable glad) {
        final GL2 gl = glad.getGL().getGL2();

        super.display(glad);

        if (pfm != null) {
            double exposure = Math.pow(2, exposureSlider.getValue());
            double gamma = gammaSpinnerSlider.getValue();


            gl.glBegin(GL2.GL_QUADS);
            for (int iy = 0; iy < imageHeight; iy++) {
                for (int ix = 0; ix < imageWidth; ix++) {
                    pfm.getColor(ix, iy, color);

                    for (int i = 0; i < 3; i++) {
                        double v = VectorUtil.getComponent(color, i);
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
        String command = e.getActionCommand();
        if (command.equals(LOAD_PFM)) {
            //Select a file
            int choice = pfmFileChooser.showOpenDialog(this);
            if (choice != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                String filename = pfmFileChooser.getSelectedFile().getAbsolutePath();
                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex >= 0) {
                    String extension = filename.substring(dotIndex);
                    if (extension.toLowerCase().equals(".pfm")) {
                        loadPfm(filename);
                        return;
                    } else {
                        throw new RuntimeException("Unsupported extension: " + extension);
                    }
                } else {
                    throw new RuntimeException("Selected file does not have an extension");
                }
            } catch (Exception ex) {
                showExceptionDialog(ex);
            }
        } else if (e.getSource() == srgbCheckbox) {
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
