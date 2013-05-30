package com.ra4king.opengl.arcsynthesis.gl33.chapter17.example2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.xmlpull.v1.XmlPullParserException;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.ViewData;
import com.ra4king.opengl.util.MousePoles.ViewPole;
import com.ra4king.opengl.util.MousePoles.ViewScale;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;
import com.ra4king.opengl.util.scene.Scene;
import com.ra4king.opengl.util.scene.Scene.SceneNode;
import com.ra4king.opengl.util.scene.binders.UniformIntBinder;
import com.ra4king.opengl.util.scene.binders.UniformMat4Binder;
import com.ra4king.opengl.util.scene.binders.UniformVec3Binder;

import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.TextureGenerator;

/**
 * @author ra4king
 */
public class Example17_2 extends GLProgram {
	public static void main(String[] args) {
		new Example17_2().run(true);
	}
	
	private Scene scene;
	private ArrayList<SceneNode> nodes;
	
	private Timer timer = new Timer(Type.LOOP, 10);
	
	private static final int MAX_NUMBER_OF_LIGHTS = 4;
	
	private UniformIntBinder lightNumBinder;
	private UniformMat4Binder lightProjMatBinder;
	private UniformVec3Binder camLightPosBinder;
	
	private Quaternion spinBarOrient;
	
	private ShaderProgram unlit;
	private int unlitModelToCameraMatrixUniform;
	private int unlitObjectColorUniform;
	
	private ShaderProgram colored;
	private int coloredModelToCameraMatrixUniform;
	
	private Mesh sphereMesh;
	private Mesh axesMesh;
	
	private ViewPole viewPole;
	private ViewPole lightViewPole;
	
	private final String[][] texDefs = {
			{ "Flashlight.dds", "Flashlight" },
			{ "PointsOfLight.dds", "Multiple Point Lights" },
			{ "Bands.dds", "Light Bands" },
	};
	
	private final int NUM_LIGHT_TEXTURES = texDefs.length;
	
	private int[] lightTextures = new int[NUM_LIGHT_TEXTURES];
	private int currTextureIndex;
	
	private int projectionUniformBuffer;
	private int lightUniformBuffer;
	
	private final int NUM_SAMPLERS = 2;
	private int[] samplers = new int[NUM_SAMPLERS];
	private int currSampler;
	
	private final float[] lightFOVS = { 10, 20, 45, 75, 90, 120, 150, 170 };
	private int currFOVIndex = 3;
	
	private boolean drawCameraPos, showOtherLights = true;
	
	private final int projectionBlockIndex = 0;
	private final int lightBlockIndex = 1;
	private final int lightProjectionTexUnit = 3;
	
	public Example17_2() {
		super("Example 17.2 - Projected Light", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0.8f, 0.8f, 0.8f, 1);
		glClearDepth(1);
		
		ViewData initialView = new ViewData(new Vector3(0, 0, 10), new Quaternion(0.16043f, -0.376867f, -0.0664516f, 0.909845f), 25, 0);
		ViewScale initialViewScale = new ViewScale(5, 70, 2, 0.5f, 2, 0.5f, 90f / 250f);
		
		ViewData initLightView = new ViewData(new Vector3(0, 0, 20), new Quaternion(), 5, 0);
		ViewScale initLightViewScale = new ViewScale(0.05f, 10, 0.1f, 0.05f, 4, 1, 90f / 250f);
		
		viewPole = new ViewPole(initialView, initialViewScale, MouseButton.LEFT_BUTTON);
		lightViewPole = new ViewPole(initLightView, initLightViewScale, MouseButton.RIGHT_BUTTON, true);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);
		
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0, 1);
		glEnable(GL_DEPTH_CLAMP);
		glEnable(GL_FRAMEBUFFER_SRGB);
		
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, 16 * 4, GL_STREAM_DRAW);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, 16 * 4);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		createSamplers();
		loadTextures();
		
		try {
			loadAndSetupScene();
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_STREAM_DRAW);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	private void createSamplers() {
		for(int a = 0; a < samplers.length; a++) {
			samplers[a] = glGenSamplers();
			glSamplerParameteri(samplers[a], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glSamplerParameteri(samplers[a], GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}
		
		glSamplerParameteri(samplers[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(samplers[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		glSamplerParameteri(samplers[1], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glSamplerParameteri(samplers[1], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
		
		glSamplerParameter(samplers[1], GL_TEXTURE_BORDER_COLOR, new Vector4(0, 0, 0, 1).toBuffer());
	}
	
	private void loadTextures() {
		for(int tex = 0; tex < NUM_LIGHT_TEXTURES; tex++) {
			try {
				ImageSet imageSet = DdsLoader.load(getClass().getResourceAsStream("example17.2." + texDefs[tex][0]));
				lightTextures[tex] = TextureGenerator.createTexture(imageSet, 0);
			} catch(Exception exc) {
				exc.printStackTrace();
				destroy();
			}
		}
	}
	
	private void loadAndSetupScene() throws IOException, XmlPullParserException {
		scene = new Scene(getClass().getResource("example17.2.scene.xml"), getClass(), "example17.2.");
		
		nodes = new ArrayList<>();
		nodes.add(scene.findNode("cube"));
		nodes.add(scene.findNode("rightBar"));
		nodes.add(scene.findNode("leaningBar"));
		nodes.add(scene.findNode("spinBar"));
		nodes.add(scene.findNode("diorama"));
		nodes.add(scene.findNode("floor"));
		
		lightNumBinder = new UniformIntBinder();
		lightProjMatBinder = new UniformMat4Binder();
		camLightPosBinder = new UniformVec3Binder();
		
		for(SceneNode node : nodes) {
			lightNumBinder.associateWithProgram(node.getProgram(), "numberOfLights");
			lightProjMatBinder.associateWithProgram(node.getProgram(), "cameraToLightProjMatrix");
			camLightPosBinder.associateWithProgram(node.getProgram(), "cameraSpaceProjLightPos");
		}
		
		spinBarOrient = nodes.get(3).getOrient();
		
		sphereMesh = scene.findMesh("m_sphere");
		unlit = scene.findProgram("p_unlit");
		unlitModelToCameraMatrixUniform = glGetUniformLocation(unlit.getProgram(), "modelToCameraMatrix");
		unlitObjectColorUniform = glGetUniformLocation(unlit.getProgram(), "objectColor");
		
		axesMesh = scene.findMesh("m_axes");
		colored = scene.findProgram("p_colored");
		coloredModelToCameraMatrixUniform = glGetUniformLocation(colored.getProgram(), "modelToCameraMatrix");
	}
	
	@Override
	public void resized() {}
	
	@Override
	public void update(long deltaTime) {
		Utils.updateMousePoles(viewPole, lightViewPole);
		
		timer.update(deltaTime);
		
		viewPole.charPress(deltaTime);
		lightViewPole.charPress(deltaTime);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_SPACE:
				lightViewPole.reset();
				break;
			case Keyboard.KEY_T:
				drawCameraPos = !drawCameraPos;
				break;
			case Keyboard.KEY_G:
				showOtherLights = !showOtherLights;
				break;
			case Keyboard.KEY_H:
				currSampler = (currSampler + 1) % NUM_SAMPLERS;
				break;
			case Keyboard.KEY_P:
				timer.togglePause();
				break;
			case Keyboard.KEY_Y:
				currFOVIndex = Math.min(currFOVIndex + 1, lightFOVS.length - 1);
				System.out.println("Curr FOV: " + lightFOVS[currFOVIndex]);
				break;
			case Keyboard.KEY_N:
				currFOVIndex = Math.max(currFOVIndex - 1, 0);
				System.out.println("Curr FOV: " + lightFOVS[currFOVIndex]);
				break;
			case Keyboard.KEY_RETURN:
				try {
					loadAndSetupScene();
				} catch(Exception exc) {
					exc.printStackTrace();
					destroy();
				}
				break;
		}
		
		int posibleIndex = c - '1';
		if(posibleIndex >= 0 && posibleIndex < NUM_LIGHT_TEXTURES) {
			currTextureIndex = posibleIndex;
			System.out.println(texDefs[currTextureIndex][1]);
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		Matrix4 cameraMatrix = viewPole.calcMatrix();
		Matrix4 lightView = lightViewPole.calcMatrix();
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(cameraMatrix);
		
		buildLights(cameraMatrix);
		
		nodes.get(0).setOrient(Utils.angleAxisDeg(360 * timer.getAlpha(), new Vector3(0, 1, 0)));
		nodes.get(3).setOrient(spinBarOrient.copy().mult(Utils.angleAxisDeg(360 * timer.getAlpha(), new Vector3(0, 0, 1))));
		
		{
			glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
			glBufferData(GL_UNIFORM_BUFFER, new Matrix4().clearToPerspectiveDeg(60, getWidth(), getHeight(), 1, 1000).toBuffer(), GL_STREAM_DRAW);
			glBindBuffer(GL_UNIFORM_BUFFER, 0);
		}
		
		glActiveTexture(GL_TEXTURE0 + lightProjectionTexUnit);
		glBindTexture(GL_TEXTURE_2D, lightTextures[currTextureIndex]);
		glBindSampler(lightProjectionTexUnit, samplers[currSampler]);
		
		{
			Matrix4 lightProjMatrix = new Matrix4().clearToIdentity();
			lightProjMatrix.translate(0.5f, 0.5f, 0);
			lightProjMatrix.scale(0.5f, 0.5f, 1);
			lightProjMatrix.mult(new Matrix4().clearToPerspectiveDeg(lightFOVS[currFOVIndex], 1, 1, 1, 100));
			lightProjMatrix.mult(lightView);
			lightProjMatrix.mult(cameraMatrix.copy().inverse());
			
			lightProjMatBinder.setValue(lightProjMatrix);
			
			Vector4 worldLightPos = lightView.copy().inverse().getColumn(3);
			Vector3 lightPos = new Vector3(cameraMatrix.mult(worldLightPos));
			
			camLightPosBinder.setValue(lightPos);
		}
		
		glViewport(0, 0, getWidth(), getHeight());
		scene.render(modelMatrix.getTop());
		
		{
			modelMatrix.pushMatrix();
			
			modelMatrix.getTop().mult(lightView.copy().inverse());
			modelMatrix.getTop().scale(15);
			modelMatrix.getTop().scale(1, 1, -1);
			
			colored.begin();
			glUniformMatrix4(coloredModelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			axesMesh.render();
			colored.end();
			
			modelMatrix.popMatrix();
		}
		
		if(drawCameraPos) {
			modelMatrix.pushMatrix();
			modelMatrix.getTop().clearToIdentity();
			modelMatrix.getTop().translate(0, 0, -viewPole.getView().radius);
			modelMatrix.getTop().scale(0.5f);
			
			unlit.begin();
			
			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			
			glUniformMatrix4(unlitModelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4f(unlitObjectColorUniform, 0.25f, 0.25f, 0.25f, 1);
			sphereMesh.render("flat");
			
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			
			glUniform4f(unlitObjectColorUniform, 1, 1, 1, 1);
			sphereMesh.render("flat");
			
			unlit.end();
			
			modelMatrix.popMatrix();
		}
		
		glActiveTexture(GL_TEXTURE0 + lightProjectionTexUnit);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindSampler(lightProjectionTexUnit, 0);
	}
	
	private void buildLights(Matrix4 cameraMatrix) {
		LightBlock lightData = new LightBlock(new Vector4(0.2f, 0.2f, 0.2f, 1), 1f / 900f, 2);
		lightData.lights[0] = new PerLight(cameraMatrix.mult(new Vector4(-0.2f, 0.5f, 0.5f, 0).normalize()), new Vector4(0.2f, 0.2f, 0.2f, 1));
		lightData.lights[1] = new PerLight(cameraMatrix.mult(new Vector4(5, 6, 0.5f, 1)), new Vector4(3.5f, 6.5f, 3, 1).mult(0.5f));
		
		if(showOtherLights)
			lightNumBinder.setValue(2);
		else
			lightNumBinder.setValue(0);
		
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, lightData.toBuffer(), GL_STREAM_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	private static class PerLight {
		private Vector4 cameraSpaceLightPos;
		private Vector4 lightIntensity;
		
		public static final int SIZE = 2 * 4 * 4;
		
		public PerLight(Vector4 cameraSpaceLightPos, Vector4 lightIntensity) {
			this.cameraSpaceLightPos = cameraSpaceLightPos;
			this.lightIntensity = lightIntensity;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(cameraSpaceLightPos.toBuffer());
			buffer.put(lightIntensity.toBuffer());
			buffer.flip();
			return buffer;
		}
	}
	
	private static class LightBlock {
		private Vector4 ambientIntensity;
		private float lightAttenuation;
		private float maxIntensity;
		private float[] padding = new float[2];
		
		private PerLight[] lights = new PerLight[MAX_NUMBER_OF_LIGHTS];
		
		public static final int SIZE = 2 * 4 * 4 + MAX_NUMBER_OF_LIGHTS * PerLight.SIZE;
		
		public LightBlock(Vector4 ambientIntensity, float lightAttenuation, float maxIntensity) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
			this.maxIntensity = maxIntensity;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(ambientIntensity.toBuffer());
			buffer.put(lightAttenuation);
			buffer.put(maxIntensity);
			buffer.put(padding);
			
			for(PerLight light : lights)
				if(light != null)
					buffer.put(light.toBuffer());
			
			buffer.flip();
			
			return buffer;
		}
	}
}
