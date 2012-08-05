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
		super(true);
	}
	
	private ShaderProgram program;
	
	private Matrix4 modelViewMatrix;
	private int modelViewMatrixUniform;
	
	private int vao;
	
	@Override
	public void init() {
		glClearColor(0,0,0,0);
		glClearDepth(1);
		
		program = new ShaderProgram(readFromFile("test.vert"),readFromFile("test.frag"));
		
		int perspectiveMatrix = glGetUniformLocation(program.getProgram(), "perspectiveMatrix");
		modelViewMatrixUniform = glGetUniformLocation(program.getProgram(), "modelViewMatrix");
		
		modelViewMatrix = new Matrix4().clearToIdentity();
		modelViewMatrix.translate(0, -8, -20).rotate((float)Math.PI, 0, 1, 0);
		
		program.begin();
		glUniformMatrix4(perspectiveMatrix, false, new Matrix4().clearToPerspective((float)Math.PI/2, getWidth(), getHeight(), 1, 1000).getBuffer());
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