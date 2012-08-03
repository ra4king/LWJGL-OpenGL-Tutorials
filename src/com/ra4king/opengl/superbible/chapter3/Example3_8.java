package com.ra4king.opengl.superbible.chapter3;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

public class Example3_8 extends GLProgram {
	public static void main(String[] args) {
		new Example3_8().run();
	}
	
	public Example3_8() {
		super("TRIANGLE",800,600,true);
	}
	
	private float xRot, yRot;
	
	public void init() {
		glClearColor(0,0,0,1);
		
		glFrontFace(GL_CW);
		
		glShadeModel(GL_FLAT);
		
		glEnable(GL_DEPTH_TEST);
		//glEnable(GL_CULL_FACE);
		
		glPolygonMode(GL_BACK,GL_LINE);
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
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		glPushMatrix();
		
		glRotatef(yRot, 1, 0, 0);
		glRotatef(xRot, 0, 1, 0);
		
		glBegin(GL_TRIANGLE_FAN);
		
		glVertex3f(0,0,75);
		int pivot = 1;
		for(float angle = 0; angle <= 2 * Math.PI + 1; angle += Math.PI/8) {
			if(pivot++ % 2 == 0)
				glColor3f(0,1,.5f);
			else
				glColor3f(1,0,.5f);
			
			glVertex3f(50*(float)Math.cos(angle),50*(float)Math.sin(angle),0);
		}
		
		glEnd();
		
		glBegin(GL_TRIANGLE_FAN);
		
		glVertex2f(0,0);
		pivot = 1;
		for(float angle = 0; angle <= 2.1 * Math.PI; angle += Math.PI/8) {
			if(pivot++ % 2 == 0)
				glColor3f(0,1,0);
			else
				glColor3f(1,0,0);
			
			glVertex2f(50*(float)Math.cos(angle),50*(float)Math.sin(angle));
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
