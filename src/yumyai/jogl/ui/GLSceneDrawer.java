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

import java.awt.event.MouseEvent;

import javax.media.opengl.GLAutoDrawable;

public interface GLSceneDrawer
{
    public void init(GLAutoDrawable drawable, CameraController controller);

    public void draw(GLAutoDrawable drawable, CameraController controller);
    
    public void dispose(GLAutoDrawable drawable, CameraController controller);

    public void mousePressed(MouseEvent e, CameraController controller);

    public void mouseReleased(MouseEvent e, CameraController controller);

    public void mouseDragged(MouseEvent e, CameraController controller);

    public void mouseMoved(MouseEvent e, CameraController controller);
}
