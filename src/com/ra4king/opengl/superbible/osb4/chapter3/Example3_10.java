package com.ra4king.opengl.superbible.osb4.chapter3;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.ra4king.opengl.GLProgram;

public class Example3_10 extends GLProgram {
	public static void main(String[] args) {
		new Example3_10().run();
	}
	
	public Example3_10() {
		super("PSTIPPLE", 800, 600, true);
	}
	
	private float xRot, yRot;
	
	public void init() {
		glClearColor(0, 0, 0, 1);
		
		glColor3f(1, 0, 0);
		
		glEnable(GL_POLYGON_STIPPLE);
		
		glPolygonStipple((ByteBuffer)BufferUtils.createByteBuffer(16 * 8).put(new byte[] {
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0xc0, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0xf0,
				(byte)0x00, (byte)0x00, (byte)0x07, (byte)0xf0, (byte)0x0f, (byte)0x00, (byte)0x1f, (byte)0xe0,
				(byte)0x1f, (byte)0x80, (byte)0x1f, (byte)0xc0, (byte)0x0f, (byte)0xc0, (byte)0x3f, (byte)0x80,
				(byte)0x07, (byte)0xe0, (byte)0x7e, (byte)0x00, (byte)0x03, (byte)0xf0, (byte)0xff, (byte)0x80,
				(byte)0x03, (byte)0xf5, (byte)0xff, (byte)0xe0, (byte)0x07, (byte)0xfd, (byte)0xff, (byte)0xf8,
				(byte)0x1f, (byte)0xfc, (byte)0xff, (byte)0xe8, (byte)0xff, (byte)0xe3, (byte)0xbf, (byte)0x70,
				(byte)0xde, (byte)0x80, (byte)0xb7, (byte)0x00, (byte)0x71, (byte)0x10, (byte)0x4a, (byte)0x80,
				(byte)0x03, (byte)0x10, (byte)0x4e, (byte)0x40, (byte)0x02, (byte)0x88, (byte)0x8c, (byte)0x20,
				(byte)0x05, (byte)0x05, (byte)0x04, (byte)0x40, (byte)0x02, (byte)0x82, (byte)0x14, (byte)0x40,
				(byte)0x02, (byte)0x40, (byte)0x10, (byte)0x80, (byte)0x02, (byte)0x64, (byte)0x1a, (byte)0x80,
				(byte)0x00, (byte)0x92, (byte)0x29, (byte)0x00, (byte)0x00, (byte)0xb0, (byte)0x48, (byte)0x00,
				(byte)0x00, (byte)0xc8, (byte)0x90, (byte)0x00, (byte)0x00, (byte)0x85, (byte)0x10, (byte)0x00,
				(byte)0x00, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00
		}).flip());
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
		
		glBegin(GL_POLYGON);
		glVertex2f(-20, 50);
		glVertex2f(20, 50);
		glVertex2f(50, 20);
		glVertex2f(50, -20);
		glVertex2f(20, -50);
		glVertex2f(-20, -50);
		glVertex2f(-50, -20);
		glVertex2f(-50, 20);
		glEnd();
		
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
