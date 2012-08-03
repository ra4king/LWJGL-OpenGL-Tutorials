package com.ra4king.opengl.superbible.chapter2;

import com.ra4king.opengl.GLProgram;

import static org.lwjgl.opengl.GL11.*;

public class Example2_1 extends GLProgram {
	public static void main(String[] args) {
		new Example2_1().run();
	}
	
	public Example2_1() {
		super("SIMPLE",800,600,false);
	}
	
	public void init() {
		glClearColor(0,0,1,1);
	}
	
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
	}
}
