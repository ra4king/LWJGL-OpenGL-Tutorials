package com.ra4king.opengl.arcsynthesis.gl33.chapter15;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.img.dds.DdsLoader;
import com.ra4king.opengl.util.img.dds.ImageSet;
import com.ra4king.opengl.util.img.dds.ImageSet.Dimensions;
import com.ra4king.opengl.util.img.dds.ImageSet.SingleImage;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Vector3;

/**
 * @author ra4king
 */
public class Example15_1 extends GLProgram {
	public static void main(String[] args) {
		new Example15_1().run(true);
	}
	
	private ProgramData program;
	
	private Mesh plane;
	private Mesh corridor;
	
	private Timer camTimer = new Timer(Type.LOOP, 5);
	
	private int projectionUniformBuffer;
	private int checkerTexture;
	private int mipmapTestTexture;
	
	private final int NUM_SAMPLERS = 6;
	private int[] samplers = new int[NUM_SAMPLERS];
	
	private int currSampler = 0;
	
	private boolean useMipmapTexture, drawCorridor;
	
	private final int projectionBlockIndex = 0;
	private final int colorTexUnit = 0;
	
	public Example15_1() {
		super("Example 15.1 - Many Images", 500, 500, true);
	}
	
	@Override
	public void init() {
		setFPS(0);
		
		glClearColor(0.75f, 0.75f, 1, 1);
		glClearDepth(1);
		
		program = loadProgram("example15.1.PT.vert", "example15.1.Tex.frag");
		
		try {
			corridor = new Mesh(getClass().getResource("example15.1.Corridor.xml"));
			plane = new Mesh(getClass().getResource("example15.1.BigPlane.xml"));
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
		
		loadCheckerTexture();
		loadMipmapTexture();
		createSamplers();
	}
	
	private ProgramData loadProgram(String vertexShader, String fragmentShader) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertexShader), readFromFile(fragmentShader)));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		int colorTextureUniform = glGetUniformLocation(data.program.getProgram(), "colorTexture");
		data.program.begin();
		glUniform1i(colorTextureUniform, colorTexUnit);
		data.program.end();
		
		return data;
	}
	
	private void loadCheckerTexture() {
		try {
			ImageSet imageSet = DdsLoader.load(getClass().getResourceAsStream("example15.1.checker.dds"));
			
			checkerTexture = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, checkerTexture);
			
			for(int mipmapLevel = 0; mipmapLevel < imageSet.getMipmapCount(); mipmapLevel++) {
				SingleImage image = imageSet.getImage(mipmapLevel, 0, 0);
				Dimensions dims = image.getDimensions();
				
				glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_RGB8, dims.width, dims.height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, image.getImageData());
			}
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, imageSet.getMipmapCount() - 1);
			glBindTexture(GL_TEXTURE_2D, 0);
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
	}
	
	private void loadMipmapTexture() {
		final byte[] mipmapColors = {
				(byte)0xFF, (byte)0xFF, (byte)0x00,
				(byte)0xFF, (byte)0x00, (byte)0xFF,
				(byte)0x00, (byte)0xFF, (byte)0xFF,
				(byte)0xFF, (byte)0x00, (byte)0x00,
				(byte)0x00, (byte)0xFF, (byte)0x00,
				(byte)0x00, (byte)0x00, (byte)0xFF,
				(byte)0x00, (byte)0x00, (byte)0x00,
				(byte)0xFF, (byte)0xFF, (byte)0xFF
		};
		
		mipmapTestTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, mipmapTestTexture);
		
		int oldAlign = glGetInteger(GL_UNPACK_ALIGNMENT);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		
		for(int mipmapLevel = 0; mipmapLevel < 8; mipmapLevel++) {
			int width = 128 >> mipmapLevel;
			int height = 128 >> mipmapLevel;
			
			ByteBuffer buffer = fillWithColor(mipmapColors[mipmapLevel * 3 + 0], mipmapColors[mipmapLevel * 3 + 1], mipmapColors[mipmapLevel * 3 + 2], width, height);
			glTexImage2D(GL_TEXTURE_2D, mipmapLevel, GL_RGB8, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);
		}
		
		glPixelStorei(GL_UNPACK_ALIGNMENT, oldAlign);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 7);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	private ByteBuffer fillWithColor(byte red, byte green, byte blue, int width, int height) {
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 3);
		
		while(buffer.hasRemaining())
			buffer.put(red).put(green).put(blue);
		
		buffer.flip();
		
		return buffer;
	}
	
	private void createSamplers() {
		for(int a = 0; a < NUM_SAMPLERS; a++) {
			samplers[a] = glGenSamplers();
			
			glSamplerParameteri(samplers[a], GL_TEXTURE_WRAP_S, GL_REPEAT);
			glSamplerParameteri(samplers[a], GL_TEXTURE_WRAP_T, GL_REPEAT);
		}
		
		glSamplerParameteri(samplers[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(samplers[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		
		glSamplerParameteri(samplers[1], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[1], GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		
		glSamplerParameteri(samplers[2], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[2], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		
		glSamplerParameteri(samplers[3], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[3], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		
		glSamplerParameteri(samplers[4], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[4], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(samplers[4], GL_TEXTURE_MAX_ANISOTROPY_EXT, 4);
		
		float maxAniso = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
		
		System.out.println("Maximum anisotropy: " + maxAniso);
		
		glSamplerParameteri(samplers[5], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glSamplerParameteri(samplers[5], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glSamplerParameterf(samplers[5], GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAniso);
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
		final String[] samplerNames = {
				"Nearest",
				"Linear",
				"Linear with nearest mipmaps",
				"Linear with linear mipmaps",
				"Low anisotropic",
				"Max anisotropic"
		};
		
		switch(key) {
			case Keyboard.KEY_SPACE:
				useMipmapTexture = !useMipmapTexture;
				break;
			case Keyboard.KEY_Y:
				drawCorridor = !drawCorridor;
				break;
			case Keyboard.KEY_P:
				camTimer.togglePause();
				break;
		}
		
		if(c >= '1' && c <= '9') {
			int number = c - '1';
			if(number < NUM_SAMPLERS) {
				System.out.println("Sampler: " + samplerNames[number]);
				currSampler = number;
			}
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		float cyclicAngle = camTimer.getAlpha() * 2 * (float)Math.PI;
		float hOffset = (float)(Math.cos(cyclicAngle) * 0.25);
		float vOffset = (float)(Math.sin(cyclicAngle) * 0.25);
		
		MatrixStack modelMatrix = new MatrixStack();
		
		Matrix4 worldToCameraMatrix = Utils.lookAt(new Vector3(hOffset, 1, -64), new Vector3(hOffset, -5 + vOffset, -44), new Vector3(0, 1, 0));
		
		modelMatrix.getTop().mult(worldToCameraMatrix);
		
		program.program.begin();
		glUniformMatrix4(program.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
		
		glActiveTexture(GL_TEXTURE0 + colorTexUnit);
		glBindTexture(GL_TEXTURE_2D, useMipmapTexture ? mipmapTestTexture : checkerTexture);
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
