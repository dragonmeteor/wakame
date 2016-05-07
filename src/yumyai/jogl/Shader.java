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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.media.opengl.GL2;

public abstract class Shader implements GLResource {
    // Check whether the GLSL vertex and fragment shaders are supported
    public static Boolean checkGlslSupport(GL2 gl) {
        if (!gl.isExtensionAvailable("GL_ARB_vertex_shader")
                || !gl.isExtensionAvailable("GL_ARB_fragment_shader")) {

            System.err.println("GLSL is not supported!");
            return false;

        } else {
            System.out.println("GLSL is supported!");
            return true;
        }
    }

    public static String getInfoLog(GL2 gl, int objectId) {
        int[] buf = new int[1];

        // Retrieve the log length
        gl.glGetObjectParameterivARB(objectId,
                GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, buf, 0);

        int logLength = buf[0];

        if (logLength <= 1) {
            return "";
        } else {
            // Retrieve the log message
            byte[] content = new byte[logLength + 1];
            gl.glGetInfoLogARB(objectId, logLength, buf, 0, content, 0);

            return new String(content);
        }
    }

    // ************* Private variables *************
    private final int type; // GL2.GL_FRAGMENT_SHADER or GL2.GL_VERTEX_SHADER
    private int id;
    private GL2 gl;
    private boolean disposed;

    /**
     * Create a shader from a source code text.
     *
     * @param shaderType the type of the shader
     * @param gl         the OpenGL context
     * @param src        the source code
     * @param fileName   the name of the file that contains the source code.
     * @throws GlslException
     */
    public Shader(int shaderType, GL2 gl,
                  String src, String fileName) throws GlslException {
        this.type = shaderType;
        this.gl = gl;

        this.id = this.gl.glCreateShaderObjectARB(this.type);

        setSource(src);

        if (!compile()) {
            if (src != null) {
                throw new GlslException("Compiliation error in " + fileName + "\n"
                        + getInfoLog(this.gl, this.id));
            } else {
                throw new GlslException("Compilation error "
                        + getInfoLog(this.gl, this.id));
            }
        }
    }

    public Shader(int shaderType, GL2 gl, String src) {
        this(shaderType, gl, src, null);
    }

    public int getId() {
        return this.id;
    }

    public void disposeGL() {
        if (!disposed) {
            gl.glDeleteShader(this.id);
            disposed = true;
        }
    }

    private void setSource(String source) {
        // Attach the GLSL source code
        gl.glShaderSourceARB(this.id, 1,
                new String[]
                        {
                                source
                        },
                new int[]
                        {
                                source.length()
                        }, 0);
    }

    private Boolean compile() {
        // Try to compile the GLSL source code
        gl.glCompileShaderARB(this.id);

        // Check the compilation status
        int[] compileCheck = new int[1];
        this.gl.glGetObjectParameterivARB(this.id,
                GL2.GL_OBJECT_COMPILE_STATUS_ARB, compileCheck, 0);

        return compileCheck[0] == GL2.GL_TRUE;
    }
}
