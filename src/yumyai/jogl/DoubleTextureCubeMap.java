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

public class DoubleTextureCubeMap implements GLResource {
    private TextureCubeMap[] buffers;
    private int readIndex = 0;

    public DoubleTextureCubeMap(GL2 gl) {
        this(gl, GL2.GL_RGBA);
    }

    public DoubleTextureCubeMap(GL2 gl, int internalFormat) {
        buffers = new TextureCubeMap[2];
        for (int i = 0; i < 2; i++) {
            buffers[i] = new TextureCubeMap(gl, internalFormat);
        }
    }

    public void allocate(int size, int format, int type) {
        for (int i = 0; i < 2; i++) {
            buffers[i].allocate(size, format, type);
        }
    }

    @Override
    public void disposeGL() {
        for (int i = 0; i < 2; i++) {
            buffers[i].disposeGL();
        }
    }

    public void swap() {
        readIndex = (readIndex + 1) % 2;
    }

    public TextureCubeMap getReadBuffer() {
        return buffers[readIndex];
    }

    public TextureCubeMap getWriteBuffer() {
        return buffers[(readIndex+1)%2];
    }

    public int getSize() {
        return buffers[0].getSize();
    }
}
