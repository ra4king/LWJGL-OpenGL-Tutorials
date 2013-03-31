package com.ra4king.opengl.arcsynthesis.gl33.chapter16.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.PNGDecoder;
import com.ra4king.opengl.util.PNGDecoder.Format;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix4;

/**
 * @author ra4king
 */
public class Example16_1 extends GLProgram {
	public static void main(String[] args) {
		new Example16_1().run(true);
	}
	
	private ShaderProgram gammaProgram;
	private ShaderProgram noGammaProgram;
	
	private int dataBufferObject;
	private int vao;
	
	private int projectionUniformBuffer;
	
	private int[] textures = new int[2];
	private int samplerObject;
	
	private boolean[] useGammaCorrect = new boolean[2];
	
	private final int projectionBlockIndex = 0;
	private final int gammaRampTextureUnit = 0;
	
	public Example16_1() {
		super("Example 16.1 - Gamma Ramp", 500, 192, false);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0.5f, 0.3f, 0);
		glClearDepth(1);
		
		initializePrograms();
		initializeVertexData();
		loadTextures();
		
		projectionUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, 16 * 4, GL_DYNAMIC_DRAW);
		
		glBindBufferRange(GL_UNIFORM_BUFFER, projectionBlockIndex, projectionUniformBuffer, 0, 16 * 4);
		
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	private void initializePrograms() {
		String vertexShader = readFromFile("example16.1.ScreenCoords.vert");
		noGammaProgram = new ShaderProgram(vertexShader, readFromFile("example16.1.TextureNoGamma.frag"));
		
		int projectionBlock = glGetUniformBlockIndex(noGammaProgram.getProgram(), "Projection");
		glUniformBlockBinding(noGammaProgram.getProgram(), projectionBlock, projectionBlockIndex);
		
		int colorTextureUniform = glGetUniformLocation(noGammaProgram.getProgram(), "colorTexture");
		noGammaProgram.begin();
		glUniform1i(colorTextureUniform, gammaRampTextureUnit);
		noGammaProgram.end();
		
		gammaProgram = new ShaderProgram(vertexShader, readFromFile("example16.1.TextureGamma.frag"));
		projectionBlock = glGetUniformBlockIndex(gammaProgram.getProgram(), "Projection");
		glUniformBlockBinding(gammaProgram.getProgram(), projectionBlock, projectionBlockIndex);
		
		colorTextureUniform = glGetUniformLocation(gammaProgram.getProgram(), "colorTexture");
		gammaProgram.begin();
		glUniform1i(colorTextureUniform, gammaRampTextureUnit);
		gammaProgram.end();
	}
	
	private void initializeVertexData() {
		final short[] vertexData = {
				90, 80, 0, 0,
				90, 16, 0, (short)65535,
				410, 80, (short)65535, 0,
				410, 16, (short)65535, (short)65535,
				
				90, 176, 0, 0,
				90, 112, 0, (short)65535,
				410, 176, (short)65535, 0,
				410, 112, (short)65535, (short)65535,
		};
		
		ShortBuffer buffer = (ShortBuffer)BufferUtils.createShortBuffer(vertexData.length).put(vertexData).flip();
		
		dataBufferObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, dataBufferObject);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		
		glBindBuffer(GL_ARRAY_BUFFER, dataBufferObject);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_UNSIGNED_SHORT, false, 8, 0);
		glEnableVertexAttribArray(5);
		glVertexAttribPointer(5, 2, GL_UNSIGNED_SHORT, true, 8, 4);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glBindVertexArray(0);
	}
	
	private void loadTextures() {
		for(int a = 0; a < textures.length; a++)
			textures[a] = glGenTextures();
		
		try {
			PNGDecoder decoder = new PNGDecoder(getClass().getResourceAsStream("example16.1.gamma_ramp.png"));
			ByteBuffer buffer = BufferUtils.createByteBuffer(decoder.getWidth() * decoder.getHeight() * 4);
			decoder.decode(buffer, decoder.getWidth() * 4, Format.RGBA);
			buffer.flip();
			
			glBindTexture(GL_TEXTURE_2D, textures[0]);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
			
			glBindTexture(GL_TEXTURE_2D, textures[1]);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
			
			glBindTexture(GL_TEXTURE_2D, 0);
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		samplerObject = glGenSamplers();
		glSamplerParameteri(samplerObject, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(samplerObject, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glSamplerParameteri(samplerObject, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glSamplerParameteri(samplerObject, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	}
	
	@Override
	public void resized() {
		super.resized();
		
		Matrix4 perspectiveMatrix = new Matrix4().clearToIdentity();
		perspectiveMatrix.translate(-1, 1, 0);
		perspectiveMatrix.scale(2.0f / getWidth(), -2.0f / getHeight(), 1);
		
		glBindBuffer(GL_UNIFORM_BUFFER, projectionUniformBuffer);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, perspectiveMatrix.toBuffer());
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_1:
				useGammaCorrect[0] = !useGammaCorrect[0];
				if(useGammaCorrect[0])
					System.out.println("Top:\tsRGB texture.");
				else
					System.out.println("Top:\tlinear texture.");
				break;
			case Keyboard.KEY_2:
				useGammaCorrect[1] = !useGammaCorrect[1];
				if(useGammaCorrect[1])
					System.out.println("Bottom:\tsRGB texture.");
				else
					System.out.println("Bottom:\tlinear texture.");
				break;
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		glActiveTexture(GL_TEXTURE0 + gammaRampTextureUnit);
		glBindTexture(GL_TEXTURE_2D, textures[useGammaCorrect[0] ? 1 : 0]);
		glBindSampler(gammaRampTextureUnit, samplerObject);
		
		glBindVertexArray(vao);
		
		noGammaProgram.begin();
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		noGammaProgram.end();
		
		glBindTexture(GL_TEXTURE_2D, textures[useGammaCorrect[1] ? 1 : 0]);
		
		gammaProgram.begin();
		glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);
		gammaProgram.end();
		
		glBindVertexArray(0);
		
		glActiveTexture(GL_TEXTURE0 + gammaRampTextureUnit);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindSampler(gammaRampTextureUnit, 0);
	}
}
