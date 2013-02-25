package com.ra4king.opengl.redbook.chapter2;

import static org.lwjgl.opengl.GL11.*;

import com.ra4king.opengl.GLProgram;

public class Circle extends GLProgram {
	public static void main(String[] args) {
		new Circle().run();
	}

	public Circle() {
		super("Example 2.1", 800, 600, false);
	}

	@Override
	public void init() {
		glOrtho(-1, 1, -1, 1, -1, 1);

		glClearColor(0, 0, 0, 0);
		glColor3f(1, 0, 0);
	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);

		glBegin(GL_LINE_LOOP);
		for(int a = 0; a < 100; a++) {
			double angle = 2 * Math.PI * a / 100;
			glVertex2f((float)Math.cos(angle), (float)Math.sin(angle));
		}
		glEnd();
	}
}
