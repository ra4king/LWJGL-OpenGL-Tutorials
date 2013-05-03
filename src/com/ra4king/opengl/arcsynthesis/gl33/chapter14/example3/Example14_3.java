package com.ra4king.opengl.arcsynthesis.gl33.chapter14.example3;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.SingleImage;

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

public class Example14_3 extends GLProgram {
	public static void main(String[] args) {
		new Example14_3().run(true);
	}
	
	private ProgramData[] programs = new ProgramData[ShaderMode.values().length];
	private UnlitProgramData unlit;
	
	private ShaderPair[] shaderPairs = {
			new ShaderPair("PN.vert", "FixedShininess.frag"),
			new ShaderPair("PNT.vert", "TextureShininess.frag"),
			new ShaderPair("PNT.vert", "TextureCompute.frag")
	};
	
	private Mesh objectMesh;
	private Mesh cubeMesh;
	private Mesh planeMesh;
	
	private ViewPole viewPole;
	private ObjectPole objectPole;
	
	private ShaderMode mode = ShaderMode.MODE_FIXED;
	
	private Timer lightTimer = new Timer(Type.LOOP, 6);
	
	private int materialUniformBuffer;
	private int lightUniformBuffer;
	private int projectionUniformBuffer;
	
	private int materialOffset;
	
	private final int NUM_GAUSS_TEXTURES = 4;
	private int[] gaussTextures = new int[NUM_GAUSS_TEXTURES];
	
	private int textureSampler;
	
	private int shineTexture;
	
	private int currentTexture = NUM_GAUSS_TEXTURES - 1;
	private int currentMaterial;
	
	private boolean drawCameraPos, drawLights = true, useInfinity = true;
	
	private final int materialBlockIndex = 0;
	private final int lightBlockIndex = 1;
	private final int projectionBlockIndex = 2;
	
	private final int gaussTexUnit = 0;
	private final int shineTexUnit = 1;
	
	private final float lightAttenuation = 1f / (25f * 25f);
	
	private static final int NUMBER_OF_MATERIALS = 2;
	private static final int NUMBER_OF_LIGHTS = 2;
	
	public Example14_3() {
		super("Example 14.3 - Material Texture", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0.75f, 0.75f, 1, 1);
		glClearDepth(1);
		
		ObjectData objectData = new ObjectData(new Vector3(0, 0.5f, 0), new Quaternion());
		ViewData viewData = new ViewData(objectData.position, new Quaternion(0.3826834f, 0, 0, 0.92387953f), 10, 0);
		ViewScale viewScale = new ViewScale(1.5f, 70, 1.5f, 0.5f, 0, 0, 90 / 250f);
		
		viewPole = new ViewPole(viewData, viewScale, MouseButton.LEFT_BUTTON);
		objectPole = new ObjectPole(objectData, 90 / 250f, MouseButton.RIGHT_BUTTON, viewPole);
		
		for(int a = 0; a < shaderPairs.length; a++)
			programs[a] = loadStandardProgram("example14.3." + shaderPairs[a].vertexShader, "example14.3." + shaderPairs[a].fragmentShader);
		
		unlit = loadUnlitProgram("example14.3.Unlit.vert", "example14.3.Unlit.frag");
		
		try {
			objectMesh = new Mesh(getClass().getResource("example14.3.Infinity.xml"));
			cubeMesh = new Mesh(getClass().getResource("example14.3.UnitCube.xml"));
			planeMesh = new Mesh(getClass().getResource("example14.3.UnitPlane.xml"));
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
		
		setupMaterials();
		
		lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_DYNAMIC_DRAW);
		
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, 16 * 4, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE);
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, 16 * 4);
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, 0, MaterialBlock.SIZE);
		
		createGaussianTextures();
		createShininessTexture();
	}
	
	private ProgramData loadStandardProgram(String vertexShader, String fragmentShader) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.normalModelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		
		int materialBlock = glGetUniformBlockIndex(data.program.getProgram(), "Material");
		int lightBlock = glGetUniformBlockIndex(data.program.getProgram(), "Light");
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		
		glUniformBlockBinding(data.program.getProgram(), materialBlock, materialBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), lightBlock, lightBlockIndex);
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		int gaussianTextureUniform = glGetUniformLocation(data.program.getProgram(), "gaussianTexture");
		int shininessTextureUniform = glGetUniformLocation(data.program.getProgram(), "shininessTexture");
		
		data.program.begin();
		glUniform1i(gaussianTextureUniform, gaussTexUnit);
		glUniform1i(shininessTextureUniform, shineTexUnit);
		data.program.end();
		
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
	
	private void setupMaterials() {
		UniformBlockArray<MaterialBlock> materials = new UniformBlockArray<>(MaterialBlock.SIZE, NUMBER_OF_MATERIALS);
		
		materials.setBlockMember(0, new MaterialBlock(new Vector4(1, 0.673f, 0.043f, 1), new Vector4(1, 0.673f, 0.043f, 1).mult(0.4f), 0.125f));
		materials.setBlockMember(1, new MaterialBlock(new Vector4(0.01f, 0.01f, 0.01f, 1), new Vector4(0.99f, 0.99f, 0.99f, 1), 0.125f));
		
		materialUniformBuffer = materials.createBufferObject();
		materialOffset = materials.getArrayOffset();
	}
	
	private void createGaussianTextures() {
		for(int a = 0; a < NUM_GAUSS_TEXTURES; a++)
			gaussTextures[a] = createGaussianTexture(calcCosAngResolution(a), 128);
		
		textureSampler = glGenSamplers();
		glSamplerParameteri(textureSampler, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(textureSampler, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glSamplerParameteri(textureSampler, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(textureSampler, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}
	
	private int calcCosAngResolution(int level) {
		return 64 * (int)Math.pow(2, level);
	}
	
	private int createGaussianTexture(int cosAngleResolution, int shininessResolution) {
		ByteBuffer textureData = buildGaussianData(cosAngleResolution, shininessResolution);
		
		int gaussTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, gaussTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, cosAngleResolution, shininessResolution, 0, GL_RED, GL_UNSIGNED_BYTE, textureData);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		return gaussTexture;
	}
	
	private ByteBuffer buildGaussianData(int cosAngleResolution, int shininessResolution) {
		ByteBuffer textureData = BufferUtils.createByteBuffer(shininessResolution * cosAngleResolution);
		
		for(int a = 1; a <= shininessResolution; a++) {
			float shininess = a / (float)shininessResolution;
			
			for(int b = 0; b < cosAngleResolution; b++) {
				float cosAng = b / (float)(cosAngleResolution - 1);
				float angle = (float)Math.acos(cosAng);
				float exponent = angle / shininess;
				exponent = -(exponent * exponent);
				float gaussianTerm = (float)Math.exp(exponent);
				
				textureData.put((byte)(gaussianTerm * 255f));
			}
		}
		
		textureData.flip();
		
		return textureData;
	}
	
	private void createShininessTexture() {
		try {
			ImageSet imageSet = DdsLoader.load(getClass().getResourceAsStream("example14.3.main.dds"));
			
			SingleImage image = imageSet.getImage(0, 0, 0);
			Dimensions dims = image.getDimensions();
			
			shineTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, shineTexture);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, dims.width, dims.height, 0, GL_RED, GL_UNSIGNED_BYTE, image.getImageData());
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
			glBindTexture(GL_TEXTURE_2D, 0);
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
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
		lightTimer.update(deltaTime);
		
		Utils.updateMousePoles(viewPole, objectPole);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		final String[] shaderModeNames = {
				"Fixed Shininess with Gaussian Texture",
				"Texture shininess with Gaussian Texture",
				"Texture Shininess with computed Gaussian"
		};
		
		switch(key) {
			case Keyboard.KEY_SPACE:
				mode = ShaderMode.values()[(mode.ordinal() + 1) % ShaderMode.length];
				System.out.println(shaderModeNames[mode.ordinal()]);
				break;
			case Keyboard.KEY_P:
				lightTimer.togglePause();
				break;
			case Keyboard.KEY_MINUS:
				lightTimer.rewind(0.5f);
				break;
			case Keyboard.KEY_EQUALS:
				lightTimer.fastForward(0.5f);
				break;
			case Keyboard.KEY_T:
				drawCameraPos = !drawCameraPos;
				break;
			case Keyboard.KEY_G:
				drawLights = !drawLights;
				break;
			case Keyboard.KEY_Y:
				useInfinity = !useInfinity;
				break;
		}
		
		if(c >= '1' && c <= '9') {
			int number = c - '1';
			if(number < NUM_GAUSS_TEXTURES) {
				System.out.println("Angle resolution: " + calcCosAngResolution(number));
				currentTexture = number;
			}
			
			if(number >= 9 - NUMBER_OF_MATERIALS) {
				number -= 9 - NUMBER_OF_MATERIALS;
				System.out.println("Material Number: " + number);
				currentMaterial = number;
			}
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(viewPole.calcMatrix());
		
		Matrix4 worldToCameraMatrix = modelMatrix.getTop();
		
		LightBlock lightData = new LightBlock(new Vector4(0.2f, 0.2f, 0.2f, 1), lightAttenuation);
		
		Vector3 globalLightDirection = new Vector3(0.707f, 0.707f, 0);
		
		lightData.lights[0] = new PerLight(worldToCameraMatrix.mult(new Vector4(globalLightDirection, 0)), new Vector4(0.6f, 0.6f, 0.6f, 1));
		lightData.lights[1] = new PerLight(worldToCameraMatrix.mult(calcLightPosition()), new Vector4(0.4f, 0.4f, 0.4f, 1));
		
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData.toBuffer());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		{
			Mesh mesh = useInfinity ? objectMesh : planeMesh;
			
			glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, currentMaterial * materialOffset, MaterialBlock.SIZE);
			
			modelMatrix.pushMatrix();
			modelMatrix.getTop().mult(objectPole.calcMatrix());
			modelMatrix.getTop().scale(useInfinity ? 2 : 4);
			
			ProgramData program = programs[mode.ordinal()];
			
			program.program.begin();
			glUniformMatrix4(program.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniformMatrix3(program.normalModelToCameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
			
			glActiveTexture(GL_TEXTURE0 + gaussTexUnit);
			glBindTexture(GL_TEXTURE_2D, gaussTextures[currentTexture]);
			glBindSampler(gaussTexUnit, textureSampler);
			
			glActiveTexture(GL_TEXTURE0 + shineTexUnit);
			glBindTexture(GL_TEXTURE_2D, shineTexture);
			glBindSampler(shineTexUnit, textureSampler);
			
			if(mode != ShaderMode.MODE_FIXED)
				mesh.render("lit-tex");
			else
				mesh.render("lit");
			
			glBindSampler(gaussTexUnit, 0);
			glBindSampler(shineTexUnit, 0);
			
			glBindTexture(GL_TEXTURE_2D, 0);
			
			program.program.end();
			
			glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
			
			modelMatrix.popMatrix();
		}
		
		if(drawLights) {
			modelMatrix.pushMatrix();
			
			modelMatrix.getTop().translate(new Vector3(calcLightPosition()));
			modelMatrix.getTop().scale(0.25f);
			
			unlit.program.begin();
			
			glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4f(unlit.objectColorUniform, 1, 1, 1, 1);
			cubeMesh.render("flat");
			
			modelMatrix.popMatrix();
			modelMatrix.pushMatrix();
			
			modelMatrix.getTop().translate(globalLightDirection.copy().mult(100));
			modelMatrix.getTop().scale(5);
			
			glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
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
	
	private Vector4 calcLightPosition() {
		float currTime = lightTimer.getAlpha();
		
		Vector4 ret = new Vector4(0, 1, 0, 1);
		ret.x((float)Math.cos(currTime * 2 * Math.PI) * 3);
		ret.z((float)Math.sin(currTime * 2 * Math.PI) * 3);
		
		return ret;
	}
	
	private static class ProgramData {
		private ShaderProgram program;
		
		private int modelToCameraMatrixUniform;
		private int normalModelToCameraMatrixUniform;
		
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
	
	private static enum ShaderMode {
		MODE_FIXED, MODE_TEXTURED, MODE_TEXTURED_COMPUTE;
		
		static {
			length = values().length;
		}
		
		private static int length;
		
		public static int length() {
			return length;
		}
	}
	
	private static class ShaderPair {
		private String vertexShader, fragmentShader;
		
		public ShaderPair(String vertexShader, String fragmentShader) {
			this.vertexShader = vertexShader;
			this.fragmentShader = fragmentShader;
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
