package com.ra4king.opengl.superbible.osb5.chapter2.example3;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;

/**
 * "Bounce" demo -- even more trivial than "Move".
 */
public class Example2_3 extends GLProgram {
	private ShaderProgram program;
	private int vbo;
	private float blockSize = 0.1f;
	private float[] block = new float[] {
											-blockSize - 0.5f, -blockSize, 0.0f, 1.0f,
											blockSize - 0.5f, -blockSize, 0.0f, 1.0f,
											blockSize - 0.5f, blockSize, 0.0f, 1.0f,
											-blockSize - 0.5f, blockSize, 0.0f, 1.0f,
	};
	private FloatBuffer verts = BufferUtils.createFloatBuffer(16);
	private float stepSize = 0.005f;
	private float dx = stepSize;
	private float dy = stepSize;
	
	public static void main(String[] args) {
		new Example2_3().run(3, 0);
	}
	
	public Example2_3() {
		super("Bounce", 500, 500, false);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 1, 0); // Blue
		
		program = new ShaderProgram(readFromFile("example2.3.vert"), readFromFile("example2.3.frag"));
		
		verts.put(block).flip();
		
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, verts, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void update(long deltaTime) {
		// Don't look here for OSB5's stupidly obfuscated calculations, this here is as simple as it gets
		float left = block[0];
		float right = block[4];
		float bottom = block[1];
		float top = block[9];
		
		if(left < -1.0f || right > 1.0f)
			dx = -dx;
		
		if(bottom < -1.0f || top > 1.0f)
			dy = -dy;
		
		block[0] += dx;
		block[1] += dy;
		block[4] += dx;
		block[5] += dy;
		block[8] += dx;
		block[9] += dy;
		block[12] += dx;
		block[13] += dy;
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		verts.rewind();
		verts.put(block).flip();
		glBufferSubData(GL_ARRAY_BUFFER, 0, verts);
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		glEnableClientState(GL_VERTEX_ARRAY);
		program.begin();
		glUniform4f(glGetUniformLocation(program.getProgram(), "color"), 1, 0, 0, 1); // red
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glUseProgram(0);
		glDisableClientState(GL_VERTEX_ARRAY);
	}
}
