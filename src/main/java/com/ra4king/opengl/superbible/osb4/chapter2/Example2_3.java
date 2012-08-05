package com.ra4king.opengl.superbible.osb4.chapter2;

import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

import static org.lwjgl.opengl.GL11.*;

public class Example2_3 extends GLProgram {
	public static void main(String[] args) {
		new Example2_3().run();
	}
	
	private float x, y, size = 25;
	private float xstep = 1, ystep = 1;
	
	private float width = 100, height = 100;
	
	public Example2_3() {
		super("BOUNCE",500,500,true);
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
		if(w <= h) {
			width = 100;
			height = 100/aspect;
		}
		else {
			width = 100*aspect;
			height = 100;
		}
		
		glOrtho(-width,width,-height,height,1,-1);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}
	
	public void update(long deltaTime) {
		if(x > width-size || x < -width)
			xstep = -xstep;
		
		if(y > height || y < -height+size)
			ystep = -ystep;
		
		x += xstep;
		y += ystep;
		
		if(x > width - size + xstep)
			x = width-size-1;
		else if(x < -width-xstep)
			x = -width-1;
		
		if(y > height + ystep)
			y = height-1;
		else if(y < -height + size - ystep)
			y = -height + size - 1;
	}
	
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		
		glColor3f(1,0,0);
		glRectf(x,y,x+size,y-size);
	}
}
