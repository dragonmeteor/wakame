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

import java.nio.Buffer;
import javax.media.opengl.GL2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureTwoDim extends Texture {
    private static Logger logger = LoggerFactory.getLogger(TextureTwoDim.class);
    protected int width;
    protected int height;
    protected boolean allocated;
    protected boolean textureHasMipmap;

    public TextureTwoDim(GL2 gl, int target, int internalFormat) {
        this(gl, target, internalFormat, true);
    }

    public TextureTwoDim(GL2 gl, int target, int internalFormat, boolean hasMipmap) {
        super(gl, target, internalFormat);
        allocated = false;
        if (hasMipmap) {
            minFilter = GL2.GL_LINEAR_MIPMAP_LINEAR;
            magFilter = GL2.GL_LINEAR;
        } else {
            minFilter = GL2.GL_NEAREST;
            magFilter = GL2.GL_NEAREST;
        }
        this.textureHasMipmap = hasMipmap;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setImage(int width, int height, int format, int type, Buffer buffer) {
        this.width = width;
        this.height = height;

        Texture oldTexture = TextureUnit.getActiveTextureUnit(gl).getBoundTexture();
        if (oldTexture != this) {
            bind();
        }

        /*
        logger.debug("width = " + width);
        logger.debug("height = " + height);
        logger.debug("format = " + format);
        logger.debug("type = " + type);
        */

        if (buffer != null) {
            buffer.rewind();
            //logger.debug("buffer size = " + buffer.capacity());
        }

        gl.glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, buffer);
        if (textureHasMipmap) {
            gl.glGenerateMipmap(target);
            gl.glTexParameteri(target, GL2.GL_GENERATE_MIPMAP, GL2.GL_TRUE);
        } else {
            gl.glTexParameteri(target, GL2.GL_GENERATE_MIPMAP, GL2.GL_FALSE);
        }

        if (oldTexture == null) {
            unbind();
        } else if (oldTexture != this) {
            oldTexture.bind();
        }

        allocated = true;
    }

    public void setImage(TextureData data) {
        setImage(data.getWidth(), data.getHeight(), data.getPixelFormat(), data.getPixelType(), data.getBuffer());
    }

    public void getImage(int width, int height, int format, int type, Buffer buffer) {
        this.width = width;
        this.height = height;

        Texture oldTexture = TextureUnit.getActiveTextureUnit(gl).getBoundTexture();
        if (oldTexture != this) {
            bind();
        }

        logger.debug("width = " + width);
        logger.debug("height = " + height);
        logger.debug("format = " + format);
        logger.debug("type = " + type);

        if (buffer != null) {
            buffer.rewind();
            logger.debug("buffer size = " + buffer.capacity());
        }

        gl.glGetTexImage(target, 0, format, type, buffer);

        if (oldTexture == null) {
            unbind();
        } else if (oldTexture != this) {
            oldTexture.bind();
        }
    }

    public void getImage(TextureData data) {
        getImage(data.getWidth(), data.getHeight(), data.getPixelFormat(), data.getPixelType(), data.getBuffer());
    }

    protected void allocate(int width, int height, int format, int type) {
        setImage(width, height, format, type, null);
    }

    protected void allocate(int width, int height) {
        setImage(width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
    }

    public boolean isAllocated() {
        return allocated;
    }

    public boolean hasMipmap() { return textureHasMipmap; }
}
