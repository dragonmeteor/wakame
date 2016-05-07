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


import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TextureRect extends TextureTwoDim {

    public TextureRect(GL2 gl) {
        super(gl, GL2.GL_TEXTURE_RECTANGLE, GL2.GL_RGBA8, false);
        wrapS = GL2.GL_CLAMP_TO_EDGE;
        wrapT = GL2.GL_CLAMP_TO_EDGE;
        wrapR = GL2.GL_CLAMP_TO_EDGE;
        magFilter = GL2.GL_NEAREST;
        minFilter = GL2.GL_NEAREST;
    }

    public TextureRect(GL2 gl, int internalFormat) {
        super(gl, GL2.GL_TEXTURE_RECTANGLE, internalFormat, false);
        wrapS = GL2.GL_CLAMP_TO_EDGE;
        wrapT = GL2.GL_CLAMP_TO_EDGE;
        wrapR = GL2.GL_CLAMP_TO_EDGE;
        magFilter = GL2.GL_NEAREST;
        minFilter = GL2.GL_NEAREST;
    }

    public TextureRect(GL2 gl, String filename) throws IOException {
        this(gl, filename, GL2.GL_RGBA8);
    }

    public TextureRect(GL2 gl, String filename, int internalFormat) throws IOException {
        this(gl, new File(filename), internalFormat);
    }

    public TextureRect(GL2 gl, File file) throws IOException {
        this(gl, file, GL2.GL_RGBA8);
    }

    public TextureRect(GL2 gl, File file, int internalFormat) throws IOException {
        super(gl, GL2.GL_TEXTURE_2D, internalFormat, false);
        TextureData data = null;
        if (FilenameUtils.getExtension(file.getAbsolutePath()).toLowerCase().equals("png")) {
            BufferedImage image = ImageIO.read(file);
            ImageUtil.flipImageVertically(image);
            data = AWTTextureIO.newTextureData(GLProfile.getDefault(), image, false);
        } else {
            data = TextureIO.newTextureData(GLProfile.getDefault(), file, false, null);
            if (data.getMustFlipVertically()) {
                BufferedImage image = ImageIO.read(file);
                ImageUtil.flipImageVertically(image);
                data = AWTTextureIO.newTextureData(GLProfile.getDefault(), image, false);
            }
        }
        wrapS = GL2.GL_CLAMP_TO_EDGE;
        wrapT = GL2.GL_CLAMP_TO_EDGE;
        wrapR = GL2.GL_CLAMP_TO_EDGE;
        setImage(data);
    }

    @Override
    public void allocate(int width, int height, int format, int type) {
        super.allocate(width, height, format, type);
    }

    @Override
    public void allocate(int width, int height) { super.allocate(width, height); }
}