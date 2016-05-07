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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.opengl.GL2;

public class TextureUnit {
    private static boolean initialized = false;
    private static TextureUnit[] instances;
    private static int count;
    private static Logger logger = LoggerFactory.getLogger(TextureUnit.class);

    private static synchronized void initialize(GL2 gl) {
        if (!initialized) {
            int countv[] = new int[1];
            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_IMAGE_UNITS, countv, 0);
            count = countv[0];
            logger.info(String.format("number of texture units = %d", count));

            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_SIZE, countv, 0);
            count = countv[0];
            logger.info(String.format("max texture size = %d", count));

            gl.glGetIntegerv(GL2.GL_MAX_VERTEX_ATTRIBS, countv, 0);
            count = countv[0];
            logger.info(String.format("max vertex attribs = %d", count));

            instances = new TextureUnit[count];
            for (int i = 0; i < count; i++) {
                instances[i] = new TextureUnit(gl, i);
            }

            instances[0].activate();
            initialized = true;
        }
    }

    public static TextureUnit getTextureUnit(GL2 gl, int i) {
        if (!initialized) {
            initialize(gl);
        }
        return instances[i];
    }

    public static int getActiveTextureUnitId(GL2 gl) {
        int activeId[] = new int[1];
        gl.glGetIntegerv(GL2.GL_ACTIVE_TEXTURE, activeId, 0);
        return activeId[0];
    }

    public static TextureUnit getActiveTextureUnit(GL2 gl) {
        if (!initialized) {
            initialize(gl);
        }

        int activeId = getActiveTextureUnitId(gl);
        return instances[activeId - GL2.GL_TEXTURE0];
    }

    private TextureUnit(GL2 gl, int i) {
        index = i;
        id = GL2.GL_TEXTURE0 + index;
        boundTexture = null;
        this.gl = gl;
    }

    protected void activate() {
        gl.glActiveTexture(id);
    }

    private TextureUnit activateAndReturnLastActive() {
        TextureUnit last = getActiveTextureUnit(gl);
        if (this != last) {
            activate();
            return last;
        } else {
            return this;
        }
    }

    public void bindToUniform(Uniform target) {
        target.set1Int(id - GL2.GL_TEXTURE0);
    }

    void bindTexture(Texture texture) {
        if (boundTexture != null && boundTexture != texture) {
            boundTexture.unbind();
        }
        if (boundTexture != texture) {
            TextureUnit last = activateAndReturnLastActive();
            texture.enable();
            gl.glBindTexture(texture.getTarget(), texture.getId());
            boundTexture = texture;

            if (last != this) {
                last.activate();
            }
        }
    }

    void unbindTexture() {
        if (boundTexture != null) {
            boundTexture.unbind();
            boundTexture.disable();
        }
    }

    void unbindTexture(Texture texture) {
        if (boundTexture == texture) {
            TextureUnit last = activateAndReturnLastActive();

            gl.glBindTexture(texture.getTarget(), 0);
            boundTexture = null;

            if (last != this) {
                last.activate();
            }
        }
    }

    public boolean isActive() {
        return id == getActiveTextureUnitId(gl);
    }

    public int getIndex() {
        return index;
    }

    public int getId() {
        return id;
    }

    public Texture getBoundTexture() {
        return boundTexture;
    }

    private int index;
    private int id;
    private Texture boundTexture;
    private GL2 gl;
}
