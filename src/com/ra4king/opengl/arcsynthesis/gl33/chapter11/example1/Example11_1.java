package com.ra4king.opengl.arcsynthesis.gl33.chapter11.example1;

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

public class Example11_1 extends GLProgram {
	public static void main(String[] args) {
		new Example11_1().run(true);
	}
	
	private static enum LightingModel {
		PURE_DIFFUSE, DIFFUSE_AND_SPECULAR, SPECULAR_ONLY
	}
	
	private ProgramData whiteNoPhong;
	private ProgramData colorNoPhong;
	
	private ProgramData whitePhong;
	private ProgramData colorPhong;
	
	private ProgramData whitePhongOnly;
	private ProgramData colorPhongOnly;
	
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
	
	private LightingModel lightModel = LightingModel.DIFFUSE_AND_SPECULAR;
	
	private boolean drawColoredCyl, drawLightSource, scaleCyl, drawDark;
	
	private final float lightAttenuation = 1.2f;
	private float shininessFactor = 4;
	
	private Vector4 darkColor = new Vector4(0.2f, 0.2f, 0.2f, 1);
	private Vector4 lightColor = new Vector4(1);
	
	public Example11_1() {
		super("Example 11.1 - Phong Lighting", 500, 500, true);
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
		
		whiteNoPhong = loadLitProgram("example11.1.PN.vert", "example11.1.NoPhong.frag");
		colorNoPhong = loadLitProgram("example11.1.PCN.vert", "example11.1.NoPhong.frag");
		
		whitePhong = loadLitProgram("example11.1.PN.vert", "example11.1.PhongLighting.frag");
		colorPhong = loadLitProgram("example11.1.PCN.vert", "example11.1.PhongLighting.frag");
		
		whitePhongOnly = loadLitProgram("example11.1.PN.vert", "example11.1.PhongOnly.frag");
		colorPhongOnly = loadLitProgram("example11.1.PCN.vert", "example11.1.PhongOnly.frag");
		
		unlit = loadUnlitProgram("example11.1.PosTransform.vert", "example11.1.UniformColor.frag");
		
		try {
			cylinderMesh = new Mesh(getClass().getResource("example11.1.UnitCylinder.xml"));
			planeMesh = new Mesh(getClass().getResource("example11.1.LargePlane.xml"));
			cubeMesh = new Mesh(getClass().getResource("example11.1.UnitCube.xml"));
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
	
	private ProgramData loadLitProgram(String vertFile, String fragFile) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertFile), readFromFile(fragFile)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.lightIntensityUniform = glGetUniformLocation(data.program.getProgram(), "lightIntensity");
		data.ambientIntensityUniform = glGetUniformLocation(data.program.getProgram(), "ambientIntensity");
		
		data.normalModelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		data.cameraSpaceLightPosUniform = glGetUniformLocation(data.program.getProgram(), "cameraSpaceLightPos");
		data.lightAttenuationUniform = glGetUniformLocation(data.program.getProgram(), "lightAttenuation");
		data.shininessFactorUniform = glGetUniformLocation(data.program.getProgram(), "shininessFactor");
		data.baseDiffuseColorUniform = glGetUniformLocation(data.program.getProgram(), "baseDiffuseColor");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private UnlitProgramData loadUnlitProgram(String vertFile, String fragFile) {
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
	
	@Override
	public void keyPressed(int key, char c) {
		boolean changedShininess = false;
		boolean changedLightModel = false;
		
		switch(key) {
			case Keyboard.KEY_SPACE:
				drawColoredCyl = !drawColoredCyl;
				break;
			case Keyboard.KEY_O:
				shininessFactor += 0.5f;
				changedShininess = true;
				break;
			case Keyboard.KEY_U:
				shininessFactor -= 0.5f;
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
				if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
					switch(lightModel) {
						case DIFFUSE_AND_SPECULAR:
							lightModel = LightingModel.PURE_DIFFUSE;
							break;
						case PURE_DIFFUSE:
							lightModel = LightingModel.DIFFUSE_AND_SPECULAR;
							break;
						case SPECULAR_ONLY:
							lightModel = LightingModel.PURE_DIFFUSE;
							break;
					}
				else
					lightModel = LightingModel.values()[(lightModel.ordinal() + 1) % LightingModel.values().length];
				
				changedLightModel = true;
				break;
		}
		
		if(shininessFactor <= 0)
			shininessFactor = 0.0001f;
		
		if(changedShininess)
			System.out.printf("Shiny: %f\n", shininessFactor);
		
		if(changedLightModel)
			System.out.printf("%s\n", lightModel.name());
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
		
		ProgramData whiteProgram;
		ProgramData colorProgram;
		
		switch(lightModel) {
			case PURE_DIFFUSE:
				whiteProgram = whiteNoPhong;
				colorProgram = colorNoPhong;
				break;
			case DIFFUSE_AND_SPECULAR:
				whiteProgram = whitePhong;
				colorProgram = colorPhong;
				break;
			case SPECULAR_ONLY:
				whiteProgram = whitePhongOnly;
				colorProgram = colorPhongOnly;
				break;
			default:
				throw new RuntimeException("How is this even possible?");
		}
		
		whiteProgram.program.begin();
		glUniform4f(whiteProgram.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
		glUniform4f(whiteProgram.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
		glUniform3(whiteProgram.cameraSpaceLightPosUniform, lightPosCameraSpace.toBuffer());
		glUniform1f(whiteProgram.lightAttenuationUniform, lightAttenuation);
		glUniform1f(whiteProgram.shininessFactorUniform, shininessFactor);
		glUniform4(whiteProgram.baseDiffuseColorUniform, drawDark ? darkColor.toBuffer() : lightColor.toBuffer());
		whiteProgram.program.end();
		
		colorProgram.program.begin();
		glUniform4f(colorProgram.lightIntensityUniform, 0.8f, 0.8f, 0.8f, 1);
		glUniform4f(colorProgram.ambientIntensityUniform, 0.2f, 0.2f, 0.2f, 1);
		glUniform3(colorProgram.cameraSpaceLightPosUniform, lightPosCameraSpace.toBuffer());
		glUniform1f(colorProgram.lightAttenuationUniform, lightAttenuation);
		glUniform1f(colorProgram.shininessFactorUniform, shininessFactor);
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
		private int cameraSpaceLightPosUniform;
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
}
