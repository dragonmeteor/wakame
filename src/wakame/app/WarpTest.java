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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import wakame.bsdf.Bsdf;
import wakame.bsdf.BsdfQueryRecord;
import wakame.bsdf.BsdfSampledDirection;
import wakame.bsdf.Microfacet;
import wakame.sampler.FixValueSampler;
import wakame.sampler.Independent;
import wakame.sampler.Sampler;
import wakame.struct.Color3d;
import wakame.struct.Measure;
import wakame.util.Warp;
import yondoko.util.AdaptiveSimpson;
import yondoko.util.Chi2Test;
import yumyai.jogl.ui.Arcball;
import yumyai.jogl.ui.GLFrameWithControlPanel;
import yumyai.ui.JSpinnerSlider;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax_.vecmath.Matrix4d;
import javax_.vecmath.Point2d;
import javax_.vecmath.Point3d;
import javax_.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class WarpTest extends GLFrameWithControlPanel {
    public static enum PointType {
        Independent,
        Grid,
        Stratified;

        public static PointType valueOf(int x) {
            switch (x) {
                case 0:
                    return Independent;
                case 1:
                    return Grid;
                case 2:
                    return Stratified;
                default:
                    throw new RuntimeException("Invalid value: " + x);
            }
        }
    }

    public static enum WarpType {
        None,
        Disk,
        UniformSphere,
        UniformSphereCap,
        UniformHemisphere,
        CosineHemisphere,
        Beckmann,
        MicrofacetBrdf;

        public static WarpType valueOf(int x) {
            switch (x) {
                case 0:
                    return None;
                case 1:
                    return Disk;
                case 2:
                    return UniformSphere;
                case 3:
                    return UniformSphereCap;
                case 4:
                    return UniformHemisphere;
                case 5:
                    return CosineHemisphere;
                case 6:
                    return Beckmann;
                case 7:
                    return MicrofacetBrdf;
                default:
                    throw new RuntimeException("Invalid value: " + x);
            }
        }
    }

    JComboBox pointTypeComboBox;
    JComboBox warpTypeComboBox;
    JSlider pointCountSlider;
    JTextField pointCountText;
    JSlider parameterSlider;
    JTextField parameterText;
    JSpinnerSlider angleSpinnerSlider;
    JCheckBox visualizeWarpedGridCheckBox;
    JCheckBox visualizeBsdfValueCheckBox;
    JButton runTestButton;
    Arcball arcball = new Arcball();
    /**
     * Points and their values.
     */
    ArrayList<Point3d> points = new ArrayList<Point3d>();
    ArrayList<Double> values = new ArrayList<Double>();
    ArrayList<Color3d> colors = new ArrayList<Color3d>();
    ArrayList<Point3d> gridLines = new ArrayList<Point3d>();
    int pointCount = 0;
    PointType pointType;
    WarpType warpType;
    boolean drawHistorgram = false;
    /**
     * Histograms
     */
    double[] obsHistogram;
    double[] expHistogram;
    int histXRes;
    int histYRes;
    /**
     * BSDF
     */
    Bsdf bsdf;
    BsdfQueryRecord bRec = new BsdfQueryRecord();
    /**
     * Other data
     */
    Sampler rng = (Sampler)new Independent.Builder().build();
    /**
     * Canvas info.
     */
    int canvasWidth;
    int canvasHeight;

    public WarpTest() {
        super();
        setTitle("Assignment 2: Sampling and Warping");
        setSize(new Dimension(1024, 768));
        refresh();
    }

    @Override
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
                                5, TableLayout.MINIMUM,
                                5, TableLayout.MINIMUM,
                                5, TableLayout.MINIMUM,
                                5
                        }
                };
        TableLayout tableLayout = new TableLayout(tableLayoutSizes);
        return tableLayout;
    }

    protected void initControlPanel() {
        super.initControlPanel();

        initPointTypeControls(1, 1);
        initWarpTypeControls(5, 1);
        initPointCountControls(1, 3);
        initAngleControls(5, 3);
        initParameterControls(5, 5);
        initCheckBoxes(3, 5);

        runTestButton = new JButton("Run Chi-Squared Test");
        runTestButton.addActionListener(this);
        controlPanel.add(runTestButton, "1,7,7,7");
    }

    private void initCheckBoxes(int col, int row) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        controlPanel.add(panel, String.format("%d, %d, %d, %d", col, row, col, row));

        visualizeWarpedGridCheckBox = new JCheckBox("Visualize warped grid");
        visualizeWarpedGridCheckBox.setSelected(true);
        visualizeWarpedGridCheckBox.addActionListener(this);
        panel.add(visualizeWarpedGridCheckBox);


        visualizeBsdfValueCheckBox = new JCheckBox("Visualize BSDF value");
        visualizeBsdfValueCheckBox.setSelected(false);
        visualizeBsdfValueCheckBox.addActionListener(this);
        panel.add(visualizeBsdfValueCheckBox);
    }

    private void initPointTypeControls(int col, int row) {
        JLabel label = new JLabel("Point Type:");
        controlPanel.add(label, String.format("%d,%d,%d,%d", col, row, col, row));

        pointTypeComboBox = new JComboBox();
        pointTypeComboBox.addItem("Independent");
        pointTypeComboBox.addItem("Grid");
        pointTypeComboBox.addItem("Stratified");
        pointTypeComboBox.addActionListener(this);
        controlPanel.add(pointTypeComboBox, String.format("%d,%d,%d,%d", col + 2, row, col + 2, row));
    }

    private void initWarpTypeControls(int col, int row) {
        JLabel label = new JLabel("Warp Type:");
        controlPanel.add(label, String.format("%d,%d,%d,%d", col,row,col,row));

        warpTypeComboBox = new JComboBox();
        warpTypeComboBox.addItem("None");
        warpTypeComboBox.addItem("Disk");
        warpTypeComboBox.addItem("Uniform sphere");
        warpTypeComboBox.addItem("Uniform sphere cap");
        warpTypeComboBox.addItem("Uniform hemisphere");
        warpTypeComboBox.addItem("Cosine hemisphere");
        warpTypeComboBox.addItem("Beckmann");
        warpTypeComboBox.addItem("Microfacet BRDF");
        warpTypeComboBox.addActionListener(this);
        controlPanel.add(warpTypeComboBox, String.format("%d,%d,%d,%d", col+2, row, col+2, row));
    }

    private void initPointCountControls(int col, int row) {
        JLabel label = new JLabel("Point Count:");
        controlPanel.add(label, String.format("%d,%d,%d,%d", col, row, col, row));

        JPanel panel = new JPanel();
        double[][] tableLayoutSizes =
                {
                        {
                                TableLayout.MINIMUM, 5, TableLayout.FILL
                        },
                        {
                                TableLayout.MINIMUM,
                        }
                };
        panel.setLayout(new TableLayout(tableLayoutSizes));
        pointCountText = new JTextField();
        pointCountText.setEnabled(false);
        pointCountText.setMinimumSize(new Dimension(100, 25));
        panel.add(pointCountText, "0,0,0,0");
        pointCountSlider = new JSlider(0, 100, 20);
        pointCountSlider.addChangeListener(this);
        panel.add(pointCountSlider, "2,0,2,0");
        controlPanel.add(panel, String.format("%d,%d,%d,%d", col + 2, row, col + 2, row));
    }

    private void initAngleControls(int col, int row) {
        JLabel label = new JLabel("BSDF Angle:");
        controlPanel.add(label, String.format("%d,%d,%d,%d", col, row, col, row));

        angleSpinnerSlider = new JSpinnerSlider(-90, 90, 1800, 30);
        angleSpinnerSlider.addChangeListener(this);
        controlPanel.add(angleSpinnerSlider, String.format("%d,%d,%d,%d", col + 2, row, col + 2, row));
    }

    private void initParameterControls(int col, int row) {
        JLabel label = new JLabel("Parameter:");
        controlPanel.add(label, String.format("%d,%d,%d,%d", col, row, col, row));

        JPanel panel = new JPanel();
        double[][] tableLayoutSizes =
                {
                        {
                                TableLayout.MINIMUM, 5, TableLayout.FILL
                        },
                        {
                                TableLayout.MINIMUM,
                        }
                };
        panel.setLayout(new TableLayout(tableLayoutSizes));
        parameterText = new JTextField();
        parameterText.setEnabled(false);
        parameterText.setMinimumSize(new Dimension(100, 25));
        panel.add(parameterText, "0,0,0,0");
        parameterSlider = new JSlider(0, 100,50);
        parameterSlider.addChangeListener(this);
        panel.add(parameterSlider, "2,0,2,0");
        controlPanel.add(panel, String.format("%d,%d,%d,%d", col + 2, row, col + 2, row));
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == angleSpinnerSlider) {
            refresh();
        } else if (e.getSource() == parameterSlider) {
            refresh();
        } else if (e.getSource() == pointCountSlider) {
            refresh();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == warpTypeComboBox) {
            refresh();
        } else if (e.getSource() == pointTypeComboBox) {
            refresh();
        } else if (e.getSource() == visualizeWarpedGridCheckBox) {
            refresh();
        } else if (e.getSource() == visualizeBsdfValueCheckBox) {
            refresh();
        } else if (e.getSource() == runTestButton) {
            runTest();
        }
    }

    private double mapParameter(WarpType warpType, double x) {
        if (warpType == WarpType.Beckmann || warpType == WarpType.MicrofacetBrdf) {
            return Math.exp(Math.log(0.05) * (1-x) + Math.log(1) * x);
        } else {
            return x;
        }
    }

    private Point3d constHalf = new Point3d(0.5, 0.5, 0.0);
    private Point2d p = new Point2d();

    private void refresh() {
        drawHistorgram = false;

        pointType = PointType.valueOf(pointTypeComboBox.getSelectedIndex());
        warpType = WarpType.valueOf(warpTypeComboBox.getSelectedIndex());
        double rawParamterValue = (parameterSlider.getValue() - parameterSlider.getMinimum()) * 1.0 /
                (parameterSlider.getMaximum() - parameterSlider.getMinimum());
        double parameterValue = mapParameter(warpType, rawParamterValue);
        double rawPointCountValue = (pointCountSlider.getValue() - pointCountSlider.getMinimum()) * 1.0 /
                (pointCountSlider.getMaximum() - pointCountSlider.getMinimum());
        pointCount = (int)Math.pow(2.0, 15*rawPointCountValue + 5);

        if (warpType == WarpType.MicrofacetBrdf) {
            Microfacet.Builder builder = new Microfacet.Builder();
            builder.setProperty("alpha", parameterValue);
            builder.setProperty("kd", new Color3d(0,0,0));
            bsdf = (Bsdf)builder.build();

            double bsdfAngle = Math.PI * angleSpinnerSlider.getValue() / 180;
            bRec.wo.set(Math.sin(bsdfAngle), 0, Math.max(1e-4, Math.cos(bsdfAngle)));
            bRec.wo.normalize();
        }

        // Generate the point positions.
        try {
            points.clear();
            values.clear();
            generatePoints(pointCount, pointType, warpType, parameterValue, points, values);
        } catch (Exception e) {
            showExceptionDialog(e);
            warpTypeComboBox.removeActionListener(this);
            warpTypeComboBox.setSelectedIndex(0);
            warpTypeComboBox.addActionListener(this);
            refresh();
            return;
        }
        pointCount = points.size();

        double valueScale = 0;
        for (int i = 0; i < pointCount; i++) {
            valueScale = Math.max(valueScale, values.get(i));
        }
        valueScale = 1 / valueScale;

        if (!visualizeBsdfValueCheckBox.isSelected() || warpType != WarpType.MicrofacetBrdf) {
            valueScale = 0;
        }

        if (warpType != WarpType.None) {
            for (int i = 0; i < pointCount; i++) {
                if (values.get(i) == 0) {
                    points.get(i).set(Double.NaN, Double.NaN, Double.NaN);
                } else {
                    if (valueScale != 0) {
                        points.get(i).scale(valueScale * values.get(i));
                    }
                    points.get(i).scale(0.5);
                    points.get(i).add(constHalf);
                }
            }
        }

        if (visualizeWarpedGridCheckBox.isSelected()) {
            gridLines.clear();
            int gridRes = (int)(Math.sqrt(pointCount) + 0.5);
            int fineGridRes = 16*gridRes;
            final double coarseScale = 1.0 / gridRes;
            final double fineScale = 1.0 / fineGridRes;
            final double finalValueScale = valueScale;
            BiConsumer<Double, Double> processCoord = (Double x, Double y) -> {
                p.set(x, y);
                Pair<Point3d, Double> result = warpPoint(warpType, p, parameterValue);
                gridLines.add(result.getLeft());
                if (finalValueScale != 0)
                    gridLines.get(gridLines.size()-1).scale(result.getRight() * finalValueScale);
            };
            for (int i = 0; i <= gridRes; i++) {
                for (int j = 0; j <= fineGridRes; j++) {
                    processCoord.accept(j*fineScale, i*coarseScale);
                    processCoord.accept((j+1)*fineScale, i*coarseScale);
                    processCoord.accept(i*coarseScale, j*fineScale);
                    processCoord.accept(i*coarseScale, (j+1)*fineScale);
                }
            }
            if (warpType != WarpType.None) {
                for (int i = 0; i < gridLines.size(); i++) {
                    Point3d p = gridLines.get(i);
                    p.scale(0.5);
                    p.add(constHalf);
                }
            }
        }

        // Generate a color gradient
        colors.clear();
        double colScale = 1.0 / pointCount;
        for (int i = 0; i < pointCount; i++) {
            colors.add(new Color3d(i*1.0*colScale, 1-i*colScale, 0));
        }

        if (pointCount > 100000) {
            pointCountText.setText(String.format("%.2fM", pointCount * 1e-6));
        } else if (pointCount > 1000) {
            pointCountText.setText(String.format("%.2fK", pointCount * 1e-3));
        } else {
            pointCountText.setText(Integer.toString(pointCount));
        }

        if (warpType != WarpType.UniformSphereCap) {
            parameterText.setText(String.format("%.2g", parameterValue));
        } else {
            parameterText.setText(String.format("%.2f", parameterValue * 180));
        }
        parameterSlider.setEnabled(warpType == WarpType.Beckmann || warpType == WarpType.MicrofacetBrdf
                || warpType == warpType.UniformSphereCap);
        angleSpinnerSlider.setEnabled(warpType == WarpType.MicrofacetBrdf);
    }

    private void generatePoints(int pointCount, PointType pointType, WarpType warpType,
                                double parameterValue, ArrayList<Point3d> positions, ArrayList<Double> values) {
        /* Determine the number of points that should be sampled */
        int sqrtVal = (int)(Math.sqrt(pointCount + 0.5));
        double invSqrtVal = 1.0 / sqrtVal;
        if (pointType == PointType.Grid || pointType == PointType.Stratified) {
            pointCount = sqrtVal * sqrtVal;
        }

        Point2d sample = new Point2d();
        for (int i = 0; i < pointCount; i++) {
            int y = i / sqrtVal;
            int x = i % sqrtVal;

            switch (pointType) {
                case Independent:
                    double xx = rng.next1D();
                    double yy = rng.next1D();
                    sample.set(xx, yy);
                    break;
                case Grid:
                    sample.set((x + 0.5) * invSqrtVal, (y + 0.5) * invSqrtVal);
                    break;
                case Stratified:
                    sample.set((x + rng.next1D()) * invSqrtVal,
                            (y + rng.next1D()) * invSqrtVal);
                    break;
            }

            Pair<Point3d, Double> result = warpPoint(warpType, sample, parameterValue);
            positions.add(result.getLeft());
            values.add(result.getRight());
        }
    }

    private BsdfQueryRecord record = new BsdfQueryRecord();
    private FixValueSampler fixValueSampler = new FixValueSampler();

    private Pair<Point3d, Double> warpPoint(WarpType warpType, Point2d sample, double parameterValue) {
        Point3d result = new Point3d();
        Point2d warped = new Point2d();
        Color3d radiance = new Color3d();

        switch (warpType) {
            case None:
                Warp.squareToUniformSquare(sample, warped);
                result.set(warped.x, warped.y, 0);
                break;
            case Disk:
                Warp.squareToUniformDisk(sample, warped);
                result.set(warped.x, warped.y, 0);
                break;
            case UniformSphere:
                Warp.squareToUniformSphere(sample, result);
                break;
            case UniformSphereCap:
                Warp.squareToUniformSphereCap(sample, 1-2*parameterValue, result);
                break;
            case UniformHemisphere:
                Warp.squareToUniformHemisphere(sample, result);
                break;
            case CosineHemisphere:
                Warp.squareToCosineHemisphere(sample, result);
                break;
            case Beckmann:
                Warp.squareToBeckmann(sample, parameterValue, result);
                break;
            case MicrofacetBrdf:
                record.wo.set(bRec.wo);
                record.direction = BsdfSampledDirection.Wi;
                fixValueSampler.reset();
                fixValueSampler.add(sample.x);
                fixValueSampler.add(sample.y);
                bsdf.sample(record, fixValueSampler);
                double value = record.value.getLuminance();
                result.set(record.wi);
                if (value == 0)
                    return new ImmutablePair<>(result, 0.0);
                else {
                    bsdf.eval(record.wi, record.wo, record.measure, radiance);
                    return new ImmutablePair<>(result, radiance.getLuminance());
                }
        }

        return new ImmutablePair<>(result, 1.0);
    }

    private void showExceptionDialog(Exception e) {
        String str = "The following exception was thrown: " + e.toString() + ".\n\n" + "Would you like to see the stack trace?";
        int choice = JOptionPane.showConfirmDialog(this, str, "Exception Thrown", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(GLAutoDrawable glad) {
        // NO-OP
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        // NO-OP
    }

    private void colorMap(double v, double vmin, double vmax, Color3d color) {
        color.set(1,1,1);
        if (v < vmin)
            v = vmin;
        if (v > vmax)
            v = vmax;
        double dv = vmax - vmin;
        if (v < (vmin + 0.25 * dv)) {
            color.x = 0;
            color.y = 4.0 * (v - vmin) / dv;
        } else if (v < (vmin + 0.5 * dv)) {
            color.x = 0;
            color.z = 1.0 + 4.0 * (vmin + 0.25 * dv - v) / dv;
        } else if (v < (vmin + 0.75 * dv)) {
            color.x = 4.0 * (v - vmin - 0.5 * dv) / dv;
            color.z = 0.0;
        } else {
            color.y = 1.0 + 4.0 * (vmin + 0.75 * dv - v) / dv;
            color.z = 0.0;
        }
    }

    private Color3d histColor = new Color3d();

    private void renderHistogram(GL2 gl, int xres, int yres, double[] values) {
        double xWidth = 1.0 / xres;
        double yWidth = 1.0 / yres;
        gl.glBegin(GL2.GL_QUADS);
        for (int y = 0; y < yres; y++) {
            for (int x = 0; x < xres; x++) {
                double v = values[y*xres + x];
                /* http://paulbourke.net/texture_colour/colourspace/ */
                colorMap(v, 0, 1, histColor);
                gl.glColor3d(histColor.x, histColor.y, histColor.z);
                double x0 = x * 1.0 / xres;
                double x1 = (x+1) * 1.0 / xres;
                double y0 = y * 1.0 / yres;
                double y1 = (y+1) * 1.0 / yres;
                gl.glVertex3d(x0, y0, 0);
                gl.glVertex3d(x1, y0, 0);
                gl.glVertex3d(x1, y1, 0);
                gl.glVertex3d(x0, y1, 0);
            }
        }
        gl.glEnd();
    }

    Matrix4d rotMatrix = new Matrix4d();

    @Override
    public void display(GLAutoDrawable glad) {
        final GL2 gl = glad.getGL().getGL2();
        GLU glu = new GLU();

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);

        if (drawHistorgram) {
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            if (canvasWidth > canvasHeight) {
                double aspect = canvasWidth * 1.0 / canvasHeight;
                glu.gluOrtho2D(-aspect, aspect, -1, 1);

                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();

                gl.glPushMatrix();
                gl.glTranslated(-aspect/2, 0.0, 0.0);
                gl.glScaled(0.9*aspect, 0.9*aspect, 1);
                gl.glTranslated(-0.5, -0.5, 0.0);
                renderHistogram(gl, histXRes, histYRes, obsHistogram);
                gl.glPopMatrix();

                gl.glPushMatrix();
                gl.glTranslated(aspect/2, 0.0, 0.0);
                gl.glScaled(0.9*aspect, 0.9*aspect, 1);
                gl.glTranslated(-0.5, -0.5, 0.0);
                renderHistogram(gl, histXRes, histYRes, expHistogram);
                gl.glPopMatrix();
            } else {
                double aspect = canvasHeight * 1.0 / canvasWidth;
                glu.gluOrtho2D(-1, 1, -aspect, aspect);

                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();

                gl.glPushMatrix();
                gl.glTranslated(-0.5, 0.0, 0.0);
                gl.glScaled(0.9, 0.9, 1);
                gl.glTranslated(-0.5, -0.5, 0.0);
                renderHistogram(gl, histXRes, histYRes, obsHistogram);
                gl.glPopMatrix();

                gl.glPushMatrix();
                gl.glTranslated(0.5, 0.0, 0.0);
                gl.glScaled(0.9, 0.9, 1);
                gl.glTranslated(-0.5, -0.5, 0.0);
                renderHistogram(gl, histXRes, histYRes, expHistogram);
                gl.glPopMatrix();
            }
        } else {
            gl.glMatrixMode(GL2.GL_PROJECTION);
            double viewAngle = 30;
            double near = 0.01;
            double far = 100;
            double fH = Math.tan(viewAngle / 360 * Math.PI) * near;
            double fW = fH * canvasWidth / canvasHeight;
            gl.glLoadIdentity();
            gl.glFrustum(-fW, fW, -fH, fH, near, far);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(0, 0, 2, 0, 0, 0, 0, 1, 0);
            if (warpType != WarpType.None && warpType != WarpType.Disk) {
                arcball.getMatrix(rotMatrix);
                gl.glMultMatrixd(new double[] {
                        rotMatrix.m00, rotMatrix.m10, rotMatrix.m20, rotMatrix.m30,
                        rotMatrix.m01, rotMatrix.m11, rotMatrix.m21, rotMatrix.m31,
                        rotMatrix.m02, rotMatrix.m12, rotMatrix.m22, rotMatrix.m32,
                        rotMatrix.m03, rotMatrix.m13, rotMatrix.m23, rotMatrix.m33
                }, 0);
            }
            gl.glTranslated(-0.5, -0.5, 0.0);

            // Render the point set.
            gl.glEnable(GL2.GL_DEPTH_TEST);
            if (visualizeWarpedGridCheckBox.isSelected()) {
                gl.glBegin(GL2.GL_LINES);
                gl.glColor3d(0.4, 0.4, 0.4);
                for (int i = 0; i < gridLines.size(); i++) {
                    Point3d point = gridLines.get(i);
                    gl.glVertex3d(point.x, point.y, point.z);
                }
                gl.glEnd();
            }
            gl.glPointSize(2.0f);
            gl.glBegin(GL2.GL_POINTS);
            for (int i = 0; i < points.size(); i++) {
                Color3d color = colors.get(i);
                gl.glColor3d(color.x, color.y, color.z);
                Point3d point = points.get(i);
                gl.glVertex3d(point.x, point.y, point.z);
            }
            gl.glEnd();
        }
    }

    @Override
    public void reshape(GLAutoDrawable glad, int x, int y, int width, int height) {
        canvasHeight = height;
        canvasWidth = width;
        arcball.setSize(width, height);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // NO-OP
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && warpType != WarpType.Disk && warpType != WarpType.None) {
            arcball.button(e.getX(), e.getY(), true);
        }
        drawHistorgram = false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (warpType != WarpType.Disk && warpType != WarpType.None)
        arcball.button(e.getX(), e.getY(), false);
        drawHistorgram = false;
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
        if (arcball.isActive()) {
            arcball.motion(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // NO-OP
    }

    private void runTest() {
        int xres = 51;
        int yres = 51;
        WarpType warpType = WarpType.valueOf(warpTypeComboBox.getSelectedIndex());
        double rawParamterValue = (parameterSlider.getValue() - parameterSlider.getMinimum()) * 1.0 /
                (parameterSlider.getMaximum() - parameterSlider.getMinimum());
        double parameterValue = mapParameter(warpType, rawParamterValue);

        if (warpType != WarpType.None && warpType != WarpType.Disk) {
            xres *= 2;
        }

        int res = yres*xres;
        int sampleCount = 1000 * res;
        double[] obsFrequencies = new double[res];
        double[] expFrequencies = new double[res];
        for (int i = 0; i < res; i++) {
            obsFrequencies[i] = 0;
            expFrequencies[i] = 0;
        }

        ArrayList<Point3d> points = new ArrayList<Point3d>();
        ArrayList<Double> values = new ArrayList<Double>();
        generatePoints(sampleCount, PointType.Independent, warpType, parameterValue, points, values);

        for (int i = 0; i < sampleCount; i++) {
            if (values.get(i) == 0) {
                continue;
            }
            Point3d sample = points.get(i);
            double x, y;

            if (warpType == WarpType.None) {
                x = sample.x;
                y = sample.y;
            } else if (warpType == WarpType.Disk) {
                x = sample.x * 0.5 + 0.5;
                y = sample.y * 0.5 + 0.5;
            } else {
                x = Math.atan2(sample.y, sample.x)  / (2*Math.PI);
                if (x < 0)
                    x += 1;
                y = sample.z * 0.5 + 0.5;
            }

            int xbin = Math.min(xres-1, (int)Math.max(0, Math.floor(x * xres)));
            int ybin = Math.min(yres-1, (int)Math.max(0, Math.floor(y * yres)));
            obsFrequencies[ybin * xres + xbin] += 1;
        }

        BiFunction<Double, Double, Double> integrand = (Double y, Double x) -> {
            if (warpType == WarpType.None) {
                return Warp.squareToUniformSquarePdf(new Point2d(x,y));
            } else if (warpType == WarpType.Disk) {
                x = x*2 - 1;
                y = y*2 - 1;
                return Warp.squareToUniformDiskPdf(new Point2d(x, y));
            } else {
                x *= 2*Math.PI;
                y = y*2 - 1;

                double sinTheta = Math.sqrt(1 - y*y);
                double sinPhi = Math.sin(x);
                double cosPhi = Math.cos(x);

                Vector3d v = new Vector3d(sinTheta * cosPhi, sinTheta * sinPhi, y);

                if (warpType == WarpType.UniformSphere) {
                    return Warp.squareToUniformSpherePdf(v);
                } else if (warpType == WarpType.UniformSphereCap) {
                    return Warp.squareToUniformSphereCapPdf(v, 1-2*parameterValue);
                } else if (warpType == WarpType.UniformHemisphere) {
                    return Warp.squareToUniformHemispherePdf(v);
                } else if (warpType == WarpType.CosineHemisphere) {
                    return Warp.squareToCosineHemispherePdf(v);
                } else if (warpType == WarpType.Beckmann) {
                    return Warp.squareToBeckmannPdf(v, parameterValue);
                } else if (warpType == WarpType.MicrofacetBrdf) {
                    BsdfQueryRecord record = new BsdfQueryRecord();
                    record.wo.set(bRec.wo);
                    record.wi.set(v);
                    record.measure = Measure.SolidAngle;
                    return bsdf.pdf(record);
                } else {
                    throw new RuntimeException("Invalid warp type");
                }
            }
        };


        double scale = sampleCount;
        if (warpType == WarpType.None) {
            scale *= 1;
        } else if (warpType == WarpType.Disk) {
            scale *= 4;
        } else {
            scale *= 4*Math.PI;
        }

        for (int y = 0; y < yres; y++) {
            double yStart = y * 1.0 / yres;
            double yEnd = (y+1) * 1.0 / yres;
            for (int x = 0; x < xres; x++) {
                double xStart = x * 1.0 / xres;
                double xEnd = (x+1) * 1.0 / xres;
                expFrequencies[y*xres + x] = AdaptiveSimpson.integrate2D(integrand, yStart, xStart, yEnd, xEnd, 1e-12, 10) * scale;
                if (expFrequencies[y*xres + x] < 0)
                    throw new RuntimeException("The pdf() function returned negative values");
            }
        }

        /* Write the test input data to disk for debugging */
        chi2Dump(yres, xres, obsFrequencies, expFrequencies, "chitest.m");

        /* Perform the Chi^2 test */
        final double minExpFrequency = 5;
        final double significanceLevel = 0.01;

        Pair<Boolean, String> testResult = Chi2Test.chi2Test(yres*xres, obsFrequencies, expFrequencies,
                sampleCount, minExpFrequency, significanceLevel, 1);

        double maxValue = 0;
        double minValue = Double.POSITIVE_INFINITY;
        for (int i = 0; i < res; i++) {
            maxValue = Math.max(maxValue, Math.max(obsFrequencies[i], expFrequencies[i]));
            minValue = Math.min(minValue, Math.min(obsFrequencies[i], expFrequencies[i]));
        }
        minValue /= 2;
        double texScale = 1 / (maxValue - minValue);

        obsHistogram = new double[res];
        expHistogram = new double[res];
        histXRes = xres;
        histYRes = yres;
        for (int i = 0; i < res; i++) {
            obsHistogram[i] = (obsFrequencies[i] - minValue) * texScale;
            expHistogram[i] = (expFrequencies[i] - minValue) * texScale;
        }

        System.out.println(testResult.getRight());
        System.out.println();
        drawHistorgram = true;
        JOptionPane.showMessageDialog(this, testResult.getRight());
    }

    /**
     * Write 2D Chi^2 frequency tables to disk in a format that is nicely plottable by Octave and MATLAB
     * @param res1
     * @param res2
     * @param obsFrequencies
     * @param expFrequencies
     * @param fileName
     */
    private void chi2Dump(int res1, int res2, double[] obsFrequencies, double[] expFrequencies, String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter fout = new BufferedWriter(fileWriter);

            fout.write("obsFrequencies = [");
            for (int i = 0; i < res1; i++) {
                for (int j = 0; j < res2; j++) {
                    fout.write(Double.toString(obsFrequencies[i * res2 + j]));
                    if (j+1 < res2)
                        fout.write(", ");
                }
                if (i+1 < res1)
                    fout.write("; ");
            }
            fout.write(" ];\n");

            fout.write("expFrequencies = [");
            for (int i = 0; i < res1; i++) {
                for (int j = 0; j < res2; j++) {
                    fout.write(Double.toString(expFrequencies[i * res2 + j]));
                    if (j+1 < res2)
                        fout.write(", ");
                }
                if (i+1 < res1)
                    fout.write("; ");
            }
            fout.write(" ];\n");

            fout.write("colormap(jet);\n");
            fout.write("clf; subplot(2,1,1);\n");
            fout.write("imagesc(obsFrequencies);");
            fout.write("title('Observed frequencies');");
            fout.write("axis equal;");
            fout.write("subplot(2,1,2);");
            fout.write("imagesc(expFrequencies);");
            fout.write("axis equal;");
            fout.write("title('Expected frequencies');");

            fout.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    WarpTest frame = new WarpTest();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
