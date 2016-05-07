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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

public class BasicAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    protected ActionListener listener;

    public BasicAction(String newName, ActionListener newListener) {
        super(newName);
        listener = newListener;
    }

    public void setShortDescription(String s) {
        putValue(AbstractAction.SHORT_DESCRIPTION, s);
    }

    public void setMnemonicKey(int i) {

        putValue(AbstractAction.MNEMONIC_KEY, new Integer(i));
    }

    public void setAcceleratorKey(int key, int masks) {
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key, masks));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        listener.actionPerformed(e);
    }
}