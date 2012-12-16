package com.ra4king.opengl.arcsynthesis.gl33.chapter3.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;

public class Example3_1 extends GLProgram {
	public static void main(String[] args) {
		new Example3_1().run(true);
	}
	
	private ShaderProgram program;
	
	private FloatBuffer vertexData, newData;
	private int vbo;
	
	private long elapsedTime;
	
	public Example3_1() {
		super("Example 3.1", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		
		program = new ShaderProgram(readFromFile("example3.1.vert"), readFromFile("example3.1.frag"));
		
		vbo = glGenBuffers();
		
		vertexData = (FloatBuffer)BufferUtils.createFloatBuffer(12).put(new float[] { 0.25f, 0.25f, 0.0f, 1.0f,
																						0.25f, -0.25f, 0.0f, 1.0f,
																						-0.25f, -0.25f, 0.0f, 1.0f }).flip();
		newData = BufferUtils.createFloatBuffer(12);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STREAM_DRAW);
		
		glBindVertexArray(glGenVertexArrays());
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void update(long deltaTime) {
		elapsedTime += deltaTime;
		
		float loopDuration = 5;
		float scale = (float)Math.PI * 2 / loopDuration;
		float currentTimeThroughLoop = (elapsedTime / (float)1e9) % loopDuration;
		
		float xOffset = (float)Math.cos(currentTimeThroughLoop * scale) * 0.5f;
		float yOffset = (float)Math.sin(currentTimeThroughLoop * scale) * 0.5f;
		
		newData.clear();
		
		for(int a = 0; a < 12; a += 4) {
			newData.put(vertexData.get(a) + xOffset);
			newData.put(vertexData.get(a + 1) + yOffset);
			newData.put(0);
			newData.put(1);
		}
		
		newData.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferSubData(GL_ARRAY_BUFFER, 0, newData);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		
		program.begin();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		
		glDrawArrays(GL_TRIANGLES, 0, 3);
		
		glDisableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		program.end();
	}
}
