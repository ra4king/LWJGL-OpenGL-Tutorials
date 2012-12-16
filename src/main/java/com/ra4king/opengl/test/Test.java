package com.ra4king.opengl.test;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Vector3;

public class Test extends GLProgram {
	public static void main(String[] args) {
		new Test().run(true);
	}
	
	public Test() {
		super("Test", 1024, 768, true);
		
		Mouse.setGrabbed(true);
	}
	
	private ShaderProgram program;
	
	private Matrix4 perspectiveMatrix;
	private int perspectiveMatrixUniform;
	
	private Matrix4 modelViewMatrix;
	private int modelViewMatrixUniform;
	
	private int vao;
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		program = new ShaderProgram(readFromFile("test.vert"), readFromFile("test.frag"));
		
		perspectiveMatrixUniform = glGetUniformLocation(program.getProgram(), "perspectiveMatrix");
		modelViewMatrixUniform = glGetUniformLocation(program.getProgram(), "modelViewMatrix");
		
		perspectiveMatrix = new Matrix4();
		modelViewMatrix = new Matrix4();
		
		float[] vertices = {
							5, 5, 5,
							5, -5, 5,
							-5, -5, 5,
							-5, 5, 5,
							
							5, 5, -5,
							5, -5, -5,
							-5, -5, -5,
							-5, 5, -5,
							
							0, 0, 0,
							0, 1, 0,
							1, 1, 0,
							1, 0, 0,
							
							0, 0, 1,
							0, 1, 1,
							1, 1, 1,
							1, 0, 1,
		};
		
		short[] indicies = {
							0, 1, 2,
							3, 0, 2,
							
							7, 6, 5,
							4, 7, 5,
							
							4, 5, 1,
							0, 4, 1,
							
							3, 2, 6,
							7, 3, 6,
							
							4, 0, 3,
							7, 4, 3,
							
							1, 5, 6,
							2, 1, 6,
		};
		
		int vbo1 = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo1);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		int vbo2 = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, (ShortBuffer)BufferUtils.createShortBuffer(indicies.length).put(indicies).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo1);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 8 * 3 * 4);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);
		glBindVertexArray(0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		glEnable(GL_DEPTH_TEST);
		glDepthRange(0, 1);
		glDepthFunc(GL_LEQUAL);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
	}
	
	@Override
	public void resized() {
		super.resized();
		
		program.begin();
		glUniformMatrix4(perspectiveMatrixUniform, false, perspectiveMatrix.clearToPerspectiveDeg(60, getWidth(), getHeight(), 1, 1000).toBuffer());
		program.end();
	}
	
	private Vector3 position = new Vector3();
	private float angle, vy;
	private final float gravity = -100;
	
	private float angleY;
	
	@Override
	public void update(long deltaTime) {
		final float delta = deltaTime / (float)1e9;
		
		float turnSpeed = (float)Math.PI * delta;
		float moveSpeed = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 100 : 50) * delta;
		
		if(Mouse.isGrabbed()) {
			angle += turnSpeed * Mouse.getDX() / 20f;
			angleY -= turnSpeed * Mouse.getDY() / 20f;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
			position.add((float)Math.cos(angle + Math.PI) * moveSpeed, 0, (float)Math.sin(angle + Math.PI) * moveSpeed);
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
			position.sub((float)Math.cos(angle + Math.PI) * moveSpeed, 0, (float)Math.sin(angle + Math.PI) * moveSpeed);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
			position.sub((float)Math.cos(angle + Math.PI / 2) * moveSpeed, 0, (float)Math.sin(angle + Math.PI / 2) * moveSpeed);
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
			position.add((float)Math.cos(angle + Math.PI / 2) * moveSpeed, 0, (float)Math.sin(angle + Math.PI / 2) * moveSpeed);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_R))
			position.reset();
		
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE) && position.y() == 0)
			vy = 30;
		
		vy += gravity * delta;
		position.add(0, vy * delta, 0);
		
		if(position.y() < 0) {
			position.y(0);
			vy = 0;
		}
	}
	
	@Override
	public void keyPressed(int key, char c, long nanos) {
		if(key == Keyboard.KEY_M)
			Mouse.setGrabbed(!Mouse.isGrabbed());
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		program.begin();
		
		glBindVertexArray(vao);
		for(int a = -100; a <= 100; a++) {
			if(a == 0)
				continue;
			
			for(int b = -100; b <= 0; b++) {
				if(b == -50)
					continue;
				
				glUniformMatrix4(modelViewMatrixUniform, false, modelViewMatrix.clearToIdentity().rotate(angleY, 1, 0, 0).rotate(angle, 0, 1, 0).translate(a * 10 - position.x(), -20 - position.y(), b * 10 - 20 - position.z()).toBuffer());
				glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, 0);
			}
		}
		glBindVertexArray(0);
		
		program.end();
	}
}