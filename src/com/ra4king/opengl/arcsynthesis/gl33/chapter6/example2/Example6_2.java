package com.ra4king.opengl.arcsynthesis.gl33.chapter6.example2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix4;

public class Example6_2 extends GLProgram {
	public static void main(String[] args) {
		new Example6_2().run(true);
	}

	private final float[] GREEN_COLOR = {0, 1, 0, 1};
	private final float[] BLUE_COLOR = {0, 0, 1, 1};
	private final float[] RED_COLOR = {1, 0, 0, 1};
	private final float[] BROWN_COLOR = {0.5f, 0.5f, 0, 1};

	private final float[] data = {
			1, 1, 1,
			-1, -1, 1,
			-1, 1, -1,
			1, -1, -1,

			-1, -1, -1,
			1, 1, -1,
			1, -1, 1,
			-1, 1, 1,

			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],

			GREEN_COLOR[0], GREEN_COLOR[1], GREEN_COLOR[2], GREEN_COLOR[3],
			BLUE_COLOR[0], BLUE_COLOR[1], BLUE_COLOR[2], BLUE_COLOR[3],
			RED_COLOR[0], RED_COLOR[1], RED_COLOR[2], RED_COLOR[3],
			BROWN_COLOR[0], BROWN_COLOR[1], BROWN_COLOR[2], BROWN_COLOR[3],
	};

	private final short[] indices = {
			0, 1, 2,
			1, 0, 3,
			2, 3, 0,
			3, 2, 1,

			5, 4, 6,
			4, 5, 7,
			7, 6, 4,
			6, 7, 5
	};

	private ShaderProgram program;

	private final float frustumScale = calculateFrustumScale(45);

	private Matrix4 cameraToClipMatrix;
	private int cameraToClipMatrixUniform;

	private Matrix4[] modelToCameraMatrices;
	private int modelToCameraMatrixUniform;

	private int vao;

	private long elapsedTime;

	public Example6_2() {
		super("Example 6.2 - Scale", 500, 500, true);
	}

	private float calculateFrustumScale(float angle) {
		return 1 / (float)Math.tan((angle * (float)Math.PI / 180) / 2f);
	}

	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);

		program = new ShaderProgram(readFromFile("example6.2.vert"), readFromFile("example6.2.frag"));

		modelToCameraMatrixUniform = glGetUniformLocation(program.getProgram(), "modelToCameraMatrix");
		cameraToClipMatrixUniform = glGetUniformLocation(program.getProgram(), "cameraToClipMatrix");

		modelToCameraMatrices = new Matrix4[5];
		for(int a = 0; a < modelToCameraMatrices.length; a++)
			modelToCameraMatrices[a] = new Matrix4();

		float zNear = 1, zFar = 61;
		cameraToClipMatrix = new Matrix4();
		cameraToClipMatrix.put(0, frustumScale);
		cameraToClipMatrix.put(5, frustumScale);
		cameraToClipMatrix.put(10, (zFar + zNear) / (zNear - zFar));
		cameraToClipMatrix.put(14, (2 * zFar * zNear) / (zNear - zFar));
		cameraToClipMatrix.put(11, -1);

		program.begin();
		glUniformMatrix4(cameraToClipMatrixUniform, false, cameraToClipMatrix.toBuffer());
		program.end();

		int vbo1 = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, vbo1);
		glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)BufferUtils.createFloatBuffer(data.length).put(data).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		int vbo2 = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, vbo2);
		glBufferData(GL_ARRAY_BUFFER, (ShortBuffer)BufferUtils.createShortBuffer(indices.length).put(indices).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		glBindBuffer(GL_ARRAY_BUFFER, vbo1);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 8 * 3 * 4);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo2);

		glBindVertexArray(0);

		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0, 1);
	}

	@Override
	public void resized() {
		super.resized();

		cameraToClipMatrix.put(0, frustumScale / ((float)getWidth() / getHeight()));

		program.begin();
		glUniformMatrix4(cameraToClipMatrixUniform, false, cameraToClipMatrix.toBuffer());
		program.end();
	}

	private void setupNullScale(Matrix4 m) {
		m.clearToIdentity().put(14, -45);
	}

	private void setupStaticUniformScale(Matrix4 m) {
		m.clearToIdentity()
				.put(0, 4)
				.put(5, 4)
				.put(10, 4)
				.put(12, -10)
				.put(13, -10)
				.put(14, -45);
	}

	private void setupStaticNonUniformScale(Matrix4 m) {
		m.clearToIdentity()
				.put(0, 0.5f)
				.put(5, 1)
				.put(10, 10)
				.put(12, -10)
				.put(13, 10)
				.put(14, -45);
	}

	private float calculateLerpFactor(float loopDuration) {
		float value = ((elapsedTime / (float)1e9) % loopDuration) / loopDuration;
		if(value > 0.5f)
			value = 1 - value;
		return value * 2;
	}

	private void setupDynamicUniformScale(Matrix4 m) {
		float s = Utils.mix(1, 4, calculateLerpFactor(3));

		m.clearToIdentity()
				.put(0, s)
				.put(5, s)
				.put(10, s)
				.put(12, 10)
				.put(13, 10)
				.put(14, -45);
	}

	private void setupDynamicNonUniformScale(Matrix4 m) {
		m.clearToIdentity()
				.put(0, Utils.mix(1, 0.5f, calculateLerpFactor(3)))
				.put(5, 1)
				.put(10, Utils.mix(1, 10, calculateLerpFactor(5)))
				.put(12, 10)
				.put(13, -10)
				.put(14, -45);
	}

	@Override
	public void update(long deltaTime) {
		elapsedTime += deltaTime;

		setupNullScale(modelToCameraMatrices[0]);
		setupStaticUniformScale(modelToCameraMatrices[1]);
		setupStaticNonUniformScale(modelToCameraMatrices[2]);
		setupDynamicUniformScale(modelToCameraMatrices[3]);
		setupDynamicNonUniformScale(modelToCameraMatrices[4]);
	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		program.begin();
		glBindVertexArray(vao);

		for(Matrix4 m : modelToCameraMatrices) {
			glUniformMatrix4(modelToCameraMatrixUniform, false, m.toBuffer());
			glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_SHORT, 0);
		}

		glBindVertexArray(0);
		program.end();
	}
}
