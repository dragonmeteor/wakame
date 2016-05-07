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

package yumyai.jogl;

import javax.media.opengl.GL2;

public class Vao implements GLResource {
    private int id;
    private GL2 gl;
    private boolean disposed = false;
    private boolean bound = false;

    public Vao(GL2 gl) {
        int[] ids = new int[1];
        gl.glGenVertexArrays(1, ids, 0);
        id = ids[0];
        this.gl = gl;
    }

    public void bind() {
        gl.glBindVertexArray(id);
        bound = true;
    }

    public void unbind() {
        gl.glBindVertexArray(0);
        bound = false;
    }

    public boolean isBound() {
        return bound;
    }


    @Override
    public void disposeGL() {
        if (!disposed) {
            if (isBound())
                unbind();

            int idv[] = new int[1];
            idv[0] = id;
            gl.glDeleteVertexArrays(1, idv, 0);
            disposed = true;
        }
    }
}
