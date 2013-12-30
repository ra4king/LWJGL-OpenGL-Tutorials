package com.ra4king.opengl.arcsynthesis.gl33.chapter2.example2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;

public class Example2_2 extends GLProgram {
	public static void main(String[] args) {
		new Example2_2().run(true);
	}
	
	private ShaderProgram program;
	private int vbo;
	
	public Example2_2() {
		super("Example 2.2 - Vertex Colors", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		
		program = new ShaderProgram(readFromFile("example2.2.vert"), readFromFile("example2.2.frag"));
		
		vbo = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)BufferUtils.createFloatBuffer(24).put(new float[] { 0.0f, 0.5f, 0.0f, 1.0f,
				0.5f, -0.366f, 0.0f, 1.0f,
				-0.5f, -0.366f, 0.0f, 1.0f,
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f }).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		// In core OpenGL, Vertex Array Objects (VAOs) are required for all draw calls. VAOs will be explained in Chapter 5.
		glBindVertexArray(glGenVertexArrays());
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		
		program.begin();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 48);
		
		glDrawArrays(GL_TRIANGLES, 0, 3);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		program.end();
	}
}
