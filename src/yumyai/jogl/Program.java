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
import yondoko.util.IOUtil;

import java.util.ArrayList;
import java.util.HashMap;
import javax.media.opengl.GL2;

public class Program implements GLResource {
	private static Logger logger = LoggerFactory.getLogger(Program.class);
	private static Program current = null;	

	public static boolean isAProgramInUse() {
		return current != null;
	}

	public static Program getCurrent() {
		return current;
	}

	public static void unuseProgram(GL2 gl) {
		gl.glUseProgram(0);
		current = null;
	}

	private int id;
	private VertexShader vertexShader;
	private FragmentShader fragmentShader;
	private GL2 gl;
	private HashMap<String, Uniform> uniforms;
	private HashMap<String, Attribute> attributes;
	private boolean disposed = false;

	public Program(GL2 gl, VertexShader vertexShader,
			FragmentShader fragmentShader) {
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		this.gl = gl;

		this.id = gl.glCreateProgramObjectARB();

		buildProgram();

		initializeUniforms();
	}

    public Program(GL2 glContext, String vertexSrc, String fragmentSrc) {
        this(glContext, vertexSrc, null, fragmentSrc, null);
    }

	public Program(GL2 glContext, String vertexSrc, String vertexSrcFile, String fragmentSrc, String fragmentSrcFile) {
		this.vertexShader = null;
		this.fragmentShader = null;
		this.gl = glContext;

		this.id = gl.glCreateProgram();

		this.vertexShader = new VertexShader(this.gl, vertexSrc, vertexSrcFile);
		this.fragmentShader = new FragmentShader(this.gl, fragmentSrc, fragmentSrcFile);

		logger.debug("vertex source file = " + vertexSrcFile);
		logger.debug("fragment source file " + fragmentSrcFile);
		buildProgram();
		initializeUniforms();
		initializeAttributes();
	}

	public static Program createFromFile(GL2 gl, String vertexSrcFile, String fragmentSrcFile) {
		try {
			String vertexSrc = IOUtil.readTextFile(vertexSrcFile);
			String fragmentSrc = IOUtil.readTextFile(fragmentSrcFile);
			return new Program(gl, vertexSrc, vertexSrcFile, fragmentSrc, fragmentSrcFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int getId() {
		return this.id;
	}

	public Boolean isUsed() {
		return current == this;
	}

	public void use() {
		this.gl.glUseProgram(this.id);
		current = this;
	}

	public void unuse() {
		for (Attribute attrib : attributes.values()) {
			if (attrib.isEnabled()) {
				attrib.disable();
			}
		}
		unuseProgram(this.gl);
	}

	public HashMap<String, Uniform> getUniforms() {
		return this.uniforms;
	}

	public Uniform getUniform(String name) {
		return uniforms.get(name);
	}

	public boolean hasUniform(String name) {
		return uniforms.containsKey(name);
	}

	public HashMap<String, Attribute> getAttributes() {
		return this.attributes;
	}

	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}

	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	public void disposeGL() {
		if (!disposed) {
			vertexShader.disposeGL();
			fragmentShader.disposeGL();
			gl.glDeleteProgram(id);
			disposed = true;
		}
	}

	protected void buildProgram() {
		// Attach the vertex shader
		this.gl.glAttachShader(this.id, this.vertexShader.getId());

		// Attach the fragment shader
		this.gl.glAttachShader(this.id, this.fragmentShader.getId());

		gl.glLinkProgram(this.id);

		// Check the linking status
		int[] linkCheck = new int[1];
		gl.glGetProgramiv(this.id, GL2.GL_OBJECT_LINK_STATUS_ARB, linkCheck, 0);

		if (linkCheck[0] == GL2.GL_FALSE) {
			throw new GlslException("Link error "
					+ Shader.getInfoLog(this.gl, this.id));
		}
	}

	// ************* Private functions *************
	private void initializeUniforms() {
		this.uniforms = new HashMap<String, Uniform>();

		int[] uniformCount = new int[1];
		this.gl.glGetProgramiv(this.id, GL2.GL_ACTIVE_UNIFORMS, uniformCount, 0);

		//System.err.print("GLSL uniforms: ");
		for (int uniform_index = 0; uniform_index < uniformCount[0]; uniform_index++) {
			Uniform currUniform = new Uniform(this.gl, this, uniform_index);

			if (!currUniform.getName().startsWith("gl_")) {
				//System.err.print(currUniform.getName() + " ");
				this.uniforms.put(currUniform.getName(), currUniform);
			}
		}

		// Create an instance for all the array entries.
		ArrayList<Uniform> newUniforms = new ArrayList<Uniform>();
        ArrayList<Uniform> toRemove = new ArrayList<Uniform>();
		for (Uniform uniform : this.uniforms.values()) {
			if (uniform.getSize() > 1) {
				String baseName = uniform.getName().substring(0, uniform.getName().length()-3);
                int start = (baseName.endsWith("]")) ? 1 : 0;
				for (int i = start; i < uniform.getSize(); i++) {
					Uniform newUniform = new Uniform(gl, this);
					newUniform.name = baseName + "[" + Integer.toString(i) + "]";
					newUniform.size = 1;
					newUniform.type = uniform.type;
					newUniform.location = uniform.location + i;
					newUniform.isRowMajor = uniform.isRowMajor;
					newUniforms.add(newUniform);
					if (gl.glGetUniformLocation(this.getId(), newUniform.name) != newUniform.location) {
						throw new RuntimeException("uniform '" + newUniform.name + "' location not matching the OpenGL assigned location");
					}
				}
                if (start == 0)
                    toRemove.add(uniform);
			}
		}
        for(Uniform uniform : toRemove) {
            this.uniforms.remove(uniform.getName());
        }
		for(Uniform uniform : newUniforms) {
			this.uniforms.put(uniform.getName(), uniform);
		}
	}

	private void initializeAttributes() {
		this.attributes = new HashMap<String, Attribute>();

		int[] attribCount = new int[1];
		this.gl.glGetProgramiv(this.id, GL2.GL_ACTIVE_ATTRIBUTES, attribCount,
				0);

		for (int attribIndex = 0; attribIndex < attribCount[0]; attribIndex++) {
			Attribute currentAttrib = new Attribute(this.gl, this, attribIndex);
			if (!currentAttrib.getName().startsWith("gl_")) {
				this.attributes.put(currentAttrib.getName(), currentAttrib);
			}
		}
	}

	public Program setUniform(String name, int x) {
		if (hasUniform(name)) {
			getUniform(name).set1Int(x);
		}
		return this;
	}

	public Program setUniform(String name, int x1, int x2) {
		if (hasUniform(name)) {
			getUniform(name).set2Int(x1, x2);
		}
		return this;
	}

	public Program setUniform(String name, int x1, int x2, int x3) {
		if (hasUniform(name)) {
			getUniform(name).set3Int(x1, x2, x3);
		}
		return this;
	}

	public Program setUniform(String name, int x1, int x2, int x3, int x4) {
		if (hasUniform(name)) {
			getUniform(name).set4Int(x1, x2, x3, x4);
		}
		return this;
	}

	public Program setUniform(String name, boolean x) {
		if (hasUniform(name)) {
			getUniform(name).set1Int((x) ? 1 : 0);
		}
		return this;
	}

	public Program setUniform(String name, float x) {
		if (hasUniform(name)) {
			getUniform(name).set1Float(x);
		}
		return this;
	}

	public Program setUniform(String name, float x1, float x2) {
		if (hasUniform(name)) {
			getUniform(name).set2Float(x1, x2);
		}
		return this;
	}

	public Program setUniform(String name, float x1, float x2, float x3) {
		if (hasUniform(name)) {
			getUniform(name).set3Float(x1, x2, x3);
		}
		return this;
	}

	public Program setUniform(String name, float x1, float x2, float x3, float x4) {
		if (hasUniform(name)) {
			getUniform(name).set4Float(x1, x2, x3, x4);
		}
		return this;
	}

	public Program setUniform(String name, javax_.vecmath.Matrix4f m) {
		if (hasUniform(name)) {
			getUniform(name).setMatrix4(m);
		}
		return this;
	}

	public Program setUniform(String name, javax_.vecmath.Matrix3f m) {
		if (hasUniform(name)) {
			getUniform(name).setMatrix3(m);
		}
		return this;
	}

	public Program setUniform(String name, javax_.vecmath.Tuple2f x) {
		if (hasUniform(name)) {
			getUniform(name).setTuple2(x);
		}
		return this;
	}

	public Program setUniform(String name, javax_.vecmath.Tuple3f x) {
		if (hasUniform(name)) {
			getUniform(name).setTuple3(x);
		}
		return this;
	}

	public Program setUniform(String name, javax_.vecmath.Tuple4f x) {
		if (hasUniform(name)) {
			getUniform(name).setTuple4(x);
		}
		return this;
	}
}
