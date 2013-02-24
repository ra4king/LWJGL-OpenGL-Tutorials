package com.ra4king.opengl.superbible.osb4.chapter3;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

public class Example3_7 extends GLProgram {
	public static void main(String[] args) {
		new Example3_7().run();
	}
	
	public Example3_7() {
		super("LSTIPPLE", 800, 600, true);
	}
	
	private float xRot, yRot;
	
	public void init() {
		glClearColor(0, 0, 0, 1);
		
		glColor3f(0, 1, 0);
		
		glEnable(GL_LINE_STIPPLE);
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
		
		for(int y = -90, factor = 0; y <= 90; y += 20, factor++) {
			glLineStipple(factor, (short)0x5555);
			
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
