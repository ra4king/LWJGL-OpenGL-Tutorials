package com.ra4king.opengl.arcsynthesis.gl33.chapter8.example2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;

public class Example8_2 extends GLProgram {
	public static void main(String[] args) {
		new Example8_2().run(true);
	}
	
	private ShaderProgram program;
	
	private int modelToClipMatrixUniform;
	private int cameraToClipMatrixUniform;
	private int baseColorUniform;
	
	private Mesh ship;
	
	private Quaternion orientation;
	
	private boolean rightMultiply = true;
	
	public Example8_2() {
		super("Example 8.2", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0,0,0,0);
		glClearDepth(1);
		
		program = new ShaderProgram(readFromFile("example8.2.vert"), readFromFile("example8.2.frag"));
		
		modelToClipMatrixUniform = glGetUniformLocation(program.getProgram(), "modelToCameraMatrix");
		cameraToClipMatrixUniform = glGetUniformLocation(program.getProgram(), "cameraToClipMatrix");
		baseColorUniform = glGetUniformLocation(program.getProgram(), "baseColor");
		
		try {
			ship = new Mesh(getClass().getResource("example8.2.Ship.xml"));
		}
		catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		orientation = new Quaternion();
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
		
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0, 1);
	}
	
	@Override
	public void resized() {
		super.resized();
		
		program.begin();
		glUniformMatrix4(cameraToClipMatrixUniform, false, new Matrix4().clearToPerspectiveDeg(20, getWidth(), getHeight(), 1, 600).getBuffer());
		program.end();
	}
	
	private void offsetOrientation(Vector3 axis, float angle) {
		angle = angle * (float)Math.PI / 180;
		
		axis.normalize().mult((float)Math.sin(angle/2));
		
		Quaternion offset = new Quaternion(axis.x(), axis.y(), axis.z(), (float)Math.cos(angle/2));
		
		if(rightMultiply)
			orientation = orientation.mult(offset).normalize();
		else
			orientation = offset.mult(orientation);
		
		orientation.normalize();
	}
	
	@Override
	public void update(long deltaTime) {
		float speed = 90 * deltaTime / (float)1e9;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
			offsetOrientation(new Vector3(1, 0, 0), speed);
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
			offsetOrientation(new Vector3(1, 0, 0), -speed);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
			offsetOrientation(new Vector3(0, 0, 1), speed);
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
			offsetOrientation(new Vector3(0, 0, 1), -speed);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_Q))
			offsetOrientation(new Vector3(0, 1, 0), speed);
		if(Keyboard.isKeyDown(Keyboard.KEY_E))
			offsetOrientation(new Vector3(0, 1, 0), -speed);
	}
	
	@Override
	public void keyPressed(int key, char c, long nanos) {
		if(key == Keyboard.KEY_SPACE) {
			rightMultiply = !rightMultiply;
			System.out.println(rightMultiply ? "Right-multiply" : "Left-multiply");
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack stack = new MatrixStack();
		stack.getTop().translate(0, 0, -200).mult(orientation.getMatrix()).scale(3, 3, 3).rotateDeg(-90, 1, 0, 0);
		
		program.begin();
		glUniform4f(baseColorUniform, 1, 1, 1, 1);
		glUniformMatrix4(modelToClipMatrixUniform, false, stack.getTop().getBuffer());
		ship.render("tint");
		program.end();
	}
}
