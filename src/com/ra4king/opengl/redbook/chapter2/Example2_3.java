package com.ra4king.opengl.redbook.chapter2;

import static org.lwjgl.opengl.GL11.*;

import com.ra4king.opengl.GLProgram;

public class Example2_3 extends GLProgram {
	public static void main(String[] args) {
		new Example2_3().run();
	}

	public Example2_3() {
		super("Example 2.1", 200, 200, false);
	}

	@Override
	public void init() {
		glOrtho(0, getWidth(), 0, getHeight(), -1, 1);

		glClearColor(0, 0, 0, 0);
		glColor3f(1, 1, 1);

		glShadeModel(GL_FLAT);
	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);

		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glBegin(GL_POLYGON);
		glEdgeFlag(true);
		glVertex2f(50, 50);

		glEdgeFlag(false);
		glVertex2f(150, 100);

		glEdgeFlag(true);
		glVertex2f(90, 150);
		glEnd();
	}
}
