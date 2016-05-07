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

package yumyai.ui;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import layout.TableLayout;

public class JSpinnerSlider extends JPanel implements ChangeListener {
    protected JSpinner spinner;
    protected JSlider slider;
    protected EventListenerList changeListenerList;

    double minValue;
    double maxValue;
    int intervals;

    public JSpinnerSlider() {
        this(0, 1, 100, 50);
    }

    public JSpinnerSlider(
            double minValue,
            double maxValue,
            int intervals,
            double initialValue) {
        super();

        double tableSize[][] = {
                {100, TableLayout.FILL}, {TableLayout.MINIMUM}
        };
        setLayout(new TableLayout(tableSize));

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.intervals = intervals;

        slider = new JSlider(JSlider.HORIZONTAL, 0, intervals,
                convertToSliderValue(initialValue));
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        slider.addChangeListener(this);
        add(slider, "1, 0, 1, 0");

        spinner = new JSpinner(
                new SpinnerNumberModel(
                        new Double(initialValue),
                        new Double(minValue),
                        new Double(maxValue),
                        new Double(getIntervalWidth())));
        spinner.addChangeListener(this);
        add(spinner, "0, 0, 0, 0");

        changeListenerList = new EventListenerList();
    }

    public void setValue(double value) {
        if (getValue() != value) {
            spinner.setValue(new Double(value));
        }
    }

    public double getValue() {
        return ((Double) spinner.getValue()).doubleValue();
    }

    private int convertToSliderValue(double actualValue) {
        return (int) Math.round((actualValue - minValue) / getIntervalWidth());
    }

    private double convertToActualValue(int sliderValue) {
        return minValue + getIntervalWidth() * sliderValue;
    }

    private double getIntervalWidth() {
        return (maxValue - minValue) / intervals;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        boolean changed = false;
        if (e.getSource() == spinner) {
            int sliderValue = convertToSliderValue(
                    ((Double) spinner.getValue()).doubleValue());
            if (slider.getValue() != sliderValue) {
                slider.setValue(sliderValue);
                slider.repaint();
                changed = true;
            }
        } else if (e.getSource() == slider) {
            int spinnerValue = convertToSliderValue(((Double) spinner.getValue()).doubleValue());
            if ((spinnerValue != slider.getValue())) {
                double actualValue = convertToActualValue(slider.getValue());
                spinner.setValue(new Double(actualValue));
                spinner.repaint();
                changed = true;
            }
        }
        if (changed) {
            fireStateChanged();
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeListenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListenerList.remove(ChangeListener.class, listener);
    }

    protected void fireStateChanged() {
        Object[] listeners = changeListenerList.getListenerList();
        ChangeEvent event = new ChangeEvent(this);
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(event);
            }
        }
    }

    /**
     * Change the configuration to match the given parameters.
     * @param minValue the new minimum value
     * @param maxValue the new maximum value
     * @param intervals the new number of intervals
     * @param newValue the new value
     */
    public void setup(double minValue, double maxValue, int intervals, double newValue) {
        double oldValue = getValue();
        slider.removeChangeListener(this);
        spinner.removeChangeListener(this);

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.intervals = intervals;

        double currentValue = Math.min(Math.max(newValue, minValue), maxValue);
        int currentTick = convertToSliderValue(currentValue);
        currentValue = convertToActualValue(currentTick);

        slider.setMinimum(0);
        slider.setMaximum(intervals);
        slider.setValue(currentTick);

        spinner.setModel(new SpinnerNumberModel(new Double(currentValue),
                new Double(minValue),
                new Double(maxValue),
                new Double(getIntervalWidth())));

        slider.addChangeListener(this);
        spinner.addChangeListener(this);
        if (currentValue != oldValue)
            fireStateChanged();
    }

    /**
     * Change the configuration to match the given parameters while trying
     * to preserve the value of the control if possible.
     * @param minValue the new minimum value
     * @param maxValue the new maximum value
     * @param intervals the new value
     */
    public void setup(double minValue, double maxValue, int intervals) {
        setup(minValue, maxValue, intervals, getValue());
    }

    public void setEnabled(boolean enabled) {
        slider.setEnabled(enabled);
        spinner.setEnabled(enabled);
    }
}
