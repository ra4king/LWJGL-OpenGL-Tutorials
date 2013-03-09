package com.ra4king.opengl.arcsynthesis.gl33.chapter13.example2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

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
import com.ra4king.opengl.util.UniformBlockArray.UniformBlockObject;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix3;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Example13_2 extends GLProgram {
	public static void main(String[] args) {
		new Example13_2().run(true);
	}
	
	private ProgramMeshData litMeshProgram;
	private ProgramImposData litImpProgram;
	private UnlitProgramData unlit;
	
	private Mesh planeMesh;
	private Mesh cubeMesh;
	
	private ViewPole viewPole;
	
	private Timer sphereTimer = new Timer(Type.LOOP, 6);
	
	private int impostorVAO;
	private int impostorVBO;
	
	private final int materialBlockIndex = 0;
	private final int lightBlockIndex = 1;
	private final int projectionBlockIndex = 2;
	
	private int lightUniformBuffer;
	private int projectionUniformBuffer;
	private int materialArrayUniformBuffer;
	private int materialTerrainUniformBuffer;
	
	private final float halfLightDistance = 25;
	private final float lightAttenuation = 1f / (halfLightDistance * halfLightDistance);
	
	private boolean drawCameraPos, drawLights = true;
	
	private static final int NUMBER_OF_SPHERES = 4;
	private static final int NUMBER_OF_LIGHTS = 2;
	
	public Example13_2() {
		super("Example 13.2 - Geom Impostors", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0.75f, 0.75f, 1, 1);
		glClearDepth(1);
		
		ViewData viewData = new ViewData(new Vector3(0, 30, 25), new Quaternion(0.3826834f, 0, 0, 0.92387953f), 10, 0);
		ViewScale viewScale = new ViewScale(3, 70, 3.5f, 1.5f, 5, 1, 90 / 250f);
		
		viewPole = new ViewPole(viewData, viewScale, MouseButton.LEFT_BUTTON, false);
		
		litMeshProgram = loadLitMeshProgram("example13.2.PN.vert", "example13.2.Lighting.frag");
		litImpProgram = loadLitImposProgram("example13.2.GeomImpostor.vert", "example13.2.GeomImpostor.geom", "example13.2.GeomImpostor.frag");
		unlit = loadUnlitProgram("example13.2.Unlit.vert", "example13.2.Unlit.frag");
		
		try {
			planeMesh = new Mesh(getClass().getResource("example13.2.LargePlane.xml"));
			cubeMesh = new Mesh(getClass().getResource("example13.2.UnitCube.xml"));
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
		
		impostorVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, impostorVBO);
		glBufferData(GL_ARRAY_BUFFER, NUMBER_OF_SPHERES * 4 * 4, GL_STREAM_DRAW);
		
		impostorVAO = glGenVertexArrays();
		glBindVertexArray(impostorVAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 16, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 1, GL_FLOAT, false, 16, 12);
		glBindVertexArray(0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glEnable(GL_PROGRAM_POINT_SIZE);
		
		createMaterials();
	}
	
	private ProgramMeshData loadLitMeshProgram(String vertexShader, String fragmentShader) {
		ProgramMeshData data = new ProgramMeshData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrix = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.normalModelToCameraMatrix = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		
		int materialBlock = glGetUniformBlockIndex(data.program.getProgram(), "Material");
		int lightBlock = glGetUniformBlockIndex(data.program.getProgram(), "Light");
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		
		glUniformBlockBinding(data.program.getProgram(), materialBlock, materialBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), lightBlock, lightBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private ProgramImposData loadLitImposProgram(String vertexShader, String geometryShader, String fragmentShader) {
		ProgramImposData data = new ProgramImposData(new ShaderProgram(readFromFile(vertexShader), readFromFile(geometryShader), readFromFile(fragmentShader)));
		
		int materialBlock = glGetUniformBlockIndex(data.program.getProgram(), "Material");
		int lightBlock = glGetUniformBlockIndex(data.program.getProgram(), "Light");
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		
		glUniformBlockBinding(data.program.getProgram(), materialBlock, materialBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), lightBlock, lightBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private UnlitProgramData loadUnlitProgram(String vertexShader, String fragmentShader) {
		UnlitProgramData data = new UnlitProgramData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.objectColorUniform = glGetUniformLocation(data.program.getProgram(), "objectColor");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private void createMaterials() {
		ArrayList<MaterialEntry> materials = new ArrayList<>();
		materials.add(new MaterialEntry(new Vector4(0.1f, 0.1f, 0.8f, 1), new Vector4(0.8f, 0.8f, 0.8f, 1), 0.1f));
		materials.add(new MaterialEntry(new Vector4(0.4f, 0.4f, 0.4f, 1), new Vector4(0.1f, 0.1f, 0.1f, 1), 0.8f));
		materials.add(new MaterialEntry(new Vector4(0.05f, 0.05f, 0.05f, 1), new Vector4(0.95f, 0.95f, 0.95f, 1), 0.3f));
		materials.add(new MaterialEntry(new Vector4(0.803f, 0.709f, 0.15f, 1), new Vector4(0.803f, 0.709f, 0.15f, 1).mult(0.75f), 0.18f));
		
		materialArrayUniformBuffer = glGenBuffers();
		materialTerrainUniformBuffer = glGenBuffers();
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(materials.size() * MaterialEntry.SIZE / 4);
		for(MaterialEntry me : materials)
			buffer.put(me.toBuffer());
		buffer.flip();
		
		glBindBuffer(GL_UNIFORM_BUFFER, materialArrayUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, buffer, GL_STATIC_DRAW);
		
		MaterialEntry material = new MaterialEntry(new Vector4(0.5f, 0.5f, 0.5f, 1), new Vector4(0.5f, 0.5f, 0.5f, 1), 0.6f);
		glBindBuffer(GL_UNIFORM_BUFFER, materialTerrainUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, material.toBuffer(), GL_STATIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
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
		}
	}
	
	private Vector3 getSphereOrbitPosition(MatrixStack modelMatrix, Vector3 orbitCenter, Vector3 orbitAxis, float orbitRadius, float orbitAlpha) {
		modelMatrix.pushMatrix();
		
		try {
			modelMatrix.getTop().translate(orbitCenter);
			modelMatrix.getTop().rotateDeg(360 * orbitAlpha, orbitAxis);
			
			Vector3 offsetDir = orbitAxis.copy().cross(new Vector3(0, 1, 0));
			if(offsetDir.length() < 0.001f)
				offsetDir = orbitAxis.copy().cross(new Vector3(1, 0, 0));
			
			offsetDir.normalize();
			
			modelMatrix.getTop().translate(offsetDir.mult(orbitRadius));
			
			return new Vector3(modelMatrix.getTop().mult(new Vector4(0, 0, 0, 1)));
		} finally {
			modelMatrix.popMatrix();
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
			glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialTerrainUniformBuffer, 0, MaterialEntry.SIZE);
			
			litMeshProgram.program.begin();
			glUniformMatrix4(litMeshProgram.modelToCameraMatrix, false, modelMatrix.getTop().toBuffer());
			glUniformMatrix3(litMeshProgram.normalModelToCameraMatrix, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
			planeMesh.render();
			litMeshProgram.program.end();
			
			glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
		}
		
		{
			VertexData[] posSize = new VertexData[NUMBER_OF_SPHERES];
			posSize[0] = new VertexData(new Vector3(worldToCamMatrix.mult(new Vector4(0, 10, 0, 1))), 4);
			posSize[1] = new VertexData(getSphereOrbitPosition(modelMatrix, new Vector3(0, 10, 0), new Vector3(0.6f, 0.8f, 0), 20, sphereTimer.getAlpha()), 2);
			posSize[2] = new VertexData(getSphereOrbitPosition(modelMatrix, new Vector3(-10, 1, 0), new Vector3(0, 1, 0), 10, sphereTimer.getAlpha()), 1);
			posSize[3] = new VertexData(getSphereOrbitPosition(modelMatrix, new Vector3(10, 1, 0), new Vector3(0, 1, 0), 10, sphereTimer.getAlpha() * 2), 1);
			
			FloatBuffer buffer = BufferUtils.createFloatBuffer(posSize.length * VertexData.SIZE / 4);
			for(VertexData vd : posSize)
				buffer.put(vd.toBuffer());
			buffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, impostorVBO);
			glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			
			glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialArrayUniformBuffer, 0, NUMBER_OF_SPHERES * MaterialEntry.SIZE);
			
			litImpProgram.program.begin();
			glBindVertexArray(impostorVAO);
			glDrawArrays(GL_POINTS, 0, NUMBER_OF_SPHERES);
			glBindVertexArray(0);
			litImpProgram.program.end();
			
			glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
		}
		
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
	
	private static class ProgramMeshData {
		private ShaderProgram program;
		
		private int modelToCameraMatrix;
		private int normalModelToCameraMatrix;
		
		public ProgramMeshData(ShaderProgram program) {
			this.program = program;
		}
	}
	
	private static class ProgramImposData {
		private ShaderProgram program;
		
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
	
	private static class MaterialEntry implements UniformBlockObject {
		private Vector4 diffuseColor;
		private Vector4 specularColor;
		private float specularShininess;
		
		public static final int SIZE = 3 * 4 * 4;
		
		public MaterialEntry(Vector4 diffuseColor, Vector4 specularColor, float specularShininess) {
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
	
	private static class VertexData {
		private Vector3 cameraPosition;
		private float sphereRadius;
		
		public static final int SIZE = 4 * 4;
		
		public VertexData(Vector3 cameraPosition, float sphereRadius) {
			this.cameraPosition = cameraPosition;
			this.sphereRadius = sphereRadius;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(cameraPosition.toBuffer());
			buffer.put(sphereRadius);
			buffer.flip();
			return buffer;
		}
	}
}
