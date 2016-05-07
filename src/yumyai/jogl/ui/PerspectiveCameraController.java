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

public class PerspectiveCameraController extends CameraController
{
    private PerspectiveCamera perspectiveCamera;

    public PerspectiveCameraController(PerspectiveCamera camera, GLSceneDrawer drawer)
    {
        this(camera, drawer, null, 0);
    }

    public PerspectiveCameraController(PerspectiveCamera camera, GLSceneDrawer drawer, ViewsCoordinator coordinator, int viewId)
    {
        super(camera, drawer, coordinator, viewId);
        this.perspectiveCamera = camera;
    }

    public void display(GLAutoDrawable drawable)
    {
        super.display(drawable);
        if (coordinator != null)
        {
            coordinator.setViewUpdated(viewId);
        }
    }

    protected void processMouseDragged(MouseEvent e)
    {
        if (mode == TRANSLATE_MODE)
        {
            perspectiveCamera.convertMotion(mouseDelta, worldMotion);
            perspectiveCamera.translate(worldMotion);
        }
        else if (mode == ZOOM_MODE)
        {
            perspectiveCamera.zoom(mouseDelta.y);
        }
        else if (mode == ROTATE_MODE)
        {
            perspectiveCamera.orbit(mouseDelta);
        }
    }
}
