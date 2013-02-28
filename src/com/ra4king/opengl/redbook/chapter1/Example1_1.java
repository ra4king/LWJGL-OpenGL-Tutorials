package com.ra4king.opengl.redbook.chapter1;

import static org.lwjgl.opengl.GL11.*;

import com.ra4king.opengl.GLProgram;

public class Example1_1 extends GLProgram {
	public static void main(String[] args) {
		new Example1_1().run();
	}
	
	public Example1_1() {
		super("Example 1.1", 800, 600, false);
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
		
		glBegin(GL_POLYGON);
		glVertex2f(-0.5f, -0.5f);
		glVertex2f(-0.5f, 0.5f);
		glVertex2f(0.5f, 0.5f);
		glVertex2f(0.5f, -0.5f);
		glEnd();
	}
}
