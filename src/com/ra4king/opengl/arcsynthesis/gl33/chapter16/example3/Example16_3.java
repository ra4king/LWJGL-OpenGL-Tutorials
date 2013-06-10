package com.ra4king.opengl.arcsynthesis.gl33.chapter16.example3;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.PixelFormat;

import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.SingleImage;
import rosick.jglsdk.glimg.TextureGenerator;
import rosick.jglsdk.glimg.TextureGenerator.OpenGLPixelTransferParams;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.arcsynthesis.gl33.chapter16.example3.LightEnv.LightBlock;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.ViewData;
import com.ra4king.opengl.util.MousePoles.ViewPole;
import com.ra4king.opengl.util.MousePoles.ViewScale;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

/**
 * @author ra4king
 */
public class Example16_3 extends GLProgram {
	public static void main(String[] args) {
		new Example16_3().run(true, new PixelFormat().withSRGB(true));
	}
	
	private ProgramData standard;
	private UnlitProgramData unlit;
	
	private Mesh terrain;
	private Mesh sphere;
	
	private ViewPole viewPole;
	
	private LightEnv lightEnv;
	
	private int projectionUniformBuffer;
	private int lightUniformBuffer;
	private int linearTexture;
	
	private final int NUM_SAMPLERS = 2;
	private int[] samplers = new int[NUM_SAMPLERS];
	
	private int currSampler = 0;
	
	private boolean drawCameraPos, useGammaDisplay = true;
	
	private final int projectionBlockIndex = 0;
	private final int lightBlockIndex = 1;
	private final int colorTexUnit = 2;
	
	public Example16_3() {
		super("Example 16.3 - Gamma Landscape", 800, 600, true);
	}
	
	@Override
	public void init() {
		glClearDepth(1);
		
		ViewData viewData = new ViewData(new Vector3(-60.257084f, 10.947238f, 62.636356f), new Quaternion(-0.099283f, -0.211198f, -0.020028f, -0.972817f), 30, 0);
		ViewScale viewScale = new ViewScale(5, 90, 2, 0.5f, 4, 1, 90f / 250f);
		
		viewPole = new ViewPole(viewData, viewScale, MouseButton.LEFT_BUTTON);
		
		try {
			lightEnv = new LightEnv(getClass().getResource("example16.3.LightEnv.xml"));
			
			terrain = new Mesh(getClass().getResource("example16.3.Terrain.xml"));
			sphere = new Mesh(getClass().getResource("example16.3.UnitSphere.xml"));
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		standard = loadProgram("example16.3.PNT.vert", "example16.3.LitTexture.frag");
		unlit = loadUnlitProgram("example16.3.Unlit.vert", "example16.3.Unlit.frag");
		
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
		
		lightUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, LightBlock.SIZE, GL_STREAM_DRAW);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, lightUniformBuffer, 0, LightBlock.SIZE);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		loadTextures();
		createSamplers();
	}
	
	private ProgramData loadProgram(String vertexShader, String fragmentShader) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.numberOfLightsUniform = glGetUniformLocation(data.program.getProgram(), "numberOfLights");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		int lightBlock = glGetUniformBlockIndex(data.program.getProgram(), "Light");
		glUniformBlockBinding(data.program.getProgram(), lightBlock, lightBlockIndex);
		
		int colorTextureUniform = glGetUniformLocation(data.program.getProgram(), "diffuseColorTex");
		data.program.begin();
		glUniform1i(colorTextureUniform, colorTexUnit);
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
	
	private void loadTextures() {
		try {
			ImageSet imageSet = DdsLoader.load(getClass().getResourceAsStream("example16.3.Terrain_tex.dds"));
			
			linearTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, linearTexture);
			
			OpenGLPixelTransferParams xfer = TextureGenerator.getUploadFormatType(imageSet.getFormat(), 0);
			
			for(int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
				Dimensions dims = image.getDimensions();
				
				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8_ALPHA8, dims.width, dims.height, 0, xfer.format, xfer.type, image.getImageData());
			}
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
	}
	
	private void createSamplers() {
		for(int a = 0; a < samplers.length; a++) {
			samplers[a] = glGenSamplers();
			
			glSamplerParameteri(samplers[a], GL_TEXTURE_WRAP_S, GL_REPEAT);
			glSamplerParameteri(samplers[a], GL_TEXTURE_WRAP_T, GL_REPEAT);
		}
		
		glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		
		float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
		glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(samplers[1], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
	}
	
	@Override
	public void resized() {
		super.resized();
		
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, new Matrix4().clearToPerspectiveDeg(60, getWidth(), getHeight(), 1, 1000).toBuffer());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	@Override
	public void update(long deltaTime) {
		Utils.updateMousePoles(viewPole, null);
		
		lightEnv.updateTime(deltaTime);
		
		viewPole.charPress(deltaTime);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_SPACE:
				useGammaDisplay = !useGammaDisplay;
				break;
			case Keyboard.KEY_MINUS:
				lightEnv.rewindTime(1);
				break;
			case Keyboard.KEY_EQUALS:
				lightEnv.fastForwardTime(1);
				break;
			case Keyboard.KEY_T:
				drawCameraPos = !drawCameraPos;
				break;
			case Keyboard.KEY_P:
				lightEnv.togglePause();
				break;
		}
		
		if(c >= '1' && c <= '9') {
			int number = c - '1';
			if(number < NUM_SAMPLERS)
				currSampler = number;
		}
	}
	
	@Override
	public void render() {
		if(useGammaDisplay)
			glEnable(GL_FRAMEBUFFER_SRGB);
		else
			glDisable(GL_FRAMEBUFFER_SRGB);
		
		Vector4 bgColor = lightEnv.getBackgroundColor();
		glClearColor(bgColor.x(), bgColor.y(), bgColor.z(), bgColor.w());
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(viewPole.calcMatrix());
		
		LightBlock lightData = lightEnv.getLightBlock(modelMatrix.getTop());
		
		glBindBuffer(GL_UNIFORM_BUFFER, lightUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, lightData.toBuffer(), GL_STREAM_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		modelMatrix.pushMatrix();
		modelMatrix.getTop().rotateDeg(-90, 1, 0, 0);
		
		standard.program.begin();
		glUniformMatrix4(standard.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
		glUniform1i(standard.numberOfLightsUniform, lightEnv.getNumLights());
		
		glActiveTexture(GL_TEXTURE0 + colorTexUnit);
		glBindTexture(GL_TEXTURE_2D, linearTexture);
		glBindSampler(colorTexUnit, samplers[currSampler]);
		
		terrain.render("lit-tex");
		
		glBindSampler(colorTexUnit, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
		standard.program.end();
		modelMatrix.popMatrix();
		
		{
			modelMatrix.pushMatrix();
			
			Vector3 sunlightDir = new Vector3(lightEnv.getSunlightDirection());
			modelMatrix.getTop().translate(sunlightDir.mult(500));
			modelMatrix.getTop().scale(30, 30, 30);
			
			unlit.program.begin();
			glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4(unlit.objectColorUniform, lightEnv.getSunlightScaledIntensity().toBuffer());
			sphere.render("flat");
			unlit.program.end();
			
			modelMatrix.popMatrix();
		}
		
		for(int light = 0; light < lightEnv.getNumPointLights(); light++) {
			modelMatrix.pushMatrix();
			
			modelMatrix.getTop().translate(lightEnv.getPointLightWorldPos(light));
			
			unlit.program.begin();
			glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4(unlit.objectColorUniform, lightEnv.getPointLightScaledIntensity(light).toBuffer());
			sphere.render("flat");
			unlit.program.end();
			
			modelMatrix.popMatrix();
		}
		
		if(drawCameraPos) {
			modelMatrix.pushMatrix();
			
			modelMatrix.getTop().clearToIdentity();
			modelMatrix.getTop().translate(0, 0, -viewPole.getView().radius);
			
			unlit.program.begin();
			
			glUniformMatrix4(unlit.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
			
			glDepthMask(false);
			glDisable(GL_DEPTH_TEST);
			glUniform4f(unlit.objectColorUniform, 0.25f, 0.25f, 0.25f, 1);
			sphere.render("flat");
			
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
			glUniform4f(unlit.objectColorUniform, 1, 1, 1, 1);
			sphere.render("flat");
			
			unlit.program.end();
			
			modelMatrix.popMatrix();
		}
	}
	
	private static class ProgramData {
		private ShaderProgram program;
		
		private int modelToCameraMatrixUniform;
		private int numberOfLightsUniform;
		
		public ProgramData(ShaderProgram program) {
			this.program = program;
		}
	}
	
	private static class UnlitProgramData {
		private ShaderProgram program;
		
		private int modelToCameraMatrixUniform;
		private int objectColorUniform;
		
		public UnlitProgramData(ShaderProgram program) {
			this.program = program;
		}
	}
}
