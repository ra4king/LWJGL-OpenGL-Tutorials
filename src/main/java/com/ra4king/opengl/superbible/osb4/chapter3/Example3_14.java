package com.ra4king.opengl.superbible.osb4.chapter3;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;

import com.ra4king.opengl.GLProgram;

public class Example3_14 extends GLProgram {
	public static void main(String[] args) {
		new Example3_14().run(new PixelFormat(8,16,8));
	}
	
	private float x, y, size = 50;
	private float xstep = 1, ystep = 1;
	
	private float width = 100, height = 100;
	
	public Example3_14() {
		super("STENCIL", 800, 600, true);
	}
	
	public void init() {
		glClearColor(0, 0, 1, 1);
		glClearStencil(0);
		
		glEnable(GL_STENCIL_TEST);
	}
	
	public void update(long deltaTime) {
		if(x > width - size || x < -width)
			xstep = -xstep;
		
		if(y > height || y < -height + size)
			ystep = -ystep;
		
		x += xstep;
		y += ystep;
		
		if(x > width - size + xstep)
			x = width - size - 1;
		else if(x < -width - xstep)
			x = -width - 1;
		
		if(y > height + ystep)
			y = height - 1;
		else if(y < -height + size - ystep)
			y = -height + size - 1;
	}
	
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		
		glStencilFunc(GL_NEVER, 0, 0);
		glStencilOp(GL_INCR, GL_INCR, GL_INCR);
		
		glColor3f(1, 1, 1);
		glBegin(GL_LINE_STRIP);
		for(double angle = 0, radius = 0.1; angle < 400; angle += 0.1, radius *= 1.002)
			glVertex2d(radius * Math.cos(angle), radius * Math.sin(angle));
		glEnd();
		
		glStencilFunc(GL_NOTEQUAL, 1, 1);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		
		glColor3f(1, 0, 0);
		glRectf(x, y, x + size, y - size);
	}
	
	public void resized() {
		int w = Display.getWidth(), h = Display.getHeight();
		
		if(h == 0)
			h = 1;
		
		glViewport(0, 0, Display.getWidth(), Display.getHeight());
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		
		float aspect = (float)w / h;
		if(w <= h) {
			width = 100;
			height = 100 / aspect;
		}
		else {
			width = 100 * aspect;
			height = 100;
		}
		
		glOrtho(-width, width, -height, height, 1, -1);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}
}
