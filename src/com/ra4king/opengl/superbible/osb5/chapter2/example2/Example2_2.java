package com.ra4king.opengl.superbible.osb5.chapter2.example2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;

/**
 * The OSB5 "Move" demo. Use the arrow keys to move the square around the screen.
 * <p/>
 * Like the previous demo, this one eliminates the abstract libraries from the book, but this one does use a ShaderProgram class for loading shaders. This is the class we'll be using throughout the
 * rest of these examples. We're still manipulating VBOs by hand though, so you can see how they're updated with glBufferSubData.
 * <p/>
 * The input handling code is necessarily different from the OSB one, since GLUT and LWJGL have very different input models. Consequently, the movement code is also modified somewhat, but it should
 * still be very simple to understand. You'll notice it's a lot smoother than OSB.
 * <p/>
 * The movement code has a bug: hold down left, then tap right, or do the same for up and down. Fixing it is left as an exercise to the reader.
 */
public class Example2_2 extends GLProgram {
	private ShaderProgram program;
	private int vbo;
	private float blockSize = 0.1f;
	private float[] block = new float[] {
			-blockSize, -blockSize, 0.0f, 1.0f,
			blockSize, -blockSize, 0.0f, 1.0f,
			blockSize, blockSize, 0.0f, 1.0f,
			-blockSize, blockSize, 0.0f, 1.0f,
	};
	private FloatBuffer verts = BufferUtils.createFloatBuffer(16);
	private float stepSize = 0.025f;
	private float dx = 0f;
	private float dy = 0f;
	
	public static void main(String[] args) {
		new Example2_2().run(3, 0);
	}
	
	public Example2_2() {
		super("Move", 500, 500, false);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 1, 0); // Blue
		
		program = new ShaderProgram(readFromFile("example2.2.vert"), readFromFile("example2.2.frag"));
		
		verts.put(block).flip();
		
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		// Notice we use DYNAMIC_DRAW since we're going to be changing the vertices frequently.
		// It works either way, it's just more efficient to use DYNAMIC_DRAW.
		glBufferData(GL_ARRAY_BUFFER, verts, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_UP:
				dy = stepSize;
				break;
			case Keyboard.KEY_DOWN:
				dy = -stepSize;
				break;
			case Keyboard.KEY_LEFT:
				dx = -stepSize;
				break;
			case Keyboard.KEY_RIGHT:
				dx = stepSize;
				break;
		}
	}
	
	@Override
	public void keyReleased(int key, char c) {
		switch(key) {
			case Keyboard.KEY_UP:
				dy = 0;
				break;
			case Keyboard.KEY_DOWN:
				dy = 0;
				break;
			case Keyboard.KEY_LEFT:
				dx = 0;
				break;
			case Keyboard.KEY_RIGHT:
				dx = 0;
				break;
		}
	}
	
	@Override
	public void update(long deltaTime) {
		if(dx == 0 && dy == 0)
			return;
		
		// NOTE: this is NOT the way you should be moving objects around! In real apps, you should
		// provide a translation matrix to the shader to multiply vertices, leaving the VBO alone.
		
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
