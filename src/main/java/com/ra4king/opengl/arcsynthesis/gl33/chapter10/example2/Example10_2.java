package com.ra4king.opengl.arcsynthesis.gl33.chapter10.example2;

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
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Example10_2 extends GLProgram {
	public static void main(String[] args) {
		new Example10_2().run(true);
	}
	
	private ProgramData whiteDiffuseColor;
	private ProgramData vertexDiffuseColor;
	private ProgramData fragWhiteDiffuseColor;
	private ProgramData fragVertexDiffuseColor;
	
	private UnlitProgramData unlit;
	
	private int projectionUniformBuffer;
	private final int projectionBlockIndex = 2;
	
	private Mesh cylinderMesh;
	private Mesh planeMesh;
	private Mesh cubeMesh;
	
	private ViewPole viewPole;
	private ObjectPole objectPole;
	
	private Timer lightTimer = new Timer(Timer.Type.LOOP, 5);
	
	private boolean useFragmentLighting = true, drawColoredCyl, drawLight, scaleCyl;
	
	public Example10_2() {
		super("Example 10.2", 500, 500, true);
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
		
		whiteDiffuseColor = loadLitShader("example10.2.VertexLighting_PN.vert", "example10.2.frag");
		vertexDiffuseColor = loadLitShader("example10.2.VertexLighting_PCN.vert", "example10.2.frag");
		fragWhiteDiffuseColor = loadLitShader("example10.2.FragLighting_PN.vert", "example10.2.FragLighting.frag");
		fragVertexDiffuseColor = loadLitShader("example10.2.FragLighting_PCN.vert", "example10.2.FragLighting.frag");
		
		unlit = loadUnlitShader("example10.2.Transform.vert", "example10.2.UniformColor.frag");
		
		try {
			cylinderMesh = new Mesh(getClass().getResource("example10.2.UnitCylinder.xml"));
			planeMesh = new Mesh(getClass().getResource("example10.2.LargePlane.xml"));
			cubeMesh = new Mesh(getClass().getResource("example10.2.UnitCube.xml"));
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
	
	private ProgramData loadLitShader(String vertFile, String fragFile) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertFile), readFromFile(fragFile)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.modelSpaceLightPosUniform = glGetUniformLocation(data.program.getProgram(), "modelSpaceLightPos");
		data.lightIntensityUniform = glGetUniformLocation(data.program.getProgram(), "lightIntensity");
		data.ambientIntensityUniform = glGetUniformLocation(data.program.getProgram(), "ambientIntensity");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
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
	
	private float lightHeight = 1.5f, lightRadius = 1;
	
	@Override
	public void keyPressed(int key, char c, long nanos) {
		switch(key) {
			case Keyboard.KEY_SPACE:
				drawColoredCyl = !drawColoredCyl;
				break;
			case Keyboard.KEY_Y:
				drawLight = !drawLight;
				break;
			case Keyboard.KEY_H:
				useFragmentLighting = !useFragmentLighting;
				break;
			case Keyboard.KEY_B:
				lightTimer.togglePause();
				break;
		}
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
		 
		 ProgramData whiteProgram, vertColorProgram;
		 
		 if(useFragmentLighting) {
			 whiteProgram = fragWhiteDiffuseColor;
			 vertColorProgram = fragVertexDiffuseColor;
		 }
		 else {
			 whiteProgram = whiteDiffuseColor;
			 vertColorProgram = vertexDiffuseColor;
		 }
		 
		 checkGLError("1");
		 
		 whiteProgram.program.begin();
		 glUniform4f(whiteProgram.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
		 glUniform4f(whiteProgram.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
		 vertColorProgram.program.begin();
		 glUniform4f(vertColorProgram.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
		 glUniform4f(vertColorProgram.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
		 vertColorProgram.program.end();
		 
		 checkGLError("2");
		 
		 {
			 modelMatrix.pushMatrix();
			 
			 {
				 modelMatrix.pushMatrix();
				 
				 whiteProgram.program.begin();
				 glUniformMatrix4(whiteProgram.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				 
				 Vector4 lightPosModelSpace = new Matrix4(modelMatrix.getTop()).inverse().mult(lightPosCameraSpace);
				 glUniform3(whiteProgram.modelSpaceLightPosUniform, lightPosModelSpace.toBuffer());
				 
				 planeMesh.render();
				 whiteProgram.program.end();
				 
				 modelMatrix.popMatrix();
			 }
			 
			 checkGLError("3");
			 
			 {
				 modelMatrix.pushMatrix();
				 
				 modelMatrix.getTop().mult(objectPole.calcMatrix());
				 
				 if(scaleCyl)
					 modelMatrix.getTop().scale(1, 1, 0.2f);
				 
				 Vector4 lightPosModelSpace = new Matrix4(modelMatrix.getTop()).inverse().mult(lightPosCameraSpace);
				 
				 if(drawColoredCyl) {
					 vertColorProgram.program.begin();
					 glUniformMatrix4(vertColorProgram.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					 glUniform3(vertColorProgram.modelSpaceLightPosUniform, lightPosModelSpace.toBuffer());
					 cylinderMesh.render("lit-color");
					 vertColorProgram.program.end();
				 }
				 else {
					 whiteProgram.program.begin();
					 glUniformMatrix4(whiteProgram.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					 glUniform3(whiteProgram.modelSpaceLightPosUniform, lightPosModelSpace.toBuffer());
					 cylinderMesh.render("lit");
					 whiteProgram.program.end();
				 }
				 
				 modelMatrix.popMatrix();
			 }
			 
			 checkGLError("4");
			 
			 if(drawLight) {
				 modelMatrix.pushMatrix();
				 
				 modelMatrix.getTop().translate(new Vector3(worldLightPos));
				 modelMatrix.getTop().scale(0.1f, 0.1f, 0.1f);
				 
				 unlit.program.begin();
				 glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				 glUniform4f(unlit.objectColorUniform, 0.8078f, 0.8706f, 0.9922f, 1);
				 cubeMesh.render("flat");
				 
				 modelMatrix.popMatrix();
			 }
			 
			 modelMatrix.popMatrix();
		 }
	}
	
	private static class ProgramData {
		private ShaderProgram program;
		
		private int modelSpaceLightPosUniform;
		private int lightIntensityUniform;
		private int ambientIntensityUniform;
		
		private int modelToCameraMatrixUniform;
		
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
