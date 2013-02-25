package com.ra4king.opengl.superbible.osb4.chapter3;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

public class Example3_6 extends GLProgram {
	public static void main(String[] args) {
		new Example3_6().run();
	}

	public Example3_6() {
		super("LINESW", 800, 600, true);
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

		glGetFloat(GL_LINE_WIDTH_RANGE, (FloatBuffer)sizes.clear());

		for(float y = -90, curSize = sizes.get(0); y <= 90; y += 20, curSize += 1) {
			glLineWidth(curSize);

			glBegin(GL_LINES);
			glVertex2f(-80, y);
			glVertex2f(80, y);
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
