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
import java.nio.Buffer;

public class Texture1D extends Texture {    
    private int width;
    private boolean allocated = false;

    public Texture1D(GL2 gl) {
        super(gl, GL2.GL_TEXTURE_1D, GL2.GL_RGBA);
    }

    public Texture1D(GL2 gl, int internalFormat) {
        super(gl, GL2.GL_TEXTURE_1D, internalFormat);
    }

    public void setData(int width, int format, int type, Buffer buffer) {
        this.width = width;

        Texture oldTexture = TextureUnit.getActiveTextureUnit(gl).getBoundTexture();
        if (oldTexture != this) {
            bind();
        }        

        if (buffer != null) {
            buffer.rewind();            
        }
        gl.glTexImage1D(target, 0, internalFormat, width, 0, format, type, buffer);

        if (oldTexture == null) {
            unbind();
        } else if (oldTexture != this) {
            oldTexture.bind();
        }

        allocated = true;
    }

    public void allocate(int width, int format, int type) {
        setData(width, format, type, null);
    }

    public void allocate(int width, int height) { setData(width, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null); }
    
    public boolean isAllocated() {
    	return allocated;
    }
    
    public int getWidth() {
    	return width;
    }
}
