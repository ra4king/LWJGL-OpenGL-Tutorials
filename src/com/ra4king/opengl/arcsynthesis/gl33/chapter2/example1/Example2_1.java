package com.ra4king.opengl.arcsynthesis.gl33.chapter2.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;

public class Example2_1 extends GLProgram {
	public static void main(String[] args) {
		new Example2_1().run(true);
	}

	private ShaderProgram program;
	private int vbo;

	public Example2_1() {
		super("Example 2.1 - Fragment Position", 500, 500, true);
	}

	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);

		program = new ShaderProgram(readFromFile("example2.1.vert"), readFromFile("example2.1.frag"));

		vbo = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)BufferUtils.createFloatBuffer(12).put(new float[] {0.75f, 0.75f, 0.0f, 1.0f,
				0.75f, -0.75f, 0.0f, 1.0f,
				-0.75f, -0.75f, 0.0f, 1.0f}).flip(), GL_STATIC_DRAW);

		glBindVertexArray(glGenVertexArrays());

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
