package com.ra4king.opengl.redbook.chapter2;

import static org.lwjgl.opengl.GL11.*;

import com.ra4king.opengl.GLProgram;

public class Example2_4 extends GLProgram {
	public static void main(String[] args) {
		new Example2_4().run();
	}
	
	private final float x = 0.525731112119133606f;
	private final float z = 0.850650808352039932f;
	
	private final float[][] vdata = {
			{ -x, 0, z }, { x, 0, z }, { -x, 0, -z }, { x, 0, -z },
			{ 0, z, x }, { 0, z, -x }, { 0, -z, x }, { 0, -z, -x },
			{ z, x, 0 }, { -z, x, 0 }, { z, -x, 0 }, { -z, -x, 0 }
	};
	
	private final int[][] tindices = {
			{ 0, 4, 1 }, { 0, 9, 4 }, { 9, 5, 4 }, { 4, 5, 8 }, { 4, 8, 1 },
			{ 8, 10, 1 }, { 8, 3, 10 }, { 5, 3, 8 }, { 5, 2, 3 }, { 2, 7, 3 },
			{ 7, 10, 3 }, { 7, 6, 10 }, { 7, 11, 6 }, { 11, 0, 6 }, { 0, 1, 6 },
			{ 6, 1, 10 }, { 9, 0, 11 }, { 9, 11, 2 }, { 9, 2, 5 }, { 7, 2, 11 }
	};
	
	private float[][] colors;
	
	public Example2_4() {
		super("Example 2.1", 800, 600, false);
	}
	
	@Override
	public void init() {
		glOrtho(-1, 1, -1, 1, -1, 1);
		
		glClearColor(0, 0, 0, 0);
		glColor3f(1, 1, 1);
		
		glShadeModel(GL_FLAT);
		
		colors = new float[20][3];
		for(int a = 0; a < colors.length; a++) {
			colors[a][0] = (float)Math.random();
			colors[a][1] = (float)Math.random();
			colors[a][2] = (float)Math.random();
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		
		for(int a = 0; a < 20; a++) {
			glColor3f(colors[a][0], colors[a][1], colors[a][2]);
			glBegin(GL_TRIANGLES);
			float[] v = vdata[tindices[a][0]];
			glVertex3f(v[0], v[1], v[2]);
			v = vdata[tindices[a][1]];
			glVertex3f(v[0], v[1], v[2]);
			v = vdata[tindices[a][2]];
			glVertex3f(v[0], v[1], v[2]);
			glEnd();
		}
	}
}
