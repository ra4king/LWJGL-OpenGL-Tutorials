package com.ra4king.opengl.arcsynthesis.gl33.chapter9.example3;

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

public class Example9_3 extends GLProgram {
	public static void main(String[] args) {
		new Example9_3().run(true);
	}
	
	private ProgramData whiteDiffuseColor;
	private ProgramData vertexDiffuseColor;
	private ProgramData whiteAmbDiffuseColor;
	private ProgramData vertexAmbDiffuseColor;
	
	private int projectionUniformBuffer;
	private final int projectionBlockIndex = 2;
	
	private Mesh cylinderMesh;
	private Mesh planeMesh;
	
	private ViewPole viewPole;
	private ObjectPole objectPole;
	
	private Vector4 lightDirection = new Vector4(0.866f, 0.5f, 0, 0);
	
	private boolean drawColoredCyl = true, showAmbient;
	
	public Example9_3() {
		super("Example 9.3", 500, 500, false);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		ViewData initialViewData = new ViewData(new Vector3(0, 0.5f, 0), new Quaternion(0.3826834f, 0, 0, 0.92387953f), 5, 0);
		ViewScale viewScale = new ViewScale(3, 20, 1.5f, 0.5f, 0, 0, 90f/250f);
		ObjectData initialObjectData = new ObjectData(new Vector3(0, 0.5f, 0), new Quaternion());
		
		viewPole = new ViewPole(initialViewData, viewScale, MouseButton.LEFT_BUTTON, false);
		objectPole = new ObjectPole(initialObjectData, 90f/250f, MouseButton.RIGHT_BUTTON, viewPole);
		
		whiteDiffuseColor = loadShader("example9.3.VertexLighting_PN.vert", "example9.3.frag");
		vertexDiffuseColor = loadShader("example9.3.VertexLighting_PCN.vert", "example9.3.frag");
		whiteAmbDiffuseColor = loadShader("example9.3.AmbientVertexLighting_PN.vert", "example9.3.frag");
		vertexAmbDiffuseColor = loadShader("example9.3.AmbientVertexLighting_PCN.vert", "example9.3.frag");
		
		try {
			cylinderMesh = new Mesh(getClass().getResource("example9.3.UnitCylinder.xml"));
			planeMesh = new Mesh(getClass().getResource("example9.3.LargePlane.xml"));
		}
		catch(Exception exc) {
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
		glBufferData(GL_UNIFORM_BUFFER, 16*4, GL_DYNAMIC_DRAW);
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, 16*4);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	private ProgramData loadShader(String vertFile, String fragFile) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertFile), readFromFile(fragFile)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.normalModelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		data.dirTolightUniform = glGetUniformLocation(data.program.getProgram(), "dirToLight");
		data.lightIntensityUniform = glGetUniformLocation(data.program.getProgram(), "lightIntensity");
		data.ambientIntensityUniform = glGetUniformLocation(data.program.getProgram(), "ambientIntensity");
		
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
	public void keyPressed(int key, char c, long nanos) {
		if(key == Keyboard.KEY_SPACE)
			drawColoredCyl = !drawColoredCyl;
		else if(key == Keyboard.KEY_T) {
			showAmbient = !showAmbient;
			
			if(showAmbient)
				System.out.println("Ambient Lighting on.");
			else
				System.out.println("Ambient Lighting off.");
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(viewPole.calcMatrix());
		
		Vector4 lightDirCameraSpace = modelMatrix.getTop().mult(lightDirection);
		
		ProgramData whiteDiffuse = showAmbient ? whiteAmbDiffuseColor : whiteDiffuseColor;
		ProgramData vertexDiffuse = showAmbient ? vertexAmbDiffuseColor : vertexDiffuseColor;
		
		if(showAmbient) {
			whiteDiffuse.program.begin();
			glUniform4f(whiteDiffuse.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
			glUniform4f(whiteDiffuse.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
			vertexDiffuse.program.begin();
			glUniform4f(vertexDiffuse.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
			glUniform4f(vertexDiffuse.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
			vertexDiffuse.program.end();
		}
		else {
			whiteDiffuse.program.begin();
			glUniform4f(whiteDiffuse.lightIntensityUniform, 1, 1, 1, 1);
			vertexDiffuse.program.begin();
			glUniform4f(vertexDiffuse.lightIntensityUniform, 1, 1, 1, 1);
			vertexDiffuse.program.end();
		}
		
		whiteDiffuse.program.begin();
		glUniform3(whiteDiffuse.dirTolightUniform, lightDirCameraSpace.toBuffer());
		vertexDiffuse.program.begin();
		glUniform3(vertexDiffuse.dirTolightUniform, lightDirCameraSpace.toBuffer());
		vertexDiffuse.program.end();
		
		{
			modelMatrix.pushMatrix();
			
			{
				modelMatrix.pushMatrix();
				
				whiteDiffuse.program.begin();
				glUniformMatrix4(whiteDiffuse.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				glUniformMatrix3(whiteDiffuse.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).toBuffer());
				planeMesh.render();
				whiteDiffuse.program.end();
				
				modelMatrix.popMatrix();
			}
			
			{
				modelMatrix.pushMatrix();
				
				modelMatrix.getTop().mult(objectPole.calcMatrix());
				
				if(drawColoredCyl) {
					vertexDiffuse.program.begin();
					glUniformMatrix4(vertexDiffuse.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					glUniformMatrix3(vertexDiffuse.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).toBuffer());
					cylinderMesh.render("lit-color");
					vertexDiffuse.program.end();
				}
				else {
					whiteDiffuse.program.begin();
					glUniformMatrix4(whiteDiffuse.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					glUniformMatrix3(whiteDiffuse.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).toBuffer());
					cylinderMesh.render("lit");
					whiteDiffuse.program.end();
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
		private int ambientIntensityUniform;
		
		private int modelToCameraMatrixUniform;
		private int normalModelToCameraMatrixUniform;
		
		public ProgramData(ShaderProgram program) {
			this.program = program;
		}
	}
}
