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

import javax.media.opengl.GLContext;

public class GLViewPanelWithCameraControl extends GLViewPanel implements ViewsCoordinator {
    private static final long serialVersionUID = 1L;
    protected CameraController cameraController;
    protected PickingController pickingController;
    protected boolean viewsUpdated = false;

    public GLViewPanelWithCameraControl(int initialFrameRate, CameraController cameraController) {
        super(initialFrameRate);
        this.cameraController = cameraController;
        this.pickingController = new PickingController(cameraController);
        addGLController(pickingController);
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    public PickingController getPickingController() {
        return pickingController;
    }

    public void addPickingEventListener(PickingEventListener listener) {
        pickingController.addPickingEventListener(listener);
    }

    public void removePickingEventListener(PickingEventListener listener) {
        pickingController.removePickingEventListener(listener);
    }

    public void addPrioritizedObjectId(int id) {
        pickingController.addPrioritizedObjectId(id);
    }

    public void removePrioritizedObjectId(int id) {
        pickingController.removePrioritizedObjectId(id);
    }

    public void resetUpdatedStatus() {
        viewsUpdated = false;
    }

    public boolean checkAllViewsUpdated() {
        return viewsUpdated;
    }

    public void setViewUpdated(int viewId) {
        viewsUpdated = true;
    }

    public void captureNextFrame() {
        cameraController.captureNextFrame();
    }
}
