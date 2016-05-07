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

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax_.vecmath.Point3f;
import javax_.vecmath.Vector2f;
import javax_.vecmath.Vector3f;

public class OrthographicCamera extends PerspectiveCamera
{
    public OrthographicCamera()
    {
        super();
    }

    public OrthographicCamera(float near, float far, float fovy)
    {
        super(near, far, fovy);
    }

    public OrthographicCamera(Point3f eye, Point3f target, Vector3f up, float near, float far, float fovy)
    {
        super(eye, target, up, near, far, fovy);
    }

    @Override
    public void doProjection(GLAutoDrawable d)
    {
        final GL2 gl = d.getGL().getGL2();
        float height = getHeight();
        gl.glOrtho(-aspect * height, aspect * height, -height, height, near, far);
    }

    public void updateFrame()
    {
        Vector3f negGaze = new Vector3f(eye);
        negGaze.sub(target);
        negGaze.normalize();

        up.normalize();
        right.cross(up, negGaze);
        right.normalize();
        up.cross(negGaze, right);
    }

    @Override
    public void getLineThroughNDC(Vector2f imageXY, Vector3f p0,
            Vector3f p1)
    {
        p0.set(getEye());
        p1.scaleAdd(imageXY.x * aspect * getHeight(), getRight(), p1);
        p1.scaleAdd(imageXY.y * getHeight(), getUp(), p1);

        p1.set(getTarget());
        p1.scaleAdd(imageXY.x * aspect * getHeight(), getRight(), p1);
        p1.scaleAdd(imageXY.y * getHeight(), getUp(), p1);
    }

    @Override
    public Vector3f NDCToWorldAt(Vector2f ndc, Vector3f planePosition)
    {
        Vector3f gaze = new Vector3f();
        gaze.set(getTarget());
        gaze.sub(eye);
        gaze.normalize();

        Vector3f eyeToPlanePosition = new Vector3f();
        eyeToPlanePosition.set(planePosition);
        eyeToPlanePosition.sub(eye);

        float dist = eyeToPlanePosition.dot(gaze);
        float height = getHeight();

        gaze.scale(dist);
        Vector3f out = new Vector3f();
        out.scale(ndc.x * aspect * height, right);
        out.scaleAdd(ndc.y * height, up, out);
        out.add(gaze);
        return out;
    }
}
