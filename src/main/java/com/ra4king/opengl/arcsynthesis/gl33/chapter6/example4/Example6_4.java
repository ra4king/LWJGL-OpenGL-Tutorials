package com.ra4king.opengl.arcsynthesis.gl33.chapter6.example4;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Stack;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Vector3;

public class Example6_4 extends GLProgram {
	public static void main(String[] args) {
		new Example6_4().run(true);
	}
	
	private final float[] RED_COLOR = { 1, 0, 0, 1 };
	private final float[] GREEN_COLOR = { 0, 1, 0, 1 };
	private final float[] BLUE_COLOR = { 0, 0, 1, 1 };
	
	private final float[] YELLOW_COLOR = { 1, 1, 0, 1 };
	private final float[] CYAN_COLOR = { 0, 1, 1, 1 };
	private final float[] MAGENTA_COLOR = { 1, 0, 1, 1 };
	
	private final float[] vertices = {
			+1.0f, +1.0f, +1.0f,
			+1.0f, -1.0f, +1.0f,
			-1.0f, -1.0f, +1.0f,
			-1.0f, +1.0f, +1.0f,
			
			+1.0f, +1.0f, +1.0f,
			-1.0f, +1.0f, +1.0f,
			-1.0f, +1.0f, -1.0f,
			+1.0f, +1.0f, -1.0f,
			
			+1.0f, +1.0f, +1.0f,
			+1.0f, +1.0f, -1.0f,
			+1.0f, -1.0f, -1.0f,
			+1.0f, -1.0f, +1.0f,
			
			+1.0f, +1.0f, -1.0f,
			-1.0f, +1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			+1.0f, -1.0f, -1.0f,
			
			+1.0f, -1.0f, +1.0f,
			+1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, +1.0f,
			
			-1.0f, +1.0f, +1.0f,
			-1.0f, -1.0f, +1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, +1.0f, -1.0f,
		    
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
		    RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
		    
		    YELLOW_COLOR[0], YELLOW_COLOR[1], YELLOW_COLOR[2], YELLOW_COLOR[3],
		    YELLOW_COLOR[0], YELLOW_COLOR[1], YELLOW_COLOR[2], YELLOW_COLOR[3],
		    YELLOW_COLOR[0], YELLOW_COLOR[1], YELLOW_COLOR[2], YELLOW_COLOR[3],
		    YELLOW_COLOR[0], YELLOW_COLOR[1], YELLOW_COLOR[2], YELLOW_COLOR[3],
		    
		    CYAN_COLOR[0], CYAN_COLOR[1], CYAN_COLOR[2], CYAN_COLOR[3],
		    CYAN_COLOR[0], CYAN_COLOR[1], CYAN_COLOR[2], CYAN_COLOR[3],
		    CYAN_COLOR[0], CYAN_COLOR[1], CYAN_COLOR[2], CYAN_COLOR[3],
		    CYAN_COLOR[0], CYAN_COLOR[1], CYAN_COLOR[2], CYAN_COLOR[3],
		    
		    MAGENTA_COLOR[0], MAGENTA_COLOR[1], MAGENTA_COLOR[2], MAGENTA_COLOR[3],
		    MAGENTA_COLOR[0], MAGENTA_COLOR[1], MAGENTA_COLOR[2], MAGENTA_COLOR[3],
		    MAGENTA_COLOR[0], MAGENTA_COLOR[1], MAGENTA_COLOR[2], MAGENTA_COLOR[3],
		    MAGENTA_COLOR[0], MAGENTA_COLOR[1], MAGENTA_COLOR[2], MAGENTA_COLOR[3]
	};
	
	private final short[] indicies = {
			0, 1, 2,
			2, 3, 0,
			
			4, 5, 6,
			6, 7, 4,
			
			8, 9, 10,
			10, 11, 8,
			
			12, 13, 14,
			14, 15, 12,
			
			16, 17, 18,
			18, 19, 16,
			
			20, 21, 22,
			22, 23, 20
	};
	
	private ShaderProgram program;
	
	private Hierarchy hierarchy;
	private int modelToCameraMatrixUniform;
	
	private Matrix4 cameraToClipMatrix;
	private int cameraToClipMatrixUniform;
	
	private int vao;
	
	public Example6_4() {
		super("Example 6.4", 500, 500, false);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		program = new ShaderProgram(readFromFile("example6.4.vert"),readFromFile("example6.4.frag"));
		
		modelToCameraMatrixUniform = glGetUniformLocation(program.getProgram(),"modelToCameraMatrix");
		cameraToClipMatrixUniform = glGetUniformLocation(program.getProgram(),"cameraToClipMatrix");
		
		hierarchy = new Hierarchy();
		cameraToClipMatrix = new Matrix4().clearToPerspective(45*(float)Math.PI/180, getWidth(), getHeight(), 1, 100);
		
		program.begin();
		glUniformMatrix4(cameraToClipMatrixUniform, false, cameraToClipMatrix.getBuffer());
		program.end();
		
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		int vbo2 = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, (ShortBuffer)BufferUtils.createShortBuffer(indicies.length).put(indicies).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 24*3*4);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);
		
		glBindVertexArray(0);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
		
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0,1);
	}
	
	@Override
	public void update(long deltaTime) {
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
			hierarchy.adjBase(true);
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
			hierarchy.adjBase(false);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
			hierarchy.adjUpperArm(false);
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
			hierarchy.adjUpperArm(true);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_R))
			hierarchy.adjLowerArm(false);
		if(Keyboard.isKeyDown(Keyboard.KEY_F))
			hierarchy.adjLowerArm(true);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_T))
			hierarchy.adjWristPitch(false);
		if(Keyboard.isKeyDown(Keyboard.KEY_G))
			hierarchy.adjWristPitch(true);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_Z))
			hierarchy.adjWristRoll(true);
		if(Keyboard.isKeyDown(Keyboard.KEY_C))
			hierarchy.adjWristRoll(false);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_Q))
			hierarchy.adjFingerOpen(true);
		if(Keyboard.isKeyDown(Keyboard.KEY_E))
			hierarchy.adjFingerOpen(false);
	}
	
	@Override
	public void keyPressed(int key, char c, long nanos) {
		if(key == Keyboard.KEY_SPACE)
			hierarchy.writePose();
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		hierarchy.draw();
	}
	
	private static class MatrixStack {
		private Stack<Matrix4> stack;
		private Matrix4 current;
		
		public MatrixStack() {
			current = new Matrix4().clearToIdentity();
			stack = new Stack<Matrix4>();
		}
		
		public Matrix4 getTop() {
			return current;
		}
		
		public void rotateX(float angle) {
			current.rotate(angle * (float)Math.PI/180, 1, 0, 0);
		}
		
		public void rotateY(float angle) {
			current.rotate(angle * (float)Math.PI/180, 0, 1, 0);
		}
		
		public void rotateZ(float angle) {
			current.rotate(angle * (float)Math.PI/180, 0, 0, 1);
		}
		
		public void scale(Vector3 vec) {
			current.scale(vec);
		}
		
		public void translate(Vector3 vec) {
			current.translate(vec);
		}
		
		public void pushMatrix() {
			stack.push(current);
			current = new Matrix4(current);
		}
		
		public void popMatrix() {
			current = stack.pop();
		}
	}
	
	private class Hierarchy {
		private final float STANDARD_ANGLE_INCREMENT = 11.25f;
		
		private Vector3 posBase;
		private float angleBase;
		
		private Vector3 posBaseLeft, posBaseRight;
		private float scaleBaseZ;
		
		private float angleUpperArm;
		private float sizeUpperArm;
		
		private Vector3 posLowerArm;
		private float angleLowerArm;
		private float lenLowerArm;
		private float widthLowerArm;
		
		private Vector3 posWrist;
		private float angleWristRoll;
		private float angleWristPitch;
		private float lenWrist;
		private float widthWrist;
		
		private Vector3 posLeftFinger, posRightFinger;
		private float angleFingerOpen;
		private float lenFinger;
		private float widthFinger;
		private float angleLowerFinger;
		
		public Hierarchy() {
			posBase = new Vector3(3,-5,-40);
			angleBase = -45;
			
			posBaseLeft = new Vector3(2,0,0);
			posBaseRight = new Vector3(-2,0,0);
			scaleBaseZ = 3;
			
			angleUpperArm = -33.75f;
			sizeUpperArm = 9;
			
			posLowerArm = new Vector3(0,0,8);
			angleLowerArm = 146.25f;
			lenLowerArm = 5;
			widthLowerArm = 1.5f;
			
			posWrist = new Vector3(0,0,5);
			angleWristRoll = 0;
			angleWristPitch = 67.5f;
			lenWrist = 2;
			widthWrist = 2;
			
			posLeftFinger = new Vector3(1,0,1);
			posRightFinger = new Vector3(-1,0,1);
			angleFingerOpen = 180;
			lenFinger = 2;
			widthFinger = 0.5f;
			angleLowerFinger = 45;
		}
		
		public void draw() {
			MatrixStack modelToCameraStack = new MatrixStack();
			
			program.begin();
			
			glBindVertexArray(vao);
			
			modelToCameraStack.translate(posBase);
			modelToCameraStack.rotateY(angleBase);
			
			{
				modelToCameraStack.pushMatrix();
				modelToCameraStack.translate(posBaseLeft);
				modelToCameraStack.scale(new Vector3(1,1,scaleBaseZ));
				glUniformMatrix4(modelToCameraMatrixUniform, false, modelToCameraStack.getTop().getBuffer());
				glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.popMatrix();
			}
			
			{
				modelToCameraStack.pushMatrix();
				modelToCameraStack.translate(posBaseRight);
				modelToCameraStack.scale(new Vector3(1,1,scaleBaseZ));
				glUniformMatrix4(modelToCameraMatrixUniform, false, modelToCameraStack.getTop().getBuffer());
				glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.popMatrix();
			}
			
			drawUpperArm(modelToCameraStack);
			
			glBindVertexArray(0);
			
			program.end();
		}
		
		public void adjBase(boolean increment) {
			angleBase += increment ? STANDARD_ANGLE_INCREMENT : - STANDARD_ANGLE_INCREMENT;
			angleBase %= 360;
		}
		
		public void adjUpperArm(boolean increment) {
			angleUpperArm += increment ? STANDARD_ANGLE_INCREMENT : -STANDARD_ANGLE_INCREMENT;
			angleUpperArm = clamp(angleUpperArm, - 90, 0);
		}
		
		public void adjLowerArm(boolean increment) {
			angleLowerArm += increment ? STANDARD_ANGLE_INCREMENT : -STANDARD_ANGLE_INCREMENT;
			angleLowerArm = clamp(angleLowerArm, 0, 146.25f);
		}
		
		public void adjWristPitch(boolean increment) {
			angleWristPitch += increment ? STANDARD_ANGLE_INCREMENT : -STANDARD_ANGLE_INCREMENT;
			angleWristPitch = clamp(angleWristPitch, 0, 90);
		}
		
		public void adjWristRoll(boolean increment) {
			angleWristRoll += increment ? STANDARD_ANGLE_INCREMENT : -STANDARD_ANGLE_INCREMENT;
			angleWristRoll %= 360;
		}
		
		public void adjFingerOpen(boolean increment) {
			angleFingerOpen += increment ? STANDARD_ANGLE_INCREMENT : -STANDARD_ANGLE_INCREMENT;
			angleFingerOpen = clamp(angleFingerOpen, 9, 180);
		}
		
		public void writePose() {
			System.out.println("angleBase: " + angleBase);
			System.out.println("angleUpperArm: " + angleUpperArm);
			System.out.println("angleLowerArm: " + angleLowerArm);
			System.out.println("angleWristPitch: " + angleWristPitch);
			System.out.println("angleWristRoll: " + angleWristRoll);
			System.out.println("angleFingerOpenL " + angleFingerOpen);
			System.out.println();
		}
		
		private void drawFingers(MatrixStack modelToCameraStack) {
			modelToCameraStack.pushMatrix();
			modelToCameraStack.translate(posLeftFinger);
			modelToCameraStack.rotateY(angleFingerOpen);
			
			modelToCameraStack.pushMatrix();
			modelToCameraStack.translate(new Vector3(0,0,lenFinger/2));
			modelToCameraStack.scale(new Vector3(widthFinger/2,widthFinger/2,lenFinger/2));
			glUniformMatrix4(modelToCameraMatrixUniform,false,modelToCameraStack.getTop().getBuffer());
			glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.popMatrix();
			
			{
				modelToCameraStack.pushMatrix();
				modelToCameraStack.translate(new Vector3(0,0,lenFinger));
				modelToCameraStack.rotateY(-angleLowerFinger);
				
				modelToCameraStack.pushMatrix();
				modelToCameraStack.translate(new Vector3(0,0,lenFinger/2));
				modelToCameraStack.scale(new Vector3(widthFinger/2,widthFinger/2,lenFinger/2));
				glUniformMatrix4(modelToCameraMatrixUniform,false,modelToCameraStack.getTop().getBuffer());
				glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.popMatrix();
				
				modelToCameraStack.popMatrix();
			}
			
			modelToCameraStack.popMatrix();
			
			modelToCameraStack.pushMatrix();
			modelToCameraStack.translate(posRightFinger);
			modelToCameraStack.rotateY(-angleFingerOpen);
			
			modelToCameraStack.pushMatrix();
			modelToCameraStack.translate(new Vector3(0,0,lenFinger/2));
			modelToCameraStack.scale(new Vector3(widthFinger/2,widthFinger/2,lenFinger/2));
			glUniformMatrix4(modelToCameraMatrixUniform,false,modelToCameraStack.getTop().getBuffer());
			glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, 0);
			modelToCameraStack.popMatrix();
			
			{
				modelToCameraStack.pushMatrix();
				modelToCameraStack.translate(new Vector3(0,0,lenFinger));
				modelToCameraStack.rotateY(angleLowerFinger);
				
				modelToCameraStack.pushMatrix();
				modelToCameraStack.translate(new Vector3(0,0,lenFinger/2));
				modelToCameraStack.scale(new Vector3(widthFinger/2,widthFinger/2,lenFinger/2));
				glUniformMatrix4(modelToCameraMatrixUniform,false,modelToCameraStack.getTop().getBuffer());
				modelToCameraStack.popMatrix();
				
				modelToCameraStack.popMatrix();
			}
			
			modelToCameraStack.popMatrix();
		}
		
		private void drawWrist(MatrixStack modelToCameraStack) {
			modelToCameraStack.pushMatrix();
			modelToCameraStack.translate(posWrist);
			modelToCameraStack.rotateZ(angleWristRoll);
			modelToCameraStack.rotateX(angleWristPitch);
			
			modelToCameraStack.pushMatrix();
			modelToCameraStack.scale(new Vector3(widthWrist/2,widthWrist/2,lenWrist/2));
			glUniformMatrix4(modelToCameraMatrixUniform,false,modelToCameraStack.getTop().getBuffer());
			glDrawElements(GL_TRIANGLES,indicies.length,GL_UNSIGNED_SHORT,0);
			modelToCameraStack.popMatrix();
			
			drawFingers(modelToCameraStack);
			
			modelToCameraStack.popMatrix();
		}
		
		private void drawLowerArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.pushMatrix();
			modelToCameraStack.translate(posLowerArm);
			modelToCameraStack.rotateX(angleLowerArm);
			
			modelToCameraStack.pushMatrix();
			modelToCameraStack.translate(new Vector3(0,0,lenLowerArm/2));
			modelToCameraStack.scale(new Vector3(widthLowerArm/2,widthLowerArm/2,lenLowerArm/2));
			glUniformMatrix4(modelToCameraMatrixUniform,false,modelToCameraStack.getTop().getBuffer());
			glDrawElements(GL_TRIANGLES,indicies.length,GL_UNSIGNED_SHORT,0);
			modelToCameraStack.popMatrix();
			
			drawWrist(modelToCameraStack);
			
			modelToCameraStack.popMatrix();
		}
		
		private void drawUpperArm(MatrixStack modelToCameraStack) {
			modelToCameraStack.pushMatrix();
			modelToCameraStack.rotateX(angleUpperArm);
			
			{
				modelToCameraStack.pushMatrix();
				modelToCameraStack.translate(new Vector3(0,0,(sizeUpperArm/2)-1));
				modelToCameraStack.scale(new Vector3(1,1,sizeUpperArm/2));
				glUniformMatrix4(modelToCameraMatrixUniform,false,modelToCameraStack.getTop().getBuffer());
				glDrawElements(GL_TRIANGLES, indicies.length, GL_UNSIGNED_SHORT, 0);
				modelToCameraStack.popMatrix();
			}
			
			drawLowerArm(modelToCameraStack);
			
			modelToCameraStack.popMatrix();
		}
		
		private float clamp(float value, float low, float high) {
			return Math.max(Math.min(value, high), low);
		}
	}
}
