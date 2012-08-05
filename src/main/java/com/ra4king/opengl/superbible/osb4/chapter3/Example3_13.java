package com.ra4king.opengl.superbible.osb4.chapter3;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

public class Example3_13 extends GLProgram {
	public static void main(String[] args) {
		new Example3_13().run();
	}
	
	public Example3_13() {
		super("SCISSOR",800,600,true);
	}
	
	public void init() {
		glEnable(GL_SCISSOR_TEST);
	}
	
	public void render() {
		glScissor(0,0,800,600);
		glClearColor(0,0,1,1);
		glClear(GL_COLOR_BUFFER_BIT);
		
		glScissor(100,100,600,400);
		glClearColor(1,0,0,1);
		glClear(GL_COLOR_BUFFER_BIT);
		
		glScissor(200,200,400,200);
		glClearColor(0,1,0,1);
		glClear(GL_COLOR_BUFFER_BIT);
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
