package com.ra4king.opengl.arcsynthesis.gl33.chapter9.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.ObjectData;
import com.ra4king.opengl.util.MousePoles.ObjectPole;
import com.ra4king.opengl.util.MousePoles.ViewData;
import com.ra4king.opengl.util.MousePoles.ViewPole;
import com.ra4king.opengl.util.MousePoles.ViewScale;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix3;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Example9_1 extends GLProgram {
	public static void main(String[] args) {
		new Example9_1().run(true);
	}
	
	private ProgramData whiteDiffuseColor;
	private ProgramData vertexDiffuseColor;
	
	private int projectionUniformBuffer;
	private final int projectionBlockIndex = 2;
	
	private Mesh cylinderMesh;
	private Mesh planeMesh;
	
	private ViewPole viewPole;
	private ObjectPole objectPole;
	
	private Vector4 lightDirection = new Vector4(0.866f, 0.5f, 0, 0);
	
	private boolean drawColoredCyl = true;
	
	public Example9_1() {
		super("Example 9.1", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		ViewData initialViewData = new ViewData(new Vector3(0, 0.5f, 0), new Quaternion(0.3826834f, 0, 0, 0.92387953f), 5, 0);
		ViewScale viewScale = new ViewScale(3, 20, 1.5f, 0.5f, 0, 0, 90f / 250f);
		ObjectData initialObjectData = new ObjectData(new Vector3(0, 0.5f, 0), new Quaternion());
		
		viewPole = new ViewPole(initialViewData, viewScale, MouseButton.LEFT_BUTTON, false);
		objectPole = new ObjectPole(initialObjectData, 90f / 250f, MouseButton.RIGHT_BUTTON, viewPole);
		
		whiteDiffuseColor = loadShader("example9.1.VertexLighting_PN.vert", "example9.1.frag");
		vertexDiffuseColor = loadShader("example9.1.VertexLighting_PCN.vert", "example9.1.frag");
		
		try {
			cylinderMesh = new Mesh(getClass().getResource("example9.1.UnitCylinder.xml"));
			planeMesh = new Mesh(getClass().getResource("example9.1.LargePlane.xml"));
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
		
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0, 1);
		glEnable(GL_DEPTH_CLAMP);
		
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, 16 * 4, GL_DYNAMIC_DRAW);
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, 16 * 4);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	private ProgramData loadShader(String vertFile, String fragFile) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertFile), readFromFile(fragFile)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.normalModelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		data.dirTolightUniform = glGetUniformLocation(data.program.getProgram(), "dirToLight");
		data.lightIntensityUniform = glGetUniformLocation(data.program.getProgram(), "lightIntensity");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	@Override
	public void resized() {
		super.resized();
		
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, new Matrix4().clearToPerspectiveDeg(45, getWidth(), getHeight(), 1, 1000).toBuffer());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	@Override
	public void update(long deltaTime) {
		Utils.updateMousePoles(viewPole, objectPole);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		if(key == Keyboard.KEY_SPACE)
			drawColoredCyl = !drawColoredCyl;
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(viewPole.calcMatrix());
		
		Vector4 lightDirCameraSpace = modelMatrix.getTop().mult(lightDirection);
		
		whiteDiffuseColor.program.begin();
		glUniform3(whiteDiffuseColor.dirTolightUniform, lightDirCameraSpace.toBuffer());
		vertexDiffuseColor.program.begin();
		glUniform3(vertexDiffuseColor.dirTolightUniform, lightDirCameraSpace.toBuffer());
		vertexDiffuseColor.program.end();
		
		{
			modelMatrix.pushMatrix();
			
			{
				modelMatrix.pushMatrix();
				
				whiteDiffuseColor.program.begin();
				glUniformMatrix4(whiteDiffuseColor.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				glUniformMatrix3(whiteDiffuseColor.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).toBuffer());
				glUniform4f(whiteDiffuseColor.lightIntensityUniform, 1, 1, 1, 1);
				planeMesh.render();
				whiteDiffuseColor.program.end();
				
				modelMatrix.popMatrix();
			}
			
			{
				modelMatrix.pushMatrix();
				
				modelMatrix.getTop().mult(objectPole.calcMatrix());
				
				if(drawColoredCyl) {
					vertexDiffuseColor.program.begin();
					glUniformMatrix4(vertexDiffuseColor.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					glUniformMatrix3(vertexDiffuseColor.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).toBuffer());
					glUniform4f(vertexDiffuseColor.lightIntensityUniform, 1, 1, 1, 1);
					cylinderMesh.render("lit-color");
					vertexDiffuseColor.program.end();
				}
				else {
					whiteDiffuseColor.program.begin();
					glUniformMatrix4(whiteDiffuseColor.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					glUniformMatrix3(whiteDiffuseColor.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).toBuffer());
					glUniform4f(whiteDiffuseColor.lightIntensityUniform, 1, 1, 1, 1);
					cylinderMesh.render("lit");
					whiteDiffuseColor.program.end();
				}
				
				modelMatrix.popMatrix();
			}
			
			modelMatrix.popMatrix();
		}
	}
	
	private static class ProgramData {
		private ShaderProgram program;
		
		private int dirTolightUniform;
		private int lightIntensityUniform;
		
		private int modelToCameraMatrixUniform;
		private int normalModelToCameraMatrixUniform;
		
		public ProgramData(ShaderProgram program) {
			this.program = program;
		}
	}
}
