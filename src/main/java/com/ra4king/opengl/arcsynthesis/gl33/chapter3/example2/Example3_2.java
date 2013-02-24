package com.ra4king.opengl.arcsynthesis.gl33.chapter3.example2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;

public class Example3_2 extends GLProgram {
	public static void main(String[] args) {
		new Example3_2().run(true);
	}
	
	private ShaderProgram program;
	private int offsetLocation;
	private int vbo;
	
	private long elapsedTime;
	
	public Example3_2() {
		super("Example 3.2 - Vertex Position Offset", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		
		program = new ShaderProgram(readFromFile("example3.2.vert"), readFromFile("example3.2.frag"));
		offsetLocation = glGetUniformLocation(program.getProgram(), "offset");
		
		vbo = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)BufferUtils.createFloatBuffer(12).put(new float[] { 0.25f, 0.25f, 0.0f, 1.0f,
																										0.25f, -0.25f, 0.0f, 1.0f,
																										-0.25f, -0.25f, 0.0f, 1.0f }).flip(), GL_STATIC_DRAW);
		
		glBindVertexArray(glGenVertexArrays());
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void update(long deltaTime) {
		elapsedTime += deltaTime;
	}
	
	@Override
	public void render() {
		float loopDuration = 5;
		float scale = (float)Math.PI * 2 / loopDuration;
		float currentTimeThroughLoop = (elapsedTime / (float)1e9) % loopDuration;
		
		float xOffset = (float)Math.cos(currentTimeThroughLoop * scale) * 0.5f;
		float yOffset = (float)Math.sin(currentTimeThroughLoop * scale) * 0.5f;
		
		glClear(GL_COLOR_BUFFER_BIT);
		
		program.begin();
		
		glUniform2f(offsetLocation, xOffset, yOffset);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		
		glDrawArrays(GL_TRIANGLES, 0, 3);
		
		glDisableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		program.end();
	}
}
