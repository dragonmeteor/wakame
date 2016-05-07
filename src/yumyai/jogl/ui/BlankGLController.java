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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.GLAutoDrawable;

public abstract class BlankGLController implements GLController
{
    @Override
    public void display(GLAutoDrawable drawable)
    {
        // NO-OP
    }

    @Override
    public void dispose(GLAutoDrawable drawable)
    {
        // NO-OP
    }

    @Override
    public void init(GLAutoDrawable drawable)
    {
        // NO-OP
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
            int height)
    {
        // NO-OP
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        // NO-OP
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // NO-OP
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // NO-OP
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // NO-OP
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        // NO-OP
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        // NO-OP
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        // NO-OP
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // NO-OP
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // NO-OP
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        // NO-OP
    }
}
