package com.ra4king.opengl.arcsynthesis.gl33.chapter17.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

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
import com.ra4king.opengl.util.math.Matrix3;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector2;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;
import com.ra4king.opengl.util.scene.Scene;
import com.ra4king.opengl.util.scene.Scene.SceneNode;
import com.ra4king.opengl.util.scene.binders.UniformIntBinder;

/**
 * @author ra4king
 */
public class Example17_1 extends GLProgram {
	public static void main(String[] args) {
		new Example17_1().run(true);
	}
	
	private Scene scene;
	private ArrayList<SceneNode> nodes;
	private Timer timer = new Timer(Type.LOOP, 10);
	
	private UniformIntBinder lightNumBinder;
	
	private ShaderProgram unlit;
	private int unlitModelToCameraMatrixUniform;
	private int unlitObjectColorUniform;
	
	private Mesh sphereMesh;
	
	private Quaternion spinBarOrient;
	
	private ViewPole viewPole;
	private ViewPole persViewPole;
	
	private int projectionUniformBuffer;
	private int lightUniformBuffer;
	
	private boolean drawCameraPos, depthClampProj = true;
	
	private static final int MAX_NUMBER_OF_LIGHTS = 4;
	
	private final int projectionBlockIndex = 0;
	private final int lightBlockIndex = 1;
	
	public Example17_1() {
		super("Example 17.1 - Double Projection", 700, 350, true);
	}
	
	@Override
	public void init() {
		glClearColor(0.8f, 0.8f, 0.8f, 1);
		glClearDepth(1);
		
		ViewData initialView = new ViewData(new Vector3(), new Quaternion(0.16043f, -0.376867f, -0.0664516f, 0.909845f), 25, 0);
		ViewScale initialViewScale = new ViewScale(5, 70, 2, 0.5f, 2, 0.5f, 90f / 250f);
		
		ViewData initPersView = new ViewData(new Vector3(), new Quaternion(), 5, 0);
		ViewScale initPersViewScale = new ViewScale(0.5f, 10, 0.1f, 0.05f, 4, 1, 90f / 250f);
		
		viewPole = new ViewPole(initialView, initialViewScale, MouseButton.LEFT_BUTTON);
		persViewPole = new ViewPole(initPersView, initPersViewScale, MouseButton.RIGHT_BUTTON);
		
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
	
	private void loadAndSetupScene() throws IOException, XmlPullParserException {
		scene = new Scene(getClass().getResource("example17.1.scene.xml"), getClass(), "example17.1.");
		
		nodes = new ArrayList<>();
		nodes.add(scene.findNode("cube"));
		nodes.add(scene.findNode("rightBar"));
		nodes.add(scene.findNode("leaningBar"));
		nodes.add(scene.findNode("spinBar"));
		
		lightNumBinder = new UniformIntBinder();
		
		for(SceneNode node : nodes) {
			lightNumBinder.associateWithProgram(node.getProgram(), "numberOfLights");
			node.setStateBinder(lightNumBinder);
		}
		
		sphereMesh = scene.findMesh("m_sphere");
		spinBarOrient = nodes.get(3).getOrient();
		
		unlit = scene.findProgram("p_unlit");
		unlitModelToCameraMatrixUniform = unlit.getUniformLocation("modelToCameraMatrix");
		unlitObjectColorUniform = unlit.getUniformLocation("objectColor");
	}
	
	@Override
	public void resized() {}
	
	@Override
	public void update(long deltaTime) {
		Utils.updateMousePoles(viewPole, persViewPole);
		
		timer.update(deltaTime);
		
		viewPole.charPress(deltaTime);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_SPACE:
				persViewPole.reset();
				break;
			case Keyboard.KEY_T:
				drawCameraPos = !drawCameraPos;
				break;
			case Keyboard.KEY_Y:
				depthClampProj = !depthClampProj;
				break;
			case Keyboard.KEY_P:
				timer.togglePause();
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
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(viewPole.calcMatrix());
		
		buildLights(modelMatrix.getTop());
		
		nodes.get(0).setOrient(Utils.angleAxisDeg(360 * timer.getAlpha(), new Vector3(0, 1, 0)));
		nodes.get(3).setOrient(spinBarOrient.copy().mult(Utils.angleAxisDeg(360 * timer.getAlpha(), new Vector3(0, 0, 1))));
		
		Vector2 displaySize = new Vector2(getWidth() / 2, getHeight());
		
		{
			glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
			glBufferData(GL_UNIFORM_BUFFER, new Matrix4().clearToPerspectiveDeg(60, displaySize.x(), displaySize.y(), 1, 1000).toBuffer(), GL_STREAM_DRAW);
			glBindBuffer(GL_UNIFORM_BUFFER, 0);
		}
		
		glViewport(0, 0, (int)displaySize.x(), (int)displaySize.y());
		scene.render(modelMatrix.getTop());
		
		if(drawCameraPos) {
			modelMatrix.pushMatrix();
			
			modelMatrix.getTop().clearToIdentity();
			modelMatrix.getTop().translate(new Vector3(0, 0, -viewPole.getView().radius));
			modelMatrix.getTop().scale(0.5f);
			
			glDisable(GL_DEPTH_TEST);
			glDepthMask(false);
			
			unlit.begin();
			
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
		
		{
			Matrix4 persMatrix = new Matrix4().clearToIdentity().set(new Matrix3(persViewPole.calcMatrix()));
			persMatrix.mult(new Matrix4().clearToPerspectiveDeg(60, displaySize.x(), displaySize.y(), 1, 1000));
			
			glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
			glBufferData(GL_UNIFORM_BUFFER, persMatrix.toBuffer(), GL_STREAM_DRAW);
			glBindBuffer(GL_UNIFORM_BUFFER, 0);
		}
		
		if(!depthClampProj)
			glDisable(GL_DEPTH_CLAMP);
		glViewport((int)(displaySize.x() + (getWidth() % 2)), 0, (int)displaySize.x(), (int)displaySize.y());
		scene.render(modelMatrix.getTop());
		glEnable(GL_DEPTH_CLAMP);
	}
	
	private void buildLights(Matrix4 cameraMatrix) {
		LightBlock lightData = new LightBlock(new Vector4(0.2f, 0.2f, 0.2f, 1), 1f / 25f, 3);
		lightData.lights[0] = new PerLight(cameraMatrix.mult(new Vector4(-0.2f, 0.5f, 0.5f, 0).normalize()), new Vector4(2, 2, 2.5f, 1));
		lightData.lights[1] = new PerLight(cameraMatrix.mult(new Vector4(5, 6, 0.5f, 1)), new Vector4(3.5f, 6.5f, 3, 1).mult(1.2f));
		
		lightNumBinder.setValue(2);
		
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
