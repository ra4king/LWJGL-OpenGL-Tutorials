package com.ra4king.opengl.arcsynthesis.chapter5.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.ShaderProgram;

public class Example5_1 extends GLProgram {
	public static void main(String[] args) {
		new Example5_1().run(true);
	}
	
	private final float RIGHT_EXTENT = 0.8f;
	private final float LEFT_EXTENT = -RIGHT_EXTENT;
	private final float TOP_EXTENT = 0.2f;
	private final float MIDDLE_EXTENT = 0;
	private final float BOTTOM_EXTENT = -TOP_EXTENT;
	private final float FRONT_EXTENT = -1.25f;
	private final float REAR_EXTENT = -1.75f;
	
	private final float[] GREEN_COLOR = { 0.75f, 0.75f, 1, 1 };
	private final float[] BLUE_COLOR = { 0, 0.5f, 0, 1 };
	private final float[] RED_COLOR = { 1, 0, 0, 1 };
	private final float[] GREY_COLOR = { 0.8f, 0.8f, 0.8f, 1 };
	private final float[] BROWN_COLOR = { 0.5f, 0.5f, 0, 1 };
	
	private float data[] = {
			//Object 1 positions
			LEFT_EXTENT,    TOP_EXTENT,     REAR_EXTENT,
			LEFT_EXTENT,    MIDDLE_EXTENT,  FRONT_EXTENT,
			RIGHT_EXTENT,   MIDDLE_EXTENT,  FRONT_EXTENT,
			RIGHT_EXTENT,   TOP_EXTENT,     REAR_EXTENT,
			
			LEFT_EXTENT,    BOTTOM_EXTENT,  REAR_EXTENT,
			LEFT_EXTENT,    MIDDLE_EXTENT,  FRONT_EXTENT,
			RIGHT_EXTENT,   MIDDLE_EXTENT,  FRONT_EXTENT,
			RIGHT_EXTENT,   BOTTOM_EXTENT,  REAR_EXTENT,
			
			LEFT_EXTENT,    TOP_EXTENT,     REAR_EXTENT,
			LEFT_EXTENT,    MIDDLE_EXTENT,  FRONT_EXTENT,
			LEFT_EXTENT,    BOTTOM_EXTENT,  REAR_EXTENT,
			
			RIGHT_EXTENT,   TOP_EXTENT,     REAR_EXTENT,
			RIGHT_EXTENT,   MIDDLE_EXTENT,  FRONT_EXTENT,
			RIGHT_EXTENT,   BOTTOM_EXTENT,  REAR_EXTENT,
			
			LEFT_EXTENT,    BOTTOM_EXTENT,  REAR_EXTENT,
			LEFT_EXTENT,    TOP_EXTENT,     REAR_EXTENT,
			RIGHT_EXTENT,   TOP_EXTENT,     REAR_EXTENT,
			RIGHT_EXTENT,   BOTTOM_EXTENT,  REAR_EXTENT,
			
			//Object 2 positions
			TOP_EXTENT,     RIGHT_EXTENT,   REAR_EXTENT,
			MIDDLE_EXTENT,  RIGHT_EXTENT,   FRONT_EXTENT,
			MIDDLE_EXTENT,  LEFT_EXTENT,    FRONT_EXTENT,
			TOP_EXTENT,     LEFT_EXTENT,    REAR_EXTENT,
			
			BOTTOM_EXTENT,  RIGHT_EXTENT,   REAR_EXTENT,
			MIDDLE_EXTENT,  RIGHT_EXTENT,   FRONT_EXTENT,
			MIDDLE_EXTENT,  LEFT_EXTENT,    FRONT_EXTENT,
			BOTTOM_EXTENT,  LEFT_EXTENT,    REAR_EXTENT,
			
			TOP_EXTENT,     RIGHT_EXTENT,   REAR_EXTENT,
			MIDDLE_EXTENT,  RIGHT_EXTENT,   FRONT_EXTENT,
			BOTTOM_EXTENT,  RIGHT_EXTENT,   REAR_EXTENT,
			                
			TOP_EXTENT,     LEFT_EXTENT,    REAR_EXTENT,
			MIDDLE_EXTENT,  LEFT_EXTENT,    FRONT_EXTENT,
			BOTTOM_EXTENT,  LEFT_EXTENT,    REAR_EXTENT,
			                
			BOTTOM_EXTENT,  RIGHT_EXTENT,   REAR_EXTENT,
			TOP_EXTENT,     RIGHT_EXTENT,   REAR_EXTENT,
			TOP_EXTENT,     LEFT_EXTENT,    REAR_EXTENT,
			BOTTOM_EXTENT,  LEFT_EXTENT,    REAR_EXTENT,
			
			//Object 1 colors
			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			
			GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
			GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
			GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
			
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
			
			//Object 2 colors
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
			
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			
			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			
			GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
			GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
			GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
			GREY_COLOR[0], GREY_COLOR[1], GREY_COLOR[2], GREY_COLOR[3],
	};
	
	private final short[] indicies = {
			0, 2, 1,
			3, 2, 0,
			
			4, 5, 6,
			6, 7, 4,
			
			8, 9, 10,
			11, 13, 12,
			
			14, 16, 15,
			17, 16, 14
	};
	
	private ShaderProgram program;
	private int offsetUniform;
	
	private int vao1, vao2;
	
	private FloatBuffer perspectiveMatrix;
	private int perspectiveMatrixUniform;
	private float frustumScale = 1;
	
	public Example5_1() {
		super("Example 5.1", 500, 500, false);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		
		program = new ShaderProgram(readFromFile("example5.1.vert"),readFromFile("example5.1.frag"));
		
		offsetUniform = glGetUniformLocation(program.getProgram(), "offset");
		
		perspectiveMatrixUniform = glGetUniformLocation(program.getProgram(), "perspectiveMatrix");
		
		float zNear = 0.5f, zFar = 3;
		perspectiveMatrix = BufferUtils.createFloatBuffer(16);
		perspectiveMatrix.put(0,frustumScale);
		perspectiveMatrix.put(5,frustumScale);
		perspectiveMatrix.put(10,(zFar + zNear) / (zNear - zFar));
		perspectiveMatrix.put(14,(2 * zFar * zNear) / (zNear - zFar));
		perspectiveMatrix.put(11,-1);
		
		program.begin();
		glUniformMatrix4(perspectiveMatrixUniform, false, perspectiveMatrix);
		program.end();
		
		int vbo1 = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo1);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)BufferUtils.createFloatBuffer(data.length).put(data).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		int vbo2 = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, (ShortBuffer)BufferUtils.createShortBuffer(indicies.length).put(indicies).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		vao1 = glGenVertexArrays();
		glBindVertexArray(vao1);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo1);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 36 * 3 * 4);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);
		
		glBindVertexArray(0);
		
		vao2 = glGenVertexArrays();
		glBindVertexArray(vao2);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 36 / 2 * 3 * 4);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, (36 * 3 * 4) + (36 / 2 * 4 * 4));
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);
		
		glBindVertexArray(0);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
	}
	
	@Override
	public void resized() {
		super.resized();
		
		perspectiveMatrix.put(0,frustumScale / ((float)getWidth() / getHeight()));
		
		program.begin();
		glUniformMatrix4(perspectiveMatrixUniform, false, perspectiveMatrix);
		program.end();
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		
		program.begin();
		
		glBindVertexArray(vao1);
		glUniform3f(offsetUniform, 0, 0, 0);
		glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, 0);
		
		glBindVertexArray(vao2);
		glUniform3f(offsetUniform, 0, 0, -1);
		glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, 0);
		
		glBindVertexArray(0);
		
		program.end();
	}
}
