package com.ra4king.opengl.test;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix4;

public class Test extends GLProgram {
	public static void main(String[] args) {
		new Test().run(true);
	}
	
	public Test() {
		super("Test",800,600,true);
	}
	
	private ShaderProgram program;
	
	private Matrix4 perspectiveMatrix;
	private int perspectiveMatrixUniform;
	
	private Matrix4 modelViewMatrix;
	private int modelViewMatrixUniform;
	
	private int vao;
	
	@Override
	public void init() {
		glClearColor(0,0,0,0);
		glClearDepth(1);
		
		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
		
		program = new ShaderProgram(readFromFile("test.vert"),readFromFile("test.frag"));
		
		perspectiveMatrixUniform = glGetUniformLocation(program.getProgram(), "perspectiveMatrix");
		modelViewMatrixUniform = glGetUniformLocation(program.getProgram(), "modelViewMatrix");
		
		perspectiveMatrix = new Matrix4().clearToPerspective((float)Math.PI/2, getWidth(), getHeight(), 1, 1000);
		modelViewMatrix = new Matrix4();
		
		program.begin();
		glUniformMatrix4(perspectiveMatrixUniform, false, perspectiveMatrix.getBuffer());
		program.end();
		
		float[] vertices = {
			 5,  5,  5,
			 5, -5,  5,
			-5, -5,  5,
			-5,  5,  5,
			
			 5,  5, -5,
			 5, -5, -5,
			-5, -5, -5,
			-5,  5, -5,
			
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
				2, 1, 6
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
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 8*3*4);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);
		glBindVertexArray(0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		glEnable(GL_DEPTH_TEST);
		glDepthRange(0,1);
		glDepthFunc(GL_LEQUAL);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
	}
	
	@Override
	public void resized() {
		super.resized();
		
		program.begin();
		glUniformMatrix4(perspectiveMatrixUniform, false, perspectiveMatrix.clearToPerspective((float)Math.PI/2, getWidth(), getHeight(), 1, 1000).getBuffer());
		program.end();
	}
	
	private long elapsedTime;
	
	@Override
	public void update(long deltaTime) {
		elapsedTime += deltaTime;
		
		float loopDuration = 3;
		float angle = ((elapsedTime/(float)1e9)%loopDuration) * (2*(float)Math.PI/loopDuration);
		modelViewMatrix.clearToIdentity().translate(0, -7, -20).rotate(20*(float)Math.PI/180, 1, 0, 0).rotate(angle, 0, 1, 0);
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		program.begin();
		
		glBindVertexArray(vao);
		glUniformMatrix4(modelViewMatrixUniform, false, modelViewMatrix.getBuffer());
		glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, 0);
		glBindVertexArray(0);
		
		program.end();
	}
}