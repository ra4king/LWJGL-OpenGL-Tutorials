package com.ra4king.opengl.arcsynthesis.gl33.chapter8.example3;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;

public class Example8_3 extends GLProgram {
	public static void main(String[] args) {
		new Example8_3().run(true);
	}
	
	private enum OffsetRelative {
		MODEL_RELATIVE,
		WORLD_RELATIVE,
		CAMERA_RELATIVE
	}
	
	private ShaderProgram program;
	
	private int modelToCameraMatrixUniform;
	private int cameraToClipMatrixUniform;
	private int baseColorUniform;
	
	private Mesh ship, plane;
	
	private Quaternion orientation;
	private Vector3 camTarget, sphereCamRelPos;
	
	private OffsetRelative offsetRelative = OffsetRelative.MODEL_RELATIVE;
	
	public Example8_3() {
		super("Example 8.3 - Camera Relative", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		program = new ShaderProgram(readFromFile("example8.3.vert"), readFromFile("example8.3.frag"));
		
		modelToCameraMatrixUniform = program.getUniformLocation("modelToCameraMatrix");
		cameraToClipMatrixUniform = program.getUniformLocation("cameraToClipMatrix");
		baseColorUniform = program.getUniformLocation("baseColor");
		
		try {
			ship = new Mesh(getClass().getResource("example8.3.Ship.xml"));
			plane = new Mesh(getClass().getResource("example8.3.plane.xml"));
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		orientation = new Quaternion();
		camTarget = new Vector3(0, 10, 0);
		sphereCamRelPos = new Vector3(90, 0, 66);
		
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
		glUniformMatrix4(cameraToClipMatrixUniform, false, new Matrix4().clearToPerspectiveDeg(20, getWidth(), getHeight(), 1, 600).toBuffer());
		program.end();
	}
	
	private void offsetOrientation(Vector3 axis, float angle) {
		angle = angle * (float)Math.PI / 180;
		
		axis.normalize().mult((float)Math.sin(angle / 2));
		
		Quaternion offset = new Quaternion(axis.x(), axis.y(), axis.z(), (float)Math.cos(angle / 2));
		
		switch(offsetRelative) {
			case MODEL_RELATIVE:
				orientation.mult(offset);
				break;
			case WORLD_RELATIVE:
				orientation = offset.mult(orientation);
				break;
			case CAMERA_RELATIVE:
				Matrix4 camMat = calcLookAtMatrix(resolveCamPosition(), camTarget, new Vector3(0, 1, 0));
				Quaternion viewQuat = camMat.toQuaternion();
				orientation = new Quaternion(viewQuat).conjugate().mult(offset).mult(viewQuat).mult(orientation);
				break;
		}
		
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
		
		speed = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 11.25f : 112.5f) * deltaTime / (float)1e9;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_I))
			sphereCamRelPos.sub(0, speed, 0);
		if(Keyboard.isKeyDown(Keyboard.KEY_K))
			sphereCamRelPos.add(0, speed, 0);
		if(Keyboard.isKeyDown(Keyboard.KEY_J))
			sphereCamRelPos.sub(speed, 0, 0);
		if(Keyboard.isKeyDown(Keyboard.KEY_L))
			sphereCamRelPos.add(speed, 0, 0);
		
		sphereCamRelPos.y(Utils.clamp(sphereCamRelPos.y(), -78.75f, 10));
	}
	
	@Override
	public void keyPressed(int key, char c) {
		if(key == Keyboard.KEY_SPACE) {
			offsetRelative = OffsetRelative.values()[(offsetRelative.ordinal() + 1) % OffsetRelative.values().length];
			
			switch(offsetRelative) {
				case MODEL_RELATIVE:
					System.out.println("Model Relative");
					break;
				case WORLD_RELATIVE:
					System.out.println("World Relative");
					break;
				case CAMERA_RELATIVE:
					System.out.println("Camera Relative");
					break;
			}
		}
	}
	
	private Vector3 resolveCamPosition() {
		float phi = sphereCamRelPos.x() * (float)Math.PI / 180;
		float theta = (sphereCamRelPos.y() + 90) * (float)Math.PI / 180;
		
		float sinTheta = (float)Math.sin(theta);
		float cosTheta = (float)Math.cos(theta);
		float cosPhi = (float)Math.cos(phi);
		float sinPhi = (float)Math.sin(phi);
		
		return new Vector3(sinTheta * cosPhi, cosTheta, sinTheta * sinPhi).mult(sphereCamRelPos.z()).add(camTarget);
	}
	
	private Matrix4 calcLookAtMatrix(Vector3 cameraPoint, Vector3 lookPoint, Vector3 upPoint) {
		Vector3 lookDir = new Vector3(lookPoint).sub(cameraPoint).normalize();
		Vector3 upDir = upPoint.normalize();
		
		Vector3 rightDir = lookDir.cross(upDir).normalize();
		Vector3 perpUpDir = rightDir.cross(lookDir);
		
		Matrix4 rotMat = new Matrix4().clearToIdentity();
		rotMat.putColumn(0, rightDir);
		rotMat.putColumn(1, perpUpDir);
		rotMat.putColumn(2, lookDir.mult(-1));
		
		return rotMat.transpose().translate(cameraPoint.mult(-1));
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack stack = new MatrixStack();
		stack.setTop(calcLookAtMatrix(resolveCamPosition(), camTarget, new Vector3(0, 1, 0)));
		
		program.begin();
		
		{
			stack.pushMatrix();
			stack.getTop().scale(100, 1, 100);
			
			glUniform4f(baseColorUniform, 0.2f, 0.5f, 0.2f, 1);
			glUniformMatrix4(modelToCameraMatrixUniform, false, stack.getTop().toBuffer());
			
			plane.render();
			
			stack.popMatrix();
		}
		
		{
			stack.pushMatrix();
			stack.getTop().translate(camTarget).mult(orientation.toMatrix()).rotateDeg(-90, 1, 0, 0);
			
			glUniform4f(baseColorUniform, 1, 1, 1, 1);
			glUniformMatrix4(modelToCameraMatrixUniform, false, stack.getTop().toBuffer());
			
			ship.render("tint");
			
			stack.popMatrix();
		}
		
		program.end();
	}
}
