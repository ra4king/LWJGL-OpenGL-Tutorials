package com.ra4king.opengl.superbible.osb4.chapter2;

import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

import static org.lwjgl.opengl.GL11.*;

public class Example2_2 extends GLProgram {
	public static void main(String[] args) {
		new Example2_2().run();
	}
	
	public Example2_2() {
		super("GLRect",800,600,true);
	}
	
	public void init() {
		glClearColor(0,0,1,1);
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
			glOrtho(-100,100,-100/aspect,100/aspect,1,-1);
		else
			glOrtho(-100*aspect,100*aspect,-100,100,1,-1);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}
	
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		
		glColor3f(1,0,0);
		glRectf(-25,25,25,-25);
	}
}
