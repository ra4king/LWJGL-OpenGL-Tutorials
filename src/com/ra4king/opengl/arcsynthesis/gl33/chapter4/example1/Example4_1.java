package com.ra4king.opengl.arcsynthesis.gl33.chapter4.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;

public class Example4_1 extends GLProgram {
	public static void main(String[] args) {
		new Example4_1().run(true);
	}

	private final float[] data = {
			0.25f, 0.25f, 0.75f, 1.0f,
			0.25f, -0.25f, 0.75f, 1.0f,
			-0.25f, 0.25f, 0.75f, 1.0f,

			0.25f, -0.25f, 0.75f, 1.0f,
			-0.25f, -0.25f, 0.75f, 1.0f,
			-0.25f, 0.25f, 0.75f, 1.0f,

			0.25f, 0.25f, -0.75f, 1.0f,
			-0.25f, 0.25f, -0.75f, 1.0f,
			0.25f, -0.25f, -0.75f, 1.0f,

			0.25f, -0.25f, -0.75f, 1.0f,
			-0.25f, 0.25f, -0.75f, 1.0f,
			-0.25f, -0.25f, -0.75f, 1.0f,

			-0.25f, 0.25f, 0.75f, 1.0f,
			-0.25f, -0.25f, 0.75f, 1.0f,
			-0.25f, -0.25f, -0.75f, 1.0f,

			-0.25f, 0.25f, 0.75f, 1.0f,
			-0.25f, -0.25f, -0.75f, 1.0f,
			-0.25f, 0.25f, -0.75f, 1.0f,

			0.25f, 0.25f, 0.75f, 1.0f,
			0.25f, -0.25f, -0.75f, 1.0f,
			0.25f, -0.25f, 0.75f, 1.0f,

			0.25f, 0.25f, 0.75f, 1.0f,
			0.25f, 0.25f, -0.75f, 1.0f,
			0.25f, -0.25f, -0.75f, 1.0f,

			0.25f, 0.25f, -0.75f, 1.0f,
			0.25f, 0.25f, 0.75f, 1.0f,
			-0.25f, 0.25f, 0.75f, 1.0f,

			0.25f, 0.25f, -0.75f, 1.0f,
			-0.25f, 0.25f, 0.75f, 1.0f,
			-0.25f, 0.25f, -0.75f, 1.0f,

			0.25f, -0.25f, -0.75f, 1.0f,
			-0.25f, -0.25f, 0.75f, 1.0f,
			0.25f, -0.25f, 0.75f, 1.0f,

			0.25f, -0.25f, -0.75f, 1.0f,
			-0.25f, -0.25f, -0.75f, 1.0f,
			-0.25f, -0.25f, 0.75f, 1.0f,

			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,

			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,

			0.8f, 0.8f, 0.8f, 1.0f,
			0.8f, 0.8f, 0.8f, 1.0f,
			0.8f, 0.8f, 0.8f, 1.0f,

			0.8f, 0.8f, 0.8f, 1.0f,
			0.8f, 0.8f, 0.8f, 1.0f,
			0.8f, 0.8f, 0.8f, 1.0f,

			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,

			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,

			0.5f, 0.5f, 0.0f, 1.0f,
			0.5f, 0.5f, 0.0f, 1.0f,
			0.5f, 0.5f, 0.0f, 1.0f,

			0.5f, 0.5f, 0.0f, 1.0f,
			0.5f, 0.5f, 0.0f, 1.0f,
			0.5f, 0.5f, 0.0f, 1.0f,

			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,

			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,

			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f,

			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f
	};

	private ShaderProgram program;
	private int offsetUniform;

	private int vbo;

	public Example4_1() {
		super("Example 4.1 - Orthographic Cube", 500, 500, true);
	}

	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);

		program = new ShaderProgram(readFromFile("example4.1.vert"), readFromFile("example4.1.frag"));

		offsetUniform = glGetUniformLocation(program.getProgram(), "offset");

		vbo = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)BufferUtils.createFloatBuffer(data.length).put(data).flip(), GL_STATIC_DRAW);

		glBindVertexArray(glGenVertexArrays());

		glBindBuffer(GL_ARRAY_BUFFER, 0);

		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);

		program.begin();

		glUniform2f(offsetUniform, 0.5f, 0.25f);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, data.length * 2);

		glDrawArrays(GL_TRIANGLES, 0, 36);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		program.end();
	}
}
