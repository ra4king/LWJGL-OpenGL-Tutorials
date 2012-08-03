package com.ra4king.opengl.redbook.chapter1;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Mouse;

import com.ra4king.opengl.GLProgram;

public class Example1_3 extends GLProgram {
	public static void main(String[] args) {
		new Example1_3().run();
	}
	
	private float spin;
	
	public Example1_3() {
		super("Example 1.3", 800, 600,false);
	}
	
	@Override
	public void init() {
		glClearColor(0,0,0,1);
		glColor3f(1,1,1);
		glShadeModel(GL_FLAT);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		
		glOrtho(-50f * getWidth()/getHeight(),
				50f * getWidth()/getHeight(),
				-50, 50, -1 ,1);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}
	
	@Override
	public void render() {
		if(Mouse.isButtonDown(0))
			spin();
		
		glClear(GL_COLOR_BUFFER_BIT);
		
		glPushMatrix();
		glRotatef(spin, 0, 0, 1);
		glRectf(-25, -25, 25, 25);
		glPopMatrix();
	}
	
	private void spin() {
		spin = (spin + 2) % 360;
	}
}
