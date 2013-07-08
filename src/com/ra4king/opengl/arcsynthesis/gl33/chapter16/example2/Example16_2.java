package com.ra4king.opengl.arcsynthesis.gl33.chapter16.example2;

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

import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.ImageSet.Dimensions;
import rosick.jglsdk.glimg.ImageSet.SingleImage;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Vector3;

/**
 * @author ra4king
 */
public class Example16_2 extends GLProgram {
	public static void main(String[] args) {
		new Example16_2().run(true);
	}
	
	private ProgramData noGammaProgram;
	private ProgramData gammaProgram;
	
	private Mesh plane;
	private Mesh corridor;
	
	private Timer camTimer = new Timer(Type.LOOP, 5);
	
	private int projectionUniformBuffer;
	private int linearTexture;
	private int gammaTexture;
	
	private final int NUM_SAMPLERS = 2;
	private int[] samplers = new int[NUM_SAMPLERS];
	
	private int currSampler = 0;
	
	private boolean drawCorridor, drawGammaTexture, drawGammaProgram;
	
	private final int projectionBlockIndex = 0;
	private final int colorTexUnit = 0;
	
	public Example16_2() {
		super("Example 16.2 - Gamma Checkers", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0.75f, 0.75f, 1, 1);
		glClearDepth(1);
		
		noGammaProgram = loadProgram("example16.2.PT.vert", "example16.2.TextureNoGamma.frag");
		gammaProgram = loadProgram("example16.2.PT.vert", "example16.2.TextureGamma.frag");
		
		try {
			corridor = new Mesh(getClass().getResource("example16.2.Corridor.xml"));
			plane = new Mesh(getClass().getResource("example16.2.BigPlane.xml"));
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
		
		loadCheckerTextures();
		createSamplers();
	}
	
	private ProgramData loadProgram(String vertexShader, String fragmentShader) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = data.program.getUniformLocation("modelToCameraMatrix");
		
		int projectionBlock = data.program.getUniformBlockIndex("Projection");
		
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		int colorTextureUniform = data.program.getUniformLocation("colorTexture");
		data.program.begin();
		glUniform1i(colorTextureUniform, colorTexUnit);
		data.program.end();
		
		return data;
	}
	
	private void loadCheckerTextures() {
		try {
			ImageSet imageSet = DdsLoader.load(getClass().getResourceAsStream("example16.2.checker_linear.dds"));
			
			linearTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, linearTexture);
			
			for(int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
				Dimensions dims = image.getDimensions();
				
				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8, dims.width, dims.height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, image.getImageData());
			}
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);
			
			glBindTexture(GL_TEXTURE_2D, 0);
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		try {
			ImageSet imageSet = DdsLoader.load(getClass().getResourceAsStream("example16.2.checker_gamma.dds"));
			
			gammaTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, gammaTexture);
			
			for(int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
				Dimensions dims = image.getDimensions();
				
				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_SRGB8, dims.width, dims.height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, image.getImageData());
			}
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);
			
			glBindTexture(GL_TEXTURE_2D, 0);
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
	}
	
	private void createSamplers() {
		for(int a = 0; a < NUM_SAMPLERS; a++) {
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
		glBufferSubData(GL_UNIFORM_BUFFER, 0, new Matrix4().clearToPerspectiveDeg(90, getWidth(), getHeight(), 1, 1000).toBuffer());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	@Override
	public void update(long deltaTime) {
		camTimer.update(deltaTime);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_SPACE:
				drawGammaProgram = !drawGammaProgram;
				drawGammaTexture = !drawGammaTexture;
				break;
			case Keyboard.KEY_A:
				drawGammaProgram = !drawGammaProgram;
				break;
			case Keyboard.KEY_G:
				drawGammaTexture = !drawGammaTexture;
				break;
			case Keyboard.KEY_Y:
				drawCorridor = !drawCorridor;
				break;
			case Keyboard.KEY_P:
				camTimer.togglePause();
				break;
		}
		
		System.out.println("----");
		System.out.printf("Rendering:\t\t%s\n", drawGammaProgram ? "Gamma" : "Linear");
		System.out.printf("Mipmap Generation:\t%s\n", drawGammaTexture ? "Gamma" : "Linear");
		
		if(c >= '1' && c <= '9') {
			int number = c - '1';
			if(number < NUM_SAMPLERS)
				currSampler = number;
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		float cyclicAngle = camTimer.getAlpha() * 2 * (float)Math.PI;
		float hOffset = (float)(Math.cos(cyclicAngle) * 0.25f);
		float vOffset = (float)(Math.sin(cyclicAngle) * 0.25f);
		
		MatrixStack modelMatrix = new MatrixStack();
		modelMatrix.setTop(Utils.lookAt(new Vector3(hOffset, 1, -64), new Vector3(hOffset, -5 + vOffset, -44), new Vector3(0, 1, 0)));
		
		ProgramData program = drawGammaProgram ? gammaProgram : noGammaProgram;
		
		program.program.begin();
		glUniformMatrix4(program.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
		
		glActiveTexture(GL_TEXTURE0 + colorTexUnit);
		glBindTexture(GL_TEXTURE_2D, drawGammaTexture ? gammaTexture : linearTexture);
		glBindSampler(colorTexUnit, samplers[currSampler]);
		
		if(drawCorridor)
			corridor.render("tex");
		else
			plane.render("tex");
		
		glBindSampler(colorTexUnit, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		program.program.end();
	}
	
	private static class ProgramData {
		private ShaderProgram program;
		
		private int modelToCameraMatrixUniform;
		
		public ProgramData(ShaderProgram program) {
			this.program = program;
		}
	}
}
