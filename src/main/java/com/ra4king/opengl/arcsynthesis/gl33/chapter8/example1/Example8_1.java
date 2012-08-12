package com.ra4king.opengl.arcsynthesis.gl33.chapter8.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;

public class Example8_1 extends GLProgram {
	public static void main(String[] args) {
		new Example8_1().run(true);
	}
	
	private ShaderProgram program;
	
	private int modelToCameraMatrixUniform;
	private int cameraToClipMatrixUniform;
	private int baseColorUniform;
	
	private Matrix4 cameraToClipMatrix;
	
	private Mesh[] gimbals;
	private Mesh ship;
	
	private GimbalAngles angles;
	
	private boolean drawGimbals = true;
	
	public Example8_1() {
		super("Example 8.1", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		program = new ShaderProgram(readFromFile("example8.1.vert"), readFromFile("example8.1.frag"));
		
		modelToCameraMatrixUniform = glGetUniformLocation(program.getProgram(), "modelToCameraMatrix");
		cameraToClipMatrixUniform = glGetUniformLocation(program.getProgram(), "cameraToClipMatrix");
		baseColorUniform = glGetUniformLocation(program.getProgram(), "baseColor");
		
		cameraToClipMatrix = new Matrix4();
		
		String[] gimbalNames = { "example8.1.LargeGimbal.xml", "example8.1.MediumGimbal.xml", "example8.1.SmallGimbal.xml" };
		gimbals = new Mesh[gimbalNames.length];
		
		try {
			for(int a = 0; a < gimbalNames.length; a++)
				gimbals[a] = new Mesh(getClass().getResource(gimbalNames[a]));
			
			ship = new Mesh(getClass().getResource("example8.1.Ship.xml"));
		}
		catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		angles = new GimbalAngles();
		
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
		
		cameraToClipMatrix.clearToPerspective(20*(float)Math.PI/180, getWidth(), getHeight(), 1, 600);
		
		program.begin();
		glUniformMatrix4(cameraToClipMatrixUniform, false, cameraToClipMatrix.getBuffer());
		program.end();
	}
	
	@Override
	public void update(long deltaTime) {
		float speed = 9 * deltaTime / (float)1e9;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
			angles.x += speed;
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
			angles.x -= speed;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
			angles.y += speed;
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
			angles.y -= speed;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_Q))
			angles.z += speed;
		if(Keyboard.isKeyDown(Keyboard.KEY_E))
			angles.z -= speed;
	}
	
	@Override
	public void keyPressed(int key, char c, long nanos) {
		if(key == Keyboard.KEY_SPACE)
			drawGimbals = !drawGimbals;
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack stack = new MatrixStack();
		
		stack.getTop().translate(0, 0, -200);
		
		stack.getTop().rotate(angles.x, 1, 0, 0);
		drawGimbal(stack, GimbalAxis.GIMBAL_X_AXIS, 0.4f, 0.4f, 1, 1);
		stack.getTop().rotate(angles.y, 0, 1, 0);
		drawGimbal(stack, GimbalAxis.GIMBAL_Y_AXIS, 0, 1, 0, 1);
		stack.getTop().rotate(angles.z, 0, 0, 1);
		drawGimbal(stack, GimbalAxis.GIMBAL_Z_AXIS, 1, 0.3f, 0.3f, 1);
		
		program.begin();
		stack.getTop().scale(3, 3, 3).rotate(-90*(float)Math.PI/180, 1, 0, 0);
		glUniform4f(baseColorUniform, 1, 1, 1, 1);
		glUniformMatrix4(modelToCameraMatrixUniform, false, stack.getTop().getBuffer());
		ship.render("tint");
		program.end();
	}
	
	private void drawGimbal(MatrixStack stack, GimbalAxis axis, float r, float b, float g, float a) {
		if(!drawGimbals)
			return;
		
		stack.pushMatrix();
		
		switch(axis) {
			case GIMBAL_X_AXIS:
				break;
			case GIMBAL_Y_AXIS:
				stack.getTop().rotate(90, 1, 0, 0);
				stack.getTop().rotate(90, 0, 0, 1);
				break;
			case GIMBAL_Z_AXIS:
				stack.getTop().rotate(90, 1, 0, 0);
				stack.getTop().rotate(90, 0, 1, 0);
				break;
		}
		
		program.begin();
		glUniform4f(baseColorUniform, r, b, g, 1);
		glUniformMatrix4(modelToCameraMatrixUniform, false, stack.getTop().getBuffer());
		gimbals[axis.ordinal()].render();
		program.end();
		
		stack.popMatrix();
	}
	
	private static class GimbalAngles {
		private float x, y, z;
	}
	
	private static enum GimbalAxis {
		GIMBAL_X_AXIS,
		GIMBAL_Y_AXIS,
		GIMBAL_Z_AXIS
	}
}
