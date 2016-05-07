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

import com.jogamp.opengl.util.texture.TextureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.opengl.GL2;
import java.nio.Buffer;

public class TextureCubeMap extends Texture {
    private static Logger logger = LoggerFactory.getLogger(TextureCubeMap.class);
    private int size = 0;
    private boolean allocated = false;

    public TextureCubeMap(GL2 gl, int internalFormat) {
        super(gl, GL2.GL_TEXTURE_CUBE_MAP, internalFormat);
        wrapS = GL2.GL_CLAMP_TO_EDGE;
        wrapT = GL2.GL_CLAMP_TO_EDGE;
        wrapR = GL2.GL_CLAMP_TO_EDGE;
        minFilter = GL2.GL_LINEAR;
        magFilter = GL2.GL_LINEAR;
    }

    public int getSize() {
        return size;
    }

    public void allocate(int size, int format, int type) {
        this.size = size;
        Texture oldTexture = TextureUnit.getActiveTextureUnit(gl).getBoundTexture();
        if (oldTexture != this) {
            bind();
        }
        bind();
        /* Allocate space for all cube map faces. */
        for (int i=0; i<6; i++) {
            gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat,
                    size, size, 0, format, type, null);
        }
        if (oldTexture == null) {
            unbind();
        } else if (oldTexture != this) {
            oldTexture.bind();
        }
        allocated = true;
    }

    public void allocate(int size) {
        allocate(size, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE);
    }

    public void setImages(int size, int format, int type, Buffer[] buffers) {
        this.size = size;

        Texture oldTexture = TextureUnit.getActiveTextureUnit(gl).getBoundTexture();
        if (oldTexture != this) {
            bind();
        }

        for (int i = 0; i < 6; i++) {
            setImage(i, size, format, type, buffers[i]);
        }

        if (oldTexture == null) {
            unbind();
        } else if (oldTexture != this) {
            oldTexture.bind();
        }

        allocated = true;
    }

    private void setImage(int side, int size, int format, int type, Buffer buffer) {
        logger.debug("side = " + side);
        logger.debug("size = " + size);
        logger.debug("format = " + format);
        logger.debug("type = " + type);

        if (buffer != null) {
            buffer.rewind();
            logger.debug("buffer size = " + buffer.capacity());
        }
        gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X + side, 0, internalFormat, size, size, 0,
                format, type, buffer);
    }

    public void setImages(TextureData[] datas) {
        this.size = datas[0].getWidth();
        for (int i = 0; i < 6; i++) {
            if (datas[i].getWidth() != size || datas[i].getHeight() != size) {
                throw new RuntimeException("texture data not of the right size");
            }
        }

        Texture oldTexture = TextureUnit.getActiveTextureUnit(gl).getBoundTexture();
        if (oldTexture != this) {
            bind();
        }

        for (int i = 0; i < 6; i++) {
            setImage(i, datas[i].getHeight(), datas[i].getPixelFormat(), datas[i].getPixelType(), datas[i].getBuffer());
        }

        if (oldTexture == null) {
            unbind();
        } else if (oldTexture != this) {
            oldTexture.bind();
        }
        allocated = true;
    }
}
