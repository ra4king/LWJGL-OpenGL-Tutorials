package com.ra4king.opengl.arcsynthesis.gl33.chapter11.example2;

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
import com.ra4king.opengl.util.math.Matrix3;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Example11_2 extends GLProgram {
	public static void main(String[] args) {
		new Example11_2().run(true);
	}
	
	private static enum LightingModel {
		PHONG_SPECULAR, PHONG_ONLY, BLINN_SPECULAR, BLINN_ONLY
	}
	
	private ProgramPair[] programs = new ProgramPair[LightingModel.values().length];
	private ShaderPair[] shaderFiles = { new ShaderPair("PN.vert", "PCN.vert", "PhongLighting.frag"),
										new ShaderPair("PN.vert", "PCN.vert", "PhongOnly.frag"),
										new ShaderPair("PN.vert", "PCN.vert", "BlinnLighting.frag"),
										new ShaderPair("PN.vert", "PCN.vert", "BlinnOnly.frag"),
	};
	
	private UnlitProgramData unlit;
	
	private int projectionUniformBuffer;
	private final int projectionBlockIndex = 2;
	
	private Mesh cylinderMesh;
	private Mesh planeMesh;
	private Mesh cubeMesh;
	
	private ViewPole viewPole;
	private ObjectPole objectPole;
	
	private Timer lightTimer = new Timer(Timer.Type.LOOP, 5);
	private float lightHeight = 1.5f, lightRadius = 1;
	
	private LightingModel lightModel = LightingModel.BLINN_SPECULAR;
	private MaterialParams materialParams = new MaterialParams();
	
	private boolean drawColoredCyl, drawLightSource, scaleCyl, drawDark;
	
	private final float lightAttenuation = 1.2f;
	
	private Vector4 darkColor = new Vector4(0.2f, 0.2f, 0.2f, 1);
	private Vector4 lightColor = new Vector4(1);
	
	public Example11_2() {
		super("Example 11.2", 500, 500, true);
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
		
		for(int a = 0; a < shaderFiles.length; a++)
			programs[a] = new ProgramPair(loadLitProgram("example11.2." + shaderFiles[a].whiteVertexShader, "example11.2." + shaderFiles[a].fragmentShader),
					loadLitProgram("example11.2." + shaderFiles[a].colorVertexShader, "example11.2." + shaderFiles[a].fragmentShader));
		
		unlit = loadUnlitProgramData("example11.2.PosTransform.vert", "example11.2.UniformColor.frag");
		
		try {
			cylinderMesh = new Mesh(getClass().getResource("example11.2.UnitCylinder.xml"));
			planeMesh = new Mesh(getClass().getResource("example11.2.LargePlane.xml"));
			cubeMesh = new Mesh(getClass().getResource("example11.2.UnitCube.xml"));
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
	
	private ProgramData loadLitProgram(String vertexFile, String fragmentFile) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertexFile), readFromFile(fragmentFile)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.lightIntensityUniform = glGetUniformLocation(data.program.getProgram(), "lightIntensity");
		data.ambientIntensityUniform = glGetUniformLocation(data.program.getProgram(), "ambientIntensity");
		
		data.normalModelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		data.cameraSpaceLightPositionUniform = glGetUniformLocation(data.program.getProgram(), "cameraSpaceLightPos");
		data.lightAttenuationUniform = glGetUniformLocation(data.program.getProgram(), "lightAttenuation");
		data.shininessFactorUniform = glGetUniformLocation(data.program.getProgram(), "shininessFactor");
		data.baseDiffuseColorUniform = glGetUniformLocation(data.program.getProgram(), "baseDiffuseColor");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private UnlitProgramData loadUnlitProgramData(String vertexFile, String fragmentFile) {
		UnlitProgramData data = new UnlitProgramData(new ShaderProgram(readFromFile(vertexFile), readFromFile(fragmentFile)));
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
	
	@Override
	public void keyPressed(int key, char c) {
		boolean changedShininess = false;
		boolean changedLightModel = false;
		
		switch(key) {
			case Keyboard.KEY_SPACE:
				drawColoredCyl = !drawColoredCyl;
				break;
			case Keyboard.KEY_O:
				materialParams.increment(!(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
				changedShininess = true;
				break;
			case Keyboard.KEY_U:
				materialParams.decrement(!(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)));
				changedShininess = true;
				break;
			case Keyboard.KEY_Y:
				drawLightSource = !drawLightSource;
				break;
			case Keyboard.KEY_T:
				scaleCyl = !scaleCyl;
				break;
			case Keyboard.KEY_B:
				lightTimer.togglePause();
				break;
			case Keyboard.KEY_G:
				drawDark = !drawDark;
				break;
			case Keyboard.KEY_H:
				if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					if(lightModel.ordinal()%2 != 0)
						lightModel = LightingModel.values()[(lightModel.ordinal()-1)%LightingModel.values().length];
					else
						lightModel = LightingModel.values()[(lightModel.ordinal()+1)%LightingModel.values().length];
				}
				else
					lightModel = LightingModel.values()[(lightModel.ordinal()+2)%LightingModel.values().length];
				
				changedLightModel = true;
				break;
		}
		
		if(changedShininess)
			System.out.printf("Shiny: %f\n", materialParams.getSpecularValue());
		
		if(changedLightModel)
			System.out.printf("%s\n", lightModel);
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
		
		ProgramData whiteProgram = programs[lightModel.ordinal()].whiteProgram;
		ProgramData colorProgram = programs[lightModel.ordinal()].colorProgram;
		
		whiteProgram.program.begin();
		glUniform4f(whiteProgram.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
		glUniform4f(whiteProgram.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
		glUniform3(whiteProgram.cameraSpaceLightPositionUniform, lightPosCameraSpace.toBuffer());
		glUniform1f(whiteProgram.lightAttenuationUniform, lightAttenuation);
		glUniform1f(whiteProgram.shininessFactorUniform, materialParams.getSpecularValue());
		glUniform4(whiteProgram.baseDiffuseColorUniform, drawDark ? darkColor.toBuffer() : lightColor.toBuffer());
		whiteProgram.program.end();
		
		colorProgram.program.begin();
		glUniform4f(colorProgram.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
		glUniform4f(colorProgram.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
		glUniform3(colorProgram.cameraSpaceLightPositionUniform, lightPosCameraSpace.toBuffer());
		glUniform1f(colorProgram.lightAttenuationUniform, lightAttenuation);
		glUniform1f(colorProgram.shininessFactorUniform, materialParams.getSpecularValue());
		colorProgram.program.end();
		
		{
			modelMatrix.pushMatrix();
			
			{
				modelMatrix.pushMatrix();
				
				whiteProgram.program.begin();
				glUniformMatrix4(whiteProgram.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				glUniformMatrix3(whiteProgram.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
				planeMesh.render();
				whiteProgram.program.end();
				
				modelMatrix.popMatrix();
			}
			
			{
				modelMatrix.pushMatrix();
				
				modelMatrix.getTop().mult(objectPole.calcMatrix());
				
				if(scaleCyl)
					modelMatrix.getTop().scale(1, 1, 0.2f);
				
				ProgramData prog = drawColoredCyl ? colorProgram : whiteProgram;
				
				prog.program.begin();
				glUniformMatrix4(prog.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				glUniformMatrix3(prog.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
				
				if(drawColoredCyl)
					cylinderMesh.render("lit-color");
				else
					cylinderMesh.render("lit");
				
				prog.program.end();
				
				modelMatrix.popMatrix();
			}
			
			if(drawLightSource) {
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
		private int cameraSpaceLightPositionUniform;
		private int lightAttenuationUniform;
		private int shininessFactorUniform;
		private int baseDiffuseColorUniform;
		
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
	
	private static class ProgramPair {
		private ProgramData whiteProgram;
		private ProgramData colorProgram;
		
		public ProgramPair(ProgramData whiteProgram, ProgramData colorProgram) {
			this.whiteProgram = whiteProgram;
			this.colorProgram = colorProgram;
		}
	}
	
	private static class ShaderPair {
		private String whiteVertexShader;
		private String colorVertexShader;
		private String fragmentShader;
		
		public ShaderPair(String whiteVertexShader, String colorVertexShader, String fragmentShader) {
			this.whiteVertexShader = whiteVertexShader;
			this.colorVertexShader = colorVertexShader;
			this.fragmentShader = fragmentShader;
		}
	}
	
	private class MaterialParams {
		private float phongExponent;
		private float blinnExponent;
		
		public MaterialParams() {
			phongExponent = 4;
			blinnExponent = 4;
		}
		
		public float getSpecularValue() {
			switch(lightModel) {
				case PHONG_SPECULAR:
				case PHONG_ONLY:
					return phongExponent;
				case BLINN_SPECULAR:
				case BLINN_ONLY:
					return blinnExponent;
					
				default:
					return 0;
			}
		}
		
		public void setSpecularValue(float value) {
			switch(lightModel) {
				case PHONG_SPECULAR:
				case PHONG_ONLY:
					phongExponent = value;
					break;
				case BLINN_SPECULAR:
				case BLINN_ONLY:
					blinnExponent = value;
			}
		}
		
		public void increment(boolean isLarge) {
			float param = getSpecularValue();
			
			if(isLarge)
				param += 0.5f;
			else
				param += 0.1f;
			
			setSpecularValue(clamp(param));
		}
		
		public void decrement(boolean isLarge) {
			float param = getSpecularValue();
			
			if(isLarge)
				param -= 0.5f;
			else
				param -= 0.1f;
			
			setSpecularValue(clamp(param));
		}
		
		private float clamp(float param) {
			if(param <= 0f)
				param = 0.0001f;
			return param;
		}
	}
}
