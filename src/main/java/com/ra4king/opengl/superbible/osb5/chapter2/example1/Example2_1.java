package com.ra4king.opengl.superbible.osb5.chapter2.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;

/** An "unrolled" version of OSB5e's Triangle sample.  It's similar to the first arcsynthesis example,
 * but uses only VBOs and uniforms, and not not VAOs and attribute positions.
 */
public class Example2_1 extends GLProgram {
	public static void main(String[] args) {
		new Example2_1().run(3, 0);
	}

	private int program, vbo;

	public Example2_1() {
		super("Triangle", 500, 500, false);
	}

	@Override
	public void init() {
		glClearColor(0, 0, 1, 0);	// Blue

		// Loading shaders is very very "boilerplatey".  This will be the first and last time you see this done without
		// the benefit of a library that reduces all this down to just one line.
		int vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, readFromFile("example2.1.vert"));

		glCompileShader(vs);

		if(glGetShader(vs, GL_COMPILE_STATUS) == GL_FALSE) {
			System.err.println("Failure in compiling vertex shader. Error log:\n" + glGetShaderInfoLog(vs, glGetShader(vs, GL_INFO_LOG_LENGTH)));
			System.exit(0);
		}

		int fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, readFromFile("example2.1.frag"));

		glCompileShader(fs);

		if(glGetShader(fs, GL_COMPILE_STATUS) == GL_FALSE) {
			System.err.println("Failure in compiling fragment shader. Error log:\n" + glGetShaderInfoLog(fs, glGetShader(fs, GL_INFO_LOG_LENGTH)));
			destroy();
		}

		program = glCreateProgram();
		glAttachShader(program, vs);
		glAttachShader(program, fs);

		glLinkProgram(program);

		if(glGetProgram(program, GL_LINK_STATUS) == GL_FALSE) {
			System.err.println("Failure in linking program. Error log:\n" + glGetProgramInfoLog(program, glGetProgram(program, GL_INFO_LOG_LENGTH)));
			destroy();
		}

		glDetachShader(program, vs);
		glDetachShader(program, fs);

		glDeleteShader(vs);
		glDeleteShader(fs);
		// End of shader boilerplate

		// Just like shaders, creating Vertex Buffer Objects (VBOs) has a lot of boilerplate, some of which is from
		// the OpenGL API, and some of which is due to LWJGL demanding NIO bytebuffers and not just float arrays.
		// Again, we'll be pushing this into a nice abstraction eventually.
		vbo = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		FloatBuffer verts = BufferUtils.createFloatBuffer(12).put(new float[]{
				-0.5f, 0.0f, 0.0f, 1.0f,
				 0.5f, 0.0f, 0.0f, 1.0f,
				 0.0f, 0.5f, 0.0f, 1.0f
		});
		verts.flip(); // never forget to flip your buffers after filling them!
		glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		glEnableClientState(GL_VERTEX_ARRAY);
		glUseProgram(program);
		glUniform4f(glGetUniformLocation(program, "color"), 1, 0, 0, 1); // red
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		glDrawArrays(GL_TRIANGLES, 0, 3);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glUseProgram(0);
		glDisableClientState(GL_VERTEX_ARRAY);
	}
}
