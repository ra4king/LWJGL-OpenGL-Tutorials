package com.ra4king.opengl.arcsynthesis.gl33.chapter12.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.arcsynthesis.gl33.chapter12.LightManager;
import com.ra4king.opengl.arcsynthesis.gl33.chapter12.LightManager.LightBlock;
import com.ra4king.opengl.arcsynthesis.gl33.chapter12.LightManager.SunlightValue;
import com.ra4king.opengl.arcsynthesis.gl33.chapter12.LightManager.TimerTypes;
import com.ra4king.opengl.arcsynthesis.gl33.chapter12.Scene;
import com.ra4king.opengl.arcsynthesis.gl33.chapter12.Scene.LightingProgramTypes;
import com.ra4king.opengl.arcsynthesis.gl33.chapter12.Scene.ProgramData;
import com.ra4king.opengl.arcsynthesis.gl33.chapter12.Scene.ProgramStore;
import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.ViewData;
import com.ra4king.opengl.util.MousePoles.ViewPole;
import com.ra4king.opengl.util.MousePoles.ViewScale;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Example12_1 extends GLProgram {
	public static void main(String[] args) {
		new Example12_1().run(true);
	}
	
	private ProgramData[] programs = new ProgramData[Scene.LightingProgramTypes.values().length];
	private UnlitProgramData unlit;
	
	private Scene scene;
	private LightManager lights;
	
	private ViewPole viewPole;
	
	private Vector4 skyDaylightColor = new Vector4(0.65f, 0.65f, 1, 1);
	
	private TimerTypes timerMode = TimerTypes.TIMER_ALL;
	
	private final int materialBlockIndex = 0;
	private final int lightBlockIndex = 1;
	private final int projectionBlockIndex = 2;
	
	private int lightUniformBuffer;
	private int projectionUniformBuffer;
	
	private boolean drawCameraPos, drawLights = true;
	
	public Example12_1() {
		super("Example 12.1 - Scene Lighting", 800, 800, true);
	}
	
	@Override
	public void init() {
		ViewData viewData = new ViewData(new Vector3(-59.5f, 44, 95), new Quaternion(0.3826834f, 0.0f, 0.0f, 0.92387953f), 50, 0);
		ViewScale viewScale = new ViewScale(3, 80, 4, 1, 5, 1, 90 / 250f);
		
		viewPole = new ViewPole(viewData, viewScale, MouseButton.LEFT_BUTTON);
		
		String[] vertexShaders = { "PCN", "PCN", "PN", "PN" };
		String[] fragmentShaders = { "DiffuseSpecular", "DiffuseOnly", "DiffuseSpecularMtl", "DiffuseOnlyMtl" };
		
		for(int a = 0; a < vertexShaders.length && a < fragmentShaders.length; a++)
			programs[a] = loadLitProgram("example12.1." + vertexShaders[a] + ".vert", "example12.1." + fragmentShaders[a] + ".frag");
		
		unlit = loadUnlitProgram("example12.1.PosTransform.vert", "example12.1.UniformColor.frag");
		
		scene = new Scene(new ProgramStore() {
			@Override
			public ProgramData getProgram(LightingProgramTypes type) {
				return programs[type.ordinal()];
			}
		});
		
		lights = new LightManager();
		lights.createTimer("tetra", Type.LOOP, 2.5f);
		
		setupDaytimeLighting();
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
		
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0, 1);
		glEnable(GL_DEPTH_CLAMP);
		
		lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);
		
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, 16 * 4, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE);
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, 16 * 4);
	}
	
	private void setupDaytimeLighting() {
		SunlightValue[] values = {
				new SunlightValue(0.0f / 24.0f, new Vector4(0.2f, 0.2f, 0.2f, 1.0f), new Vector4(0.6f, 0.6f, 0.6f, 1.0f), skyDaylightColor),
				new SunlightValue(4.5f / 24.0f, new Vector4(0.2f, 0.2f, 0.2f, 1.0f), new Vector4(0.6f, 0.6f, 0.6f, 1.0f), skyDaylightColor),
				new SunlightValue(6.5f / 24.0f, new Vector4(0.15f, 0.05f, 0.05f, 1.0f), new Vector4(0.3f, 0.1f, 0.10f, 1.0f), new Vector4(0.5f, 0.1f, 0.1f, 1.0f)),
				new SunlightValue(8.0f / 24.0f, new Vector4(0.0f, 0.0f, 0.0f, 1.0f), new Vector4(0.0f, 0.0f, 0.0f, 1.0f), new Vector4(0.0f, 0.0f, 0.0f, 1.0f)),
				new SunlightValue(18.0f / 24.0f, new Vector4(0.0f, 0.0f, 0.0f, 1.0f), new Vector4(0.0f, 0.0f, 0.0f, 1.0f), new Vector4(0.0f, 0.0f, 0.0f, 1.0f)),
				new SunlightValue(19.5f / 24.0f, new Vector4(0.15f, 0.05f, 0.05f, 1.0f), new Vector4(0.3f, 0.1f, 0.1f, 1.0f), new Vector4(0.5f, 0.1f, 0.1f, 1.0f)),
				new SunlightValue(20.5f / 24.0f, new Vector4(0.2f, 0.2f, 0.2f, 1.0f), new Vector4(0.6f, 0.6f, 0.6f, 1.0f), skyDaylightColor)
		};
		
		lights.setSunlightValues(values);
		
		lights.setPointLightIntensity(0, new Vector4(0.2f, 0.2f, 0.2f, 1));
		lights.setPointLightIntensity(1, new Vector4(0, 0, 0.3f, 1));
		lights.setPointLightIntensity(2, new Vector4(0.3f, 0, 0, 1));
	}
	
	private void setupNighttimeLighting() {
		SunlightValue[] values = {
				new SunlightValue(0.0f / 24.0f, new Vector4(0.2f, 0.2f, 0.2f, 1.0f), new Vector4(0.6f, 0.6f, 0.6f, 1.0f), skyDaylightColor),
				new SunlightValue(4.5f / 24.0f, new Vector4(0.2f, 0.2f, 0.2f, 1.0f), new Vector4(0.6f, 0.6f, 0.6f, 1.0f), skyDaylightColor),
				new SunlightValue(6.5f / 24.0f, new Vector4(0.15f, 0.05f, 0.05f, 1.0f), new Vector4(0.3f, 0.1f, 0.10f, 1.0f), new Vector4(0.5f, 0.1f, 0.1f, 1.0f)),
				new SunlightValue(8.0f / 24.0f, new Vector4(0.0f, 0.0f, 0.0f, 1.0f), new Vector4(0.0f, 0.0f, 0.0f, 1.0f), new Vector4(0.0f, 0.0f, 0.0f, 1.0f)),
				new SunlightValue(18.0f / 24.0f, new Vector4(0.0f, 0.0f, 0.0f, 1.0f), new Vector4(0.0f, 0.0f, 0.0f, 1.0f), new Vector4(0.0f, 0.0f, 0.0f, 1.0f)),
				new SunlightValue(19.5f / 24.0f, new Vector4(0.15f, 0.05f, 0.05f, 1.0f), new Vector4(0.3f, 0.1f, 0.1f, 1.0f), new Vector4(0.5f, 0.1f, 0.1f, 1.0f)),
				new SunlightValue(20.5f / 24.0f, new Vector4(0.2f, 0.2f, 0.2f, 1.0f), new Vector4(0.6f, 0.6f, 0.6f, 1.0f), skyDaylightColor)
		};
		
		lights.setSunlightValues(values);
		
		lights.setPointLightIntensity(0, new Vector4(0.6f, 0.6f, 0.6f, 1));
		lights.setPointLightIntensity(1, new Vector4(0, 0, 0.7f, 1));
		lights.setPointLightIntensity(2, new Vector4(0.7f, 0, 0, 1));
	}
	
	private ProgramData loadLitProgram(String vertexShader, String fragmentShader) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = data.program.getUniformLocation("modelToCameraMatrix");
		data.normalModelTocameraMatrixUniform = data.program.getUniformLocation("normalModelToCameraMatrix");
		
		int materialBlock = data.program.getUniformBlockIndex("Material");
		int lightBlock = data.program.getUniformBlockIndex("Light");
		int projectionBlock = data.program.getUniformBlockIndex("Projection");
		
		if(materialBlock != GL_INVALID_INDEX)
			glUniformBlockBinding(data.program.getProgram(), materialBlock, materialBlockIndex);
		
		glUniformBlockBinding(data.program.getProgram(), lightBlock, lightBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private UnlitProgramData loadUnlitProgram(String vertexShader, String fragmentShader) {
		UnlitProgramData data = new UnlitProgramData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = data.program.getUniformLocation("modelToCameraMatrix");
		data.objectColorUniform = data.program.getUniformLocation("objectColor");
		
		int projectionBlock = data.program.getUniformBlockIndex("Projection");
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
		lights.updateTime(deltaTime);
		
		Utils.updateMousePoles(viewPole, null);
		
		viewPole.charPress(deltaTime);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_P:
				lights.togglePause(timerMode);
				break;
			case Keyboard.KEY_MINUS:
				lights.rewindTime(timerMode, 1);
				break;
			case Keyboard.KEY_EQUALS:
				lights.fastForwardTime(timerMode, 1);
				break;
			case Keyboard.KEY_T:
				drawCameraPos = !drawCameraPos;
				break;
			case Keyboard.KEY_1:
				timerMode = TimerTypes.TIMER_ALL;
				System.out.println("All");
				break;
			case Keyboard.KEY_2:
				timerMode = TimerTypes.TIMER_SUN;
				System.out.println("Sun");
				break;
			case Keyboard.KEY_3:
				timerMode = TimerTypes.TIMER_LIGHTS;
				System.out.println("Lights");
				break;
			case Keyboard.KEY_L:
				if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
					setupNighttimeLighting();
				else
					setupDaytimeLighting();
				break;
			case Keyboard.KEY_SPACE:
				float sunAlpha = lights.getSunTime();
				float sunTimeHours = sunAlpha * 24 + 12;
				sunTimeHours = sunTimeHours > 24 ? sunTimeHours - 24 : sunTimeHours;
				int sunHours = (int)sunTimeHours;
				float sunTimeMinutes = (sunTimeHours - sunHours) * 60f;
				int sunMinutes = (int)sunTimeMinutes;
				System.out.println(sunHours + ":" + sunMinutes);
				break;
		}
	}
	
	@Override
	public void render() {
		Vector4 bkg = lights.getBackgroundColor();
		
		glClearColor(bkg.x(), bkg.y(), bkg.z(), bkg.w());
		glClearDepth(1);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(viewPole.calcMatrix());
		
		Matrix4 worldToCameraMatrix = modelMatrix.getTop();
		LightBlock lightData = lights.getLightInformation(worldToCameraMatrix);
		
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.toBuffer());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		{
			modelMatrix.pushMatrix();
			scene.draw(modelMatrix, materialBlockIndex, lights.getTimerValue("tetra"));
			modelMatrix.popMatrix();
		}
		
		{
			modelMatrix.pushMatrix();
			
			{
				modelMatrix.pushMatrix();
				
				modelMatrix.getTop().translate(new Vector3(lights.getSunlightDirection()).mult(500));
				modelMatrix.getTop().scale(30, 30, 30);
				
				unlit.program.begin();
				glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				glUniform4(unlit.objectColorUniform, lights.getSunlightIntensity().toBuffer());
				scene.sphereMesh.render("flat");
				unlit.program.end();
				
				modelMatrix.popMatrix();
			}
			
			if(drawLights) {
				for(int light = 0; light < lights.getNumberOfPointLights(); light++) {
					modelMatrix.pushMatrix();
					
					modelMatrix.getTop().translate(lights.getWorldLightPosition(light));
					
					unlit.program.begin();
					glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
					glUniform4(unlit.objectColorUniform, lights.getPointLightIntensity(light).toBuffer());
					scene.cubeMesh.render("flat");
					unlit.program.end();
					
					modelMatrix.popMatrix();
				}
			}
			
			if(drawCameraPos) {
				modelMatrix.pushMatrix();
				
				modelMatrix.getTop().clearToIdentity();
				modelMatrix.getTop().translate(0, 0, -viewPole.getView().radius);
				
				unlit.program.begin();
				
				glDisable(GL_DEPTH_TEST);
				glDepthMask(false);
				glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
				glUniform4f(unlit.objectColorUniform, 0.25f, 0.25f, 0.25f, 1);
				scene.cubeMesh.render("flat");
				
				glDepthMask(true);
				glEnable(GL_DEPTH_TEST);
				glUniform4f(unlit.objectColorUniform, 1, 1, 1, 1);
				scene.cubeMesh.render("flat");
				
				unlit.program.end();
			}
			
			modelMatrix.popMatrix();
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
