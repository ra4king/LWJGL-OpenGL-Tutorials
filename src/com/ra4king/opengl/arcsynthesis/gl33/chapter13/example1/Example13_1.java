package com.ra4king.opengl.arcsynthesis.gl33.chapter13.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.ViewData;
import com.ra4king.opengl.util.MousePoles.ViewPole;
import com.ra4king.opengl.util.MousePoles.ViewScale;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.UniformBlockArray;
import com.ra4king.opengl.util.UniformBlockArray.UniformBlockObject;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix3;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Example13_1 extends GLProgram {
	public static void main(String[] args) {
		new Example13_1().run(true);
	}
	
	private ProgramMeshData litMeshProgram;
	private ProgramImposData[] litImpPrograms = new ProgramImposData[Impostors.values().length];
	private UnlitProgramData unlit;
	
	private ViewPole viewPole;
	
	private Impostors currentImpostor = Impostors.BASIC;
	private boolean[] drawImpostor = new boolean[4];
	
	private Timer sphereTimer = new Timer(Type.LOOP, 6);
	
	private int impostoreVAO;
	
	private Mesh planeMesh;
	private Mesh sphereMesh;
	private Mesh cubeMesh;
	
	private int materialUniformBuffer;
	private int lightUniformBuffer;
	private int projectionUniformBuffer;
	
	private int materialBlockOffset;
	
	private int materialBlockIndex = 0;
	private int lightBlockIndex = 1;
	private int projectionBlockIndex = 2;
	
	private final float halfLightDistance = 25;
	private final float lightAttenuation = 1f / (halfLightDistance * halfLightDistance);
	
	private boolean drawCameraPos, drawLights = true;
	
	private static final int NUMBER_OF_LIGHTS = 2;
	
	public Example13_1() {
		super("Example 13.3 - Basic Impostor", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0.75f, 0.75f, 1, 1);
		glClearDepth(1);
		
		ViewData viewData = new ViewData(new Vector3(0, 30, 25), new Quaternion(0.3826834f, 0, 0, 0.92387953f), 10, 0);
		ViewScale viewScale = new ViewScale(3, 70, 3.5f, 1.5f, 5, 1, 90 / 250f);
		
		viewPole = new ViewPole(viewData, viewScale, MouseButton.LEFT_BUTTON, false);
		
		litMeshProgram = loadLitMeshProgram("example13.1.PN.vert", "example13.1.Lighting.frag");
		
		String[] impShaderNames = {
				"BasicImpostor.vert", "BasicImpostor.frag",
				"PerspImpostor.vert", "PerspImpostor.frag",
				"DepthImpostor.vert", "DepthImpostor.frag"
		};
		
		for(int a = 0; a < impShaderNames.length / 2; a++)
			litImpPrograms[a] = loadLitImposProgram("example13.1." + impShaderNames[a * 2], "example13.1." + impShaderNames[a * 2 + 1]);
		
		unlit = loadUnlitProgram("example13.1.Unlit.vert", "example13.1.Unlit.frag");
		
		try {
			planeMesh = new Mesh(getClass().getResource("example13.1.LargePlane.xml"));
			sphereMesh = new Mesh(getClass().getResource("example13.1.UnitSphere.xml"));
			cubeMesh = new Mesh(getClass().getResource("example13.1.UnitCube.xml"));
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
		
		lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);
		
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, 16 * 4, GL_DYNAMIC_DRAW);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE);
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, 16 * 4);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		impostoreVAO = glGenVertexArrays();
		
		createMaterials();
	}
	
	private UnlitProgramData loadUnlitProgram(String vertexShader, String fragmentShader) {
		UnlitProgramData data = new UnlitProgramData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.objectColorUniform = glGetUniformLocation(data.program.getProgram(), "objectColor");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private ProgramMeshData loadLitMeshProgram(String vertexShader, String fragmentShader) {
		ProgramMeshData data = new ProgramMeshData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.normalModelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		
		int materialBlock = glGetUniformBlockIndex(data.program.getProgram(), "Material");
		int lightBlockBlock = glGetUniformBlockIndex(data.program.getProgram(), "Light");
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		
		glUniformBlockBinding(data.program.getProgram(), materialBlock, materialBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), lightBlockBlock, lightBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private ProgramImposData loadLitImposProgram(String vertexShader, String fragmentShader) {
		ProgramImposData data = new ProgramImposData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.sphereRadiusUniform = glGetUniformLocation(data.program.getProgram(), "sphereRadius");
		data.cameraSpherePosUniform = glGetUniformLocation(data.program.getProgram(), "cameraSpherePos");
		
		int materialBlock = glGetUniformBlockIndex(data.program.getProgram(), "Material");
		int lightBlockBlock = glGetUniformBlockIndex(data.program.getProgram(), "Light");
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		
		glUniformBlockBinding(data.program.getProgram(), materialBlock, materialBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), lightBlockBlock, lightBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private void createMaterials() {
		UniformBlockArray<MaterialBlock> array = new UniformBlockArray<>(MaterialBlock.SIZE, MaterialNames.values().length);
		materialBlockOffset = array.getArrayOffset();
		
		array.setBlockMember(MaterialNames.TERRAIN.ordinal(), new MaterialBlock(new Vector4(0.5f, 0.5f, 0.5f, 1), new Vector4(0.5f, 0.5f, 0.5f, 1), 0.6f));
		array.setBlockMember(MaterialNames.BLUE_SHINY.ordinal(), new MaterialBlock(new Vector4(0.1f, 0.1f, 0.8f, 1), new Vector4(0.8f, 0.8f, 0.8f, 1), 0.1f));
		array.setBlockMember(MaterialNames.GOLD_METAL.ordinal(), new MaterialBlock(new Vector4(0.803f, 0.709f, 0.15f, 1), new Vector4(0.803f, 0.709f, 0.15f, 1).mult(0.75f), 0.18f));
		array.setBlockMember(MaterialNames.DULL_GREY.ordinal(), new MaterialBlock(new Vector4(0.4f, 0.4f, 0.4f, 1), new Vector4(0.1f, 0.1f, 0.1f, 1), 0.8f));
		array.setBlockMember(MaterialNames.BLACK_SHINY.ordinal(), new MaterialBlock(new Vector4(0.05f, 0.05f, 0.05f, 1), new Vector4(0.95f, 0.95f, 0.95f, 1), 0.3f));
		
		materialUniformBuffer = array.createBufferObject();
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
		sphereTimer.update(deltaTime);
		
		Utils.updateMousePoles(viewPole, null);
		
		viewPole.charPress(deltaTime);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_P:
				sphereTimer.togglePause();
				break;
			case Keyboard.KEY_MINUS:
				sphereTimer.rewind(0.5f);
				break;
			case Keyboard.KEY_EQUALS:
				sphereTimer.fastForward(0.5f);
				break;
			case Keyboard.KEY_T:
				drawCameraPos = !drawCameraPos;
				break;
			case Keyboard.KEY_G:
				drawLights = !drawLights;
				break;
			case Keyboard.KEY_1:
				drawImpostor[0] = !drawImpostor[0];
				break;
			case Keyboard.KEY_2:
				drawImpostor[1] = !drawImpostor[1];
				break;
			case Keyboard.KEY_3:
				drawImpostor[2] = !drawImpostor[2];
				break;
			case Keyboard.KEY_4:
				drawImpostor[3] = !drawImpostor[3];
				break;
			case Keyboard.KEY_L:
				currentImpostor = Impostors.BASIC;
				break;
			case Keyboard.KEY_J:
				currentImpostor = Impostors.PERSPECTIVE;
				break;
			case Keyboard.KEY_H:
				currentImpostor = Impostors.DEPTH;
				break;
		}
	}
	
	private Vector4 calcLightPosition() {
		float currTime = sphereTimer.getAlpha();
		
		Vector4 ret = new Vector4(0, 20, 0, 1);
		ret.x((float)Math.cos(currTime * 2 * Math.PI) * 20);
		ret.z((float)Math.sin(currTime * 2 * Math.PI) * 20);
		
		return ret;
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(viewPole.calcMatrix());
		
		Matrix4 worldToCamMatrix = modelMatrix.getTop();
		
		LightBlock lightData = new LightBlock(new Vector4(0.2f, 0.2f, 0.2f, 1), lightAttenuation);
		
		lightData.lights[0] = new PerLight(worldToCamMatrix.mult(new Vector4(0.707f, 0.707f, 0, 0)), new Vector4(0.6f, 0.6f, 0.6f, 1));
		lightData.lights[1] = new PerLight(worldToCamMatrix.mult(calcLightPosition()), new Vector4(0.4f, 0.4f, 0.4f, 1));
		
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.toBuffer());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		{
			glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, MaterialNames.TERRAIN.ordinal() * materialBlockOffset, MaterialBlock.SIZE);
			
			litMeshProgram.program.begin();
			glUniformMatrix4(litMeshProgram.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniformMatrix3(litMeshProgram.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
			planeMesh.render();
			litMeshProgram.program.end();
			
			glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
		}
		
		drawSphere(modelMatrix, new Vector3(0, 10, 0), 4, MaterialNames.BLUE_SHINY, drawImpostor[0]);
		drawSphereOrbit(modelMatrix, new Vector3(0, 10, 0), new Vector3(0.6f, 0.8f, 0), 20, sphereTimer.getAlpha(), 2, MaterialNames.DULL_GREY, drawImpostor[1]);
		drawSphereOrbit(modelMatrix, new Vector3(-10, 1, 0), new Vector3(0, 1, 0), 10, sphereTimer.getAlpha(), 1, MaterialNames.BLACK_SHINY, drawImpostor[2]);
		drawSphereOrbit(modelMatrix, new Vector3(10, 1, 0), new Vector3(0, 1, 0), 10, sphereTimer.getAlpha() * 2, 1, MaterialNames.GOLD_METAL, drawImpostor[3]);
		
		if(drawLights) {
			modelMatrix.pushMatrix();
			
			modelMatrix.getTop().translate(new Vector3(calcLightPosition()));
			modelMatrix.getTop().scale(0.5f);
			
			unlit.program.begin();
			glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4(unlit.objectColorUniform, new Vector4(1).toBuffer());
			cubeMesh.render("flat");
			unlit.program.end();
			
			modelMatrix.popMatrix();
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
			cubeMesh.render("flat");
			
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(unlit.objectColorUniform, 1, 1, 1, 1);
			cubeMesh.render("flat");
			
			unlit.program.end();
		}
	}
	
	private void drawSphere(MatrixStack modelMatrix, Vector3 position, float radius, MaterialNames material, boolean drawImpostor) {
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, material.ordinal() * materialBlockOffset, MaterialBlock.SIZE);
		
		if(drawImpostor) {
			litImpPrograms[currentImpostor.ordinal()].program.begin();
			glUniform3(litImpPrograms[currentImpostor.ordinal()].cameraSpherePosUniform, modelMatrix.getTop().mult(new Vector4(position, 1)).toBuffer());
			glUniform1f(litImpPrograms[currentImpostor.ordinal()].sphereRadiusUniform, radius);
			
			glBindVertexArray(impostoreVAO);
			glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
			glBindVertexArray(0);
			
			litImpPrograms[currentImpostor.ordinal()].program.end();
		}
		else {
			modelMatrix.pushMatrix();
			modelMatrix.getTop().translate(position);
			modelMatrix.getTop().scale(radius * 2);
			
			litMeshProgram.program.begin();
			glUniformMatrix4(litMeshProgram.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniformMatrix3(litMeshProgram.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
			sphereMesh.render("lit");
			litMeshProgram.program.end();
			
			modelMatrix.popMatrix();
		}
		
		glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
	}
	
	private void drawSphereOrbit(MatrixStack modelMatrix, Vector3 orbitCenter, Vector3 orbitAxis, float orbitRadius, float orbitAlpha, float sphereRadius, MaterialNames material, boolean drawImpostor) {
		modelMatrix.pushMatrix();
		
		modelMatrix.getTop().translate(orbitCenter);
		modelMatrix.getTop().rotateDeg(360 * orbitAlpha, orbitAxis);
		
		Vector3 offsetDir = orbitAxis.copy().cross(new Vector3(0, 1, 0));
		if(offsetDir.length() < 0.001f)
			offsetDir = orbitAxis.copy().cross(new Vector3(1, 0, 0));
		
		offsetDir.normalize();
		
		modelMatrix.getTop().translate(offsetDir.mult(orbitRadius));
		
		drawSphere(modelMatrix, new Vector3(0), sphereRadius, material, drawImpostor);
		
		modelMatrix.popMatrix();
	}
	
	private static enum Impostors {
		BASIC, PERSPECTIVE, DEPTH
	}
	
	private static enum MaterialNames {
		TERRAIN, BLUE_SHINY, GOLD_METAL, DULL_GREY, BLACK_SHINY
	}
	
	private static class ProgramMeshData {
		private ShaderProgram program;
		
		private int modelToCameraMatrixUniform;
		private int normalModelToCameraMatrixUniform;
		
		public ProgramMeshData(ShaderProgram program) {
			this.program = program;
		}
	}
	
	private static class ProgramImposData {
		private ShaderProgram program;
		
		private int sphereRadiusUniform;
		private int cameraSpherePosUniform;
		
		public ProgramImposData(ShaderProgram program) {
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
		private PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
		
		public static final int SIZE = 2 * 4 * 4 + NUMBER_OF_LIGHTS * PerLight.SIZE;
		
		public LightBlock(Vector4 ambientIntensity, float lightAttenuation) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(ambientIntensity.toBuffer());
			buffer.put(lightAttenuation);
			buffer.put(new float[3]);
			
			for(PerLight light : lights)
				buffer.put(light.toBuffer());
			
			buffer.flip();
			return buffer;
		}
	}
	
	private static class MaterialBlock implements UniformBlockObject {
		private Vector4 diffuseColor;
		private Vector4 specularColor;
		private float specularShininess;
		
		public static final int SIZE = 3 * 4 * 4;
		
		public MaterialBlock(Vector4 diffuseColor, Vector4 specularColor, float specularShininess) {
			this.diffuseColor = diffuseColor;
			this.specularColor = specularColor;
			this.specularShininess = specularShininess;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(diffuseColor.toBuffer());
			buffer.put(specularColor.toBuffer());
			buffer.put(specularShininess);
			buffer.put(new float[3]);
			buffer.flip();
			return buffer;
		}
	}
}
