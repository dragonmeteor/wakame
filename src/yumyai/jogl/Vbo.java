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

/*
 */
package yumyai.jogl;

import java.nio.ByteBuffer;
import javax.media.opengl.GL2;

public class Vbo implements GLResource {
    private int id;
    private GL2 gl;
    private VboTarget target;
    private boolean disposed;

    public Vbo(GL2 gl, VboTarget target) {
        this.gl = gl;
        this.target = target;
        int[] idv = new int[1];
        gl.glGenBuffers(1, idv, 0);
        id = idv[0];
    }

    public GL2 getGL() {
        return gl;
    }

    public void bind() {
        if (target.getBoundVbo() != null) {
            target.getBoundVbo().unbind();
        }
        gl.glBindBuffer(target.getConstant(), id);
        target.setBoundVbo(this);
    }

    public void unbind() {
        if (target.getBoundVbo() == this) {
            gl.glBindBuffer(target.getConstant(), 0);
            target.setBoundVbo(null);
        }
    }

    public void use() {
        bind();
    }

    public void unuse() {
        unbind();
    }

    public boolean isBound() {
        return target.getBoundVbo() == this;
    }

    public int getId() {
        return id;
    }

    public void setData(ByteBuffer buffer) {
        setData(buffer.capacity(), buffer);
    }

    public void setData(int width, ByteBuffer buffer) {
        bind();
        buffer.rewind();
        gl.glBufferData(target.getConstant(), width, buffer, GL2.GL_STATIC_DRAW);
        unbind();
    }

    public void disposeGL() {
        if (!disposed) {
            if (isBound())
                unbind();

            int idv[] = new int[1];
            idv[0] = id;
            gl.glDeleteBuffers(1, idv, 0);
            disposed = true;
        }
    }
}
