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

import javax.media.opengl.GL2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fbo implements GLResource {
	private static Logger logger = LoggerFactory.getLogger(Program.class);
	
	/**
	 * Static members.
	 */
	private static boolean staticInitialized = false;
	private static int numColorAttachements;
	private Fbo boundFbo = null;
	
	/**
	 * Constants for different color attachments.
	 */
	private static int[] COLOR_ATTACHMENTS = { GL2.GL_COLOR_ATTACHMENT0,
			GL2.GL_COLOR_ATTACHMENT1, GL2.GL_COLOR_ATTACHMENT2,
			GL2.GL_COLOR_ATTACHMENT3, GL2.GL_COLOR_ATTACHMENT4,
			GL2.GL_COLOR_ATTACHMENT5, GL2.GL_COLOR_ATTACHMENT6,
			GL2.GL_COLOR_ATTACHMENT7, GL2.GL_COLOR_ATTACHMENT8,
			GL2.GL_COLOR_ATTACHMENT9, GL2.GL_COLOR_ATTACHMENT10,
			GL2.GL_COLOR_ATTACHMENT11, GL2.GL_COLOR_ATTACHMENT12,
			GL2.GL_COLOR_ATTACHMENT13, GL2.GL_COLOR_ATTACHMENT14,
			GL2.GL_COLOR_ATTACHMENT15 };
	
	/**
	 * Instance members
	 */
	private GL2 gl;
	private int id;
	private boolean disposed = false;
	private Texture[] colorAttachements;
	private int[] colorAttachmentTargets;
	private Texture depthAttachment;
	private int depthAttachmentTarget;
	private Texture stencilAttachment;
	private int stencilAttachmentTarget;
	private boolean bound = false;

	public static void staticInitialize(GL2 gl) {
		if (!staticInitialized) {
			int[] temp = new int[1];
			gl.glGetIntegerv(GL2.GL_MAX_COLOR_ATTACHMENTS, temp, 0);
			numColorAttachements = temp[0];
			logger.info("num color attachement = " + temp[0]);
			staticInitialized = true;
		}
	}

	public Fbo(GL2 gl) {
		staticInitialize(gl);

		this.gl = gl;
		int[] ids = new int[1];
		gl.glGenFramebuffers(1, ids, 0);
		this.id = ids[0];

		colorAttachements = new Texture[numColorAttachements];
		colorAttachmentTargets = new int[numColorAttachements];
	}

	public int getId() {
		return id;
	}

	public void bind() {
		if (boundFbo != null) {
			boundFbo.unbind();
		}
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, id);
		boundFbo = this;
		bound = true;
	}

	public void unbind() {
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		boundFbo = null;
		bound = false;
	}

	public boolean isBound() {
		return bound;
	}

	public void disposeGL() {
		unbind();
		if (!disposed) {
			int idv[] = new int[1];
			idv[0] = id;
			gl.glDeleteFramebuffers(1, idv, 0);
			disposed = true;
		}
	}

	public Texture getColorAttachements(int index) {
		return colorAttachements[index];
	}

	public int getColorAttachmentTarget(int index) {
		return colorAttachmentTargets[index];
	}

	public Texture getDepthAttachement() {
		return depthAttachment;
	}

	public int getDepthAttachmentTarget(int index) {
		return depthAttachmentTarget;
	}

	public Texture getStencilAttachment() {
		return stencilAttachment;
	}

	public int getStencilAttachmentTarget() {
		return stencilAttachmentTarget;
	}

	public Fbo getBoundFbo() {
		return boundFbo;
	}

	public void checkBound() {
		if (!isBound()) {
			throw new RuntimeException("the fbo is not bound");
		}
	}

	public void attachColorBuffer(int index, Texture texture) {
		attachColorBuffer(index, texture.getTarget(), texture);
	}

	public void attachColorBuffer(int index, int target, Texture texture) {
		checkBound();
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, COLOR_ATTACHMENTS[index],
				target, texture.getId(), 0);
		colorAttachements[index] = texture;
		colorAttachmentTargets[index] = target;
	}

	public void detachColorBuffer(int index) {
		checkBound();
		if (colorAttachements[index] != null) {
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER,
					COLOR_ATTACHMENTS[index],
					colorAttachmentTargets[index], 0, 0);
			colorAttachements[index] = null;
			colorAttachmentTargets[index] = 0;
		}
	}

	public void detachColorBuffer(int index, int target) {
		checkBound();
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, COLOR_ATTACHMENTS[index],
				target, 0, 0);
		colorAttachements[index] = null;
		colorAttachmentTargets[index] = 0;
	}

	public void attachDepthBuffer(Texture texture) {
		attachDepthBuffer(texture.getTarget(), texture);
	}

	public void attachDepthBuffer(int target, Texture texture) {
		checkBound();
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT,
				target, texture.getId(), 0);
		depthAttachment = texture;
		depthAttachmentTarget = target;
	}

	public void detachDepthTexture() {
		checkBound();
		if (depthAttachment != null) {
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER,
					GL2.GL_DEPTH_ATTACHMENT, depthAttachmentTarget, 0, 0);
			depthAttachment = null;
			depthAttachmentTarget = 0;
		}
	}

	public void detachDepthTexture(int target) {
		checkBound();
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT,
				target, 0, 0);
		depthAttachment = null;
		depthAttachmentTarget = 0;
	}

	public void attachStencilBuffer(Texture texture) {
		attachStencilBuffer(texture.getTarget(), texture);
	}

	public void attachStencilBuffer(int target, Texture texture) {
		checkBound();
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER,
				GL2.GL_STENCIL_ATTACHMENT, target,
				texture.getId(), 0);
		stencilAttachment = texture;
		stencilAttachmentTarget = target;
	}

	public void detachStencilTexture() {
		checkBound();
		if (stencilAttachment != null) {
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER,
					GL2.GL_STENCIL_ATTACHMENT, stencilAttachmentTarget,
					0, 0);
			stencilAttachment = null;
			stencilAttachmentTarget = 0;
		}
	}

	public void detachStencilTexture(int target) {
		checkBound();
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT,
				target, 0, 0);
		stencilAttachment = null;
		stencilAttachmentTarget = 0;
	}

	public void drawToNone() {
		checkBound();
		gl.glDrawBuffer(GL2.GL_NONE);
	}

	public void readFromNone() {
		checkBound();
		gl.glReadBuffer(GL2.GL_NONE);
	}

	public void drawTo(int start, int count) {
		checkBound();
		gl.glDrawBuffers(count, COLOR_ATTACHMENTS, start);
	}

	public void readFrom(int index) {
		checkBound();
		gl.glReadBuffer(COLOR_ATTACHMENTS[index]);
	}

	public void drawTo(Texture t0) {
		checkBound();
		attachColorBuffer(0, t0);
		drawTo(0, 1);
	}

	public void drawTo(Texture t0, Texture t1) {
		checkBound();
		attachColorBuffer(0, t0);
		attachColorBuffer(1, t1);
		drawTo(0, 2);
	}

	public void drawTo(Texture t0, Texture t1, Texture t2) {
		checkBound();
		attachColorBuffer(0, t0);
		attachColorBuffer(1, t1);
		attachColorBuffer(2, t2);
		drawTo(0, 3);
	}

	public void drawTo(Texture t0, Texture t1, Texture t2, Texture t3) {
		checkBound();
		attachColorBuffer(0, t0);
		attachColorBuffer(1, t1);
		attachColorBuffer(2, t2);
		attachColorBuffer(3, t3);
		drawTo(0, 4);
	}

	public void detachAllColorBuffers() {
		checkBound();
		for (int i = 0; i < numColorAttachements; i++) {
			if (colorAttachements[i] != null) {
				detachColorBuffer(i);
			}
		}
	}

	public void detachAll() {
		checkBound();
		detachAllColorBuffers();
		detachDepthTexture();
	}

	public static void checkStatus(GL2 gl) {
		int status = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		switch (status) {
		case GL2.GL_FRAMEBUFFER_COMPLETE:
			return;
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
			throw new RuntimeException(
					"frame buffer incomplete: incomplete attachement");
		case GL2.GL_FRAMEBUFFER_UNSUPPORTED:
			throw new RuntimeException("Unsupported frame buffer format");
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
			throw new RuntimeException(
					"frame buffer incomplete: missing attachment");
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
			throw new RuntimeException(
					"frame buffer incomplete: attached images must have same dimensions");
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
			throw new RuntimeException(
					"frame buffer incomplete: attached images must have same format");
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
			throw new RuntimeException(
					"frame buffer incomplete: missing draw buffer");
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
			throw new RuntimeException(
					"frame buffer incomplete: missing read buffer");
		}
	}
}
