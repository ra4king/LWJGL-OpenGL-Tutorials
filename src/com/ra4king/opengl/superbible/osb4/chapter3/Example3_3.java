package com.ra4king.opengl.superbible.osb4.chapter3;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

public class Example3_3 extends GLProgram {
	public static void main(String[] args) {
		new Example3_3().run();
	}

	public Example3_3() {
		super("POINTSZ", 800, 600, true);
	}

	FloatBuffer sizes = BufferUtils.createFloatBuffer(16);
	private float xRot, yRot;

	public void init() {
		glClearColor(0, 0, 0, 1);

		glColor3f(0, 1, 0);
	}

	public void update(long deltaTime) {
		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			xRot += 5;
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			xRot -= 5;

		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			yRot -= 5;
		if(Keyboard.isKeyDown(Keyboard.KEY_UP))
			yRot += 5;

		if(Keyboard.isKeyDown(Keyboard.KEY_R))
			xRot = yRot = 0;
	}

	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);

		glPushMatrix();

		glRotatef(yRot, 1, 0, 0);
		glRotatef(xRot, 0, 1, 0);

		float step = glGetFloat(GL_POINT_SIZE_GRANULARITY);
		glGetFloat(GL_POINT_SIZE_RANGE, (FloatBuffer)sizes.clear());

		for(float angle = 0, z = -50, curSize = sizes.get(0); angle <= 2 * Math.PI * 3; angle += 0.1f, z += 0.5f, curSize = Math.min(curSize + step, sizes.get(1))) {
			glPointSize(curSize);
			glBegin(GL_POINTS);
			glVertex3f(50 * (float)Math.cos(angle), 50 * (float)Math.sin(angle), z);
			glEnd();
		}

		glPopMatrix();
	}

	public void resized() {
		int w = Display.getWidth(), h = Display.getHeight();

		if(h == 0)
			h = 1;

		glViewport(0, 0, Display.getWidth(), Display.getHeight());

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		float aspect = (float)w / h;
		if(w <= h)
			glOrtho(-100, 100, -100 / aspect, 100 / aspect, 100, -100);
		else
			glOrtho(-100 * aspect, 100 * aspect, -100, 100, 100, -100);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}
}
