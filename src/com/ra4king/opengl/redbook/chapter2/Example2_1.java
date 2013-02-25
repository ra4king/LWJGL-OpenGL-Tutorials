package com.ra4king.opengl.redbook.chapter2;

import static org.lwjgl.opengl.GL11.*;

import com.ra4king.opengl.GLProgram;

public class Example2_1 extends GLProgram {
	public static void main(String[] args) {
		new Example2_1().run();
	}

	public Example2_1() {
		super("Example 2.1", 400, 150, false);
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

		glEnable(GL_LINE_STIPPLE);

		glLineWidth(1);
		glLineStipple(1, (short)0x0101);
		drawOneLine(50, 125, 150, 125);
		glLineStipple(1, (short)0x00FF);
		drawOneLine(150, 125, 250, 125);
		glLineStipple(1, (short)0x1C47);
		drawOneLine(250, 125, 350, 125);

		glLineWidth(5);
		glLineStipple(1, (short)0x0101);
		drawOneLine(50, 100, 150, 100);
		glLineStipple(1, (short)0x00FF);
		drawOneLine(150, 100, 250, 100);
		glLineStipple(1, (short)0x1C47);
		drawOneLine(250, 100, 350, 100);

		glLineWidth(1);
		glLineStipple(1, (short)0x1C47);
		glBegin(GL_LINE_STRIP);
		for(int a = 0; a < 7; a++)
			glVertex2f(50 + a * 50, 75);
		glEnd();

		for(int a = 0; a < 6; a++)
			drawOneLine(50 + a * 50, 50, 50 + (a + 1) * 50, 50);

		glLineStipple(5, (short)0x1C47);
		drawOneLine(50, 25, 350, 25);
	}

	private void drawOneLine(float x1, float y1, float x2, float y2) {
		glBegin(GL_LINES);
		glVertex2f(x1, y1);
		glVertex2f(x2, y2);
		glEnd();
	}
}
