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

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

public class GLViewUtil {
    public static GLCapabilities getDefaultCapabilities() {
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCapabilities.setAlphaBits(8);
        glCapabilities.setSampleBuffers(true);
        glCapabilities.setNumSamples(1);
        return glCapabilities;
    }

    public static GLView create() {
        return create(getDefaultCapabilities(), 60);
    }

    public static GLView create(float initialFrameRate) {
        return create(getDefaultCapabilities(), initialFrameRate);
    }

    public static GLView create(GLCapabilities glCapabilities) {
        return create(glCapabilities, 60);
    }

    public static GLView create(GLCapabilities glCapabilities, float initialFrameRate) {
        GLView glView;
        if (System.getProperty("os.name").startsWith("Windows") || System.getProperty("os.name").startsWith("Linux")) {
            glView = new GLViewWindows(glCapabilities, initialFrameRate);
        } else {
            glView = new GLViewMac(glCapabilities, initialFrameRate);
        }
        return glView;
    }
}
