package com.ra4king.opengl.redbook.chapter1;

import static org.lwjgl.opengl.GL11.*;

import com.ra4king.opengl.GLProgram;

public class Example1_2 extends GLProgram {
	public static void main(String[] args) {
		new Example1_2().run();
	}
	
	public Example1_2() {
		super("Example 1.2",800,600,false);
	}
	
	@Override
	public void init() {
		glClearColor(0,0,0,0);
		glColor3f(1,0,0);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		
		glOrtho(-1,1,-1,1,-1,1);
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		
		glBegin(GL_POLYGON);
			glVertex2f(-0.5f,-0.5f);
			glVertex2f(-0.5f, 0.5f);
			glVertex2f( 0.5f, 0.5f);
			glVertex2f( 0.5f,-0.5f);
		glEnd();
	}
}
