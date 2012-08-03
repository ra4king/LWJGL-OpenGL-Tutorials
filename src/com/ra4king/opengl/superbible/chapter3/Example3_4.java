package com.ra4king.opengl.superbible.chapter3;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

import static org.lwjgl.opengl.GL11.*;

public class Example3_4 extends GLProgram {
	public static void main(String[] args) {
		new Example3_4().run();
	}
	
	public Example3_4() {
		super("LINES",800,600,true);
	}
	
	private float xRot, yRot;
	
	public void init() {
		glClearColor(0,0,0,1);
		
		glColor3f(0,1,0);
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
		
		glPointSize(5);
		
		glBegin(GL_LINES);
		
		for(float angle = 0; angle <= Math.PI; angle += Math.PI/20) {
			glVertex3f(50 * (float)Math.cos(angle), 50 * (float)Math.sin(angle), 0);
			glVertex3f(50 * (float)Math.cos(angle + Math.PI), 50 * (float)Math.sin(angle + Math.PI), 0);
		}
		
		glEnd();
		
		glPopMatrix();
	}
	
	public void resized() {
		int w = Display.getWidth(), h = Display.getHeight();
		
		if(h == 0)
			h = 1;
		
		glViewport(0,0,Display.getWidth(),Display.getHeight());
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		
		float aspect = (float)w/h;
		if(w <= h)
			glOrtho(-100,100,-100/aspect,100/aspect,100,-100);
		else
			glOrtho(-100*aspect,100*aspect,-100,100,100,-100);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}
}
