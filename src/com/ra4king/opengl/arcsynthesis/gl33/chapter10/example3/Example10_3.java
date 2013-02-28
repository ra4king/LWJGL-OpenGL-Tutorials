package com.ra4king.opengl.arcsynthesis.gl33.chapter10.example3;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
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
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix3;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Example10_3 extends GLProgram {
	public static void main(String[] args) {
		new Example10_3().run(true);
	}
	
	private ProgramData fragWhiteDiffuseColor;
	private ProgramData fragVertexDiffuseColor;
	
	private UnlitProgramData unlit;
	
	private int projectionUniformBuffer;
	private int unprojectionUniformBuffer;
	
	private final int projectionBlockIndex = 2;
	private final int unprojectionBlockIndex = 1;
	
	private Mesh cylinderMesh;
	private Mesh planeMesh;
	private Mesh cubeMesh;
	
	private ViewPole viewPole;
	private ObjectPole objectPole;
	
	private Timer lightTimer = new Timer(Timer.Type.LOOP, 5);
	
	private float lightHeight = 1.5f, lightRadius = 1, lightAttenuation = 1;
	
	private boolean drawColoredCyl, drawLight, scaleCyl, useRSquare;
	
	public Example10_3() {
		super("Example 10.3 - Fragment Attenuation", 500, 500, true);
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
		
		fragWhiteDiffuseColor = loadLitShader("example10.3.FragLightAtten_PN.vert", "example10.3.FragLightAtten.frag");
		fragVertexDiffuseColor = loadLitShader("example10.3.FragLightAtten_PCN.vert", "example10.3.FragLightAtten.frag");
		
		unlit = loadUnlitShader("example10.3.Transform.vert", "example10.3.UniformColor.frag");
		
		try {
			cylinderMesh = new Mesh(getClass().getResource("example10.3.UnitCylinder.xml"));
			planeMesh = new Mesh(getClass().getResource("example10.3.LargePlane.xml"));
			cubeMesh = new Mesh(getClass().getResource("example10.3.UnitCube.xml"));
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
		
		unprojectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, unprojectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, 18 * 4, GL_DYNAMIC_DRAW);
		glBindBufferRange(GL_UNIFORM_BUFFER, unprojectionBlockIndex, unprojectionUniformBuffer, 0, 18 * 4);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	private ProgramData loadLitShader(String vertFile, String fragFile) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertFile), readFromFile(fragFile)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.lightIntensityUniform = glGetUniformLocation(data.program.getProgram(), "lightIntensity");
		data.ambientIntensityUniform = glGetUniformLocation(data.program.getProgram(), "ambientIntensity");
		data.normalModelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		data.cameraSpaceLightPosUniform = glGetUniformLocation(data.program.getProgram(), "cameraSpaceLightPos");
		data.lightAttenuationUniform = glGetUniformLocation(data.program.getProgram(), "lightAttenuation");
		data.useRSquareUniform = glGetUniformLocation(data.program.getProgram(), "useRSquare");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		int unprojectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "UnProjection");
		glUniformBlockBinding(data.program.getProgram(), unprojectionBlock, unprojectionBlockIndex);
		
		return data;
	}
	
	private UnlitProgramData loadUnlitShader(String vertFile, String fragFile) {
		UnlitProgramData data = new UnlitProgramData(new ShaderProgram(readFromFile(vertFile), readFromFile(fragFile)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.objectColorUniform = glGetUniformLocation(data.program.getProgram(), "objectColor");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private ByteBuffer unprojectionData = BufferUtils.createByteBuffer(18 * 4);
	
	@Override
	public void resized() {
		super.resized();
		
		Matrix4 persMatrix = new Matrix4().clearToPerspectiveDeg(45, getWidth(), getHeight(), 1, 1000);
		
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, persMatrix.toBuffer());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		unprojectionData.clear();
		unprojectionData.asFloatBuffer().put(persMatrix.inverse().toBuffer());
		unprojectionData.position(16 * 4);
		unprojectionData.asIntBuffer().put(getWidth()).put(getHeight());
		unprojectionData.position(18 * 4).flip();
		
		glBindBuffer(GL_UNIFORM_BUFFER, unprojectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, unprojectionData);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	@Override
	public void update(long deltaTime) {
		Utils.updateMousePoles(viewPole, objectPole);
		
		lightTimer.update(deltaTime);
		
		float speed = (deltaTime / (float)1e9) * (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 0.5f : 2f);
		
		if(Keyboard.isKeyDown(Keyboard.KEY_I))
			lightHeight += speed;
		if(Keyboard.isKeyDown(Keyboard.KEY_K))
			lightHeight -= speed;
		if(Keyboard.isKeyDown(Keyboard.KEY_L))
			lightRadius += speed;
		if(Keyboard.isKeyDown(Keyboard.KEY_J))
			lightRadius -= speed;
		
		if(lightRadius < 0.2f)
			lightRadius = 0.2f;
	}
	
	@Override
	public void keyPressed(int key, char c) {
		boolean changedAtten = false;
		
		switch(key) {
			case Keyboard.KEY_SPACE:
				drawColoredCyl = !drawColoredCyl;
				break;
			case Keyboard.KEY_O:
				lightAttenuation *= Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 1.1f : 1.5f;
				changedAtten = true;
				break;
			case Keyboard.KEY_U:
				lightAttenuation /= Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 1.1f : 1.5f;
				changedAtten = true;
				break;
			case Keyboard.KEY_Y:
				drawLight = !drawLight;
				break;
			case Keyboard.KEY_T:
				scaleCyl = !scaleCyl;
				break;
			case Keyboard.KEY_H:
				useRSquare = !useRSquare;
				if(useRSquare)
					System.out.println("Inverse Squared Attenuation");
				else
					System.out.println("Plain Inverse Attenuation");
				
				break;
			case Keyboard.KEY_B:
				lightTimer.togglePause();
				break;
		}
		
		if(lightAttenuation < 0.1f)
			lightAttenuation = 0.1f;
		
		if(changedAtten)
			System.out.println("Atten: " + lightAttenuation);
	}
	
	private Vector4 calcLightPosition() {
		float currTime = lightTimer.getAlpha();
		
		Vector4 ret = new Vector4(0, lightHeight, 0, 1);
		ret.x((float)Math.cos(currTime * 2 * Math.PI) * lightRadius);
		ret.z((float)Math.sin(currTime * 2 * Math.PI) * lightRadius);
		
		return ret;
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(viewPole.calcMatrix());
		
		Vector4 worldLightPos = calcLightPosition();
		Vector4 lightPosCameraSpace = modelMatrix.getTop().mult(worldLightPos);
		
		fragWhiteDiffuseColor.program.begin();
		glUniform4f(fragWhiteDiffuseColor.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
		glUniform4f(fragWhiteDiffuseColor.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
		glUniform3(fragWhiteDiffuseColor.cameraSpaceLightPosUniform, lightPosCameraSpace.toBuffer());
		glUniform1f(fragWhiteDiffuseColor.lightAttenuationUniform, lightAttenuation);
		glUniform1i(fragWhiteDiffuseColor.useRSquareUniform, useRSquare ? 1 : 0);
		fragWhiteDiffuseColor.program.end();
		
		fragVertexDiffuseColor.program.begin();
		glUniform4f(fragVertexDiffuseColor.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
		glUniform4f(fragVertexDiffuseColor.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
		glUniform3(fragVertexDiffuseColor.cameraSpaceLightPosUniform, lightPosCameraSpace.toBuffer());
		glUniform1f(fragVertexDiffuseColor.lightAttenuationUniform, lightAttenuation);
		glUniform1i(fragVertexDiffuseColor.useRSquareUniform, useRSquare ? 1 : 0);
		fragVertexDiffuseColor.program.end();
		
		{
			modelMatrix.pushMatrix();
			
			{
				modelMatrix.pushMatrix();
				
				fragWhiteDiffuseColor.program.begin();
				glUniformMatrix4(fragWhiteDiffuseColor.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				glUniformMatrix3(fragWhiteDiffuseColor.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
				planeMesh.render();
				fragWhiteDiffuseColor.program.end();
				
				modelMatrix.popMatrix();
			}
			
			{
				modelMatrix.pushMatrix();
				
				modelMatrix.getTop().mult(objectPole.calcMatrix());
				
				if(scaleCyl)
					modelMatrix.getTop().scale(1, 1, 0.2f);
				
				Matrix3 normMatrix = new Matrix3(modelMatrix.getTop()).inverse().transpose();
				
				if(drawColoredCyl) {
					fragVertexDiffuseColor.program.begin();
					glUniformMatrix4(fragVertexDiffuseColor.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					glUniformMatrix3(fragVertexDiffuseColor.normalModelToCameraMatrixUniform, false, normMatrix.toBuffer());
					cylinderMesh.render("lit-color");
					fragVertexDiffuseColor.program.end();
				} else {
					fragWhiteDiffuseColor.program.begin();
					glUniformMatrix4(fragWhiteDiffuseColor.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					glUniformMatrix3(fragWhiteDiffuseColor.normalModelToCameraMatrixUniform, false, normMatrix.toBuffer());
					cylinderMesh.render("lit");
					fragWhiteDiffuseColor.program.end();
				}
				
				modelMatrix.popMatrix();
			}
			
			if(drawLight) {
				modelMatrix.pushMatrix();
				
				modelMatrix.getTop().translate(new Vector3(worldLightPos));
				modelMatrix.getTop().scale(0.1f, 0.1f, 0.1f);
				
				unlit.program.begin();
				glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				glUniform4f(unlit.objectColorUniform, 0.8078f, 0.8706f, 0.9922f, 1);
				cubeMesh.render("flat");
				unlit.program.end();
				
				modelMatrix.popMatrix();
			}
			
			modelMatrix.popMatrix();
		}
	}
	
	private static class ProgramData {
		private ShaderProgram program;
		
		private int modelToCameraMatrixUniform;
		
		private int lightIntensityUniform;
		private int ambientIntensityUniform;
		
		private int normalModelToCameraMatrixUniform;
		private int cameraSpaceLightPosUniform;
		private int lightAttenuationUniform;
		private int useRSquareUniform;
		
		public ProgramData(ShaderProgram program) {
			this.program = program;
		}
	}
	
	private static class UnlitProgramData {
		private ShaderProgram program;
		
		private int objectColorUniform;
		private int modelToCameraMatrixUniform;
		
		public UnlitProgramData(ShaderProgram program) {
			this.program = program;
		}
	}
}
