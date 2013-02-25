package com.ra4king.opengl.arcsynthesis.gl33.chapter12;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix3;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Vector4;

import org.lwjgl.BufferUtils;

public class Scene {
	public final Mesh terrainMesh;
	public final Mesh cubeMesh;
	public final Mesh tetraMesh;
	public final Mesh cylMesh;
	public final Mesh sphereMesh;

	private final int sizeMaterialBlock;
	private int materialUniformBuffer;

	private ProgramStore store;

	private final int MATERIAL_COUNT = 6;

	public Scene(ProgramStore store) {
		this.store = store;

		try {
			terrainMesh = new Mesh(Scene.class.getResource("example12.Ground.xml"));
			cubeMesh = new Mesh(Scene.class.getResource("example12.UnitCube.xml"));
			tetraMesh = new Mesh(Scene.class.getResource("example12.UnitTetrahedron.xml"));
			cylMesh = new Mesh(Scene.class.getResource("example12.UnitCylinder.xml"));
			sphereMesh = new Mesh(Scene.class.getResource("example12.UnitSphere.xml"));
		} catch(Exception exc) {
			throw new RuntimeException("Error loading meshes", exc);
		}

		int uniformBufferAlignSize = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);

		sizeMaterialBlock = MaterialBlock.SIZE + uniformBufferAlignSize - (MaterialBlock.SIZE % uniformBufferAlignSize);

		int sizeMaterialUniformBuffer = sizeMaterialBlock * MATERIAL_COUNT;
		ArrayList<MaterialBlock> materials = getMaterials();

		FloatBuffer buffer = BufferUtils.createByteBuffer(sizeMaterialUniformBuffer).asFloatBuffer();
		for(MaterialBlock block : materials) {
			buffer.put(block.diffuseColor.toBuffer());
			buffer.put(block.specularColor.toBuffer());
			buffer.put(block.specularShininess);
			buffer.put(0).put(0).put(0);
		}
		buffer.flip();

		materialUniformBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, materialUniformBuffer);
		glBufferData(GL_UNIFORM_BUFFER, buffer, GL_STATIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	private ArrayList<MaterialBlock> getMaterials() {
		ArrayList<MaterialBlock> materials = new ArrayList<>(MATERIAL_COUNT);
		materials.add(new MaterialBlock(new Vector4(1), new Vector4(0.5f, 0.5f, 0.5f, 1), 0.6f));
		materials.add(new MaterialBlock(new Vector4(0.5f), new Vector4(0.5f, 0.5f, 0.5f, 1), 0.05f));
		materials.add(new MaterialBlock(new Vector4(0.05f), new Vector4(0.95f, 0.95f, 0.95f, 1), 0.4f));
		materials.add(new MaterialBlock(new Vector4(0.5f), new Vector4(0.3f, 0.3f, 0.3f, 1), 0.1f));
		materials.add(new MaterialBlock(new Vector4(0.5f), new Vector4(0, 0, 0, 1), 0.6f));
		materials.add(new MaterialBlock(new Vector4(0.63f, 0.6f, 0.02f, 1), new Vector4(0.22f, 0.2f, 0, 1), 0.3f));
		return materials;
	}

	public void draw(MatrixStack modelMatrix, int materialBlockIndex, float alphaTetra) {
		{
			modelMatrix.pushMatrix();
			modelMatrix.getTop().rotateDeg(-90, 1, 0, 0);

			drawObject(terrainMesh, store.getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE), materialBlockIndex, 0, modelMatrix);

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();
			modelMatrix.getTop().translate(75, 5, 75);
			modelMatrix.getTop().rotateDeg(360 * alphaTetra, 0, 1, 0);
			modelMatrix.getTop().scale(10, 10, 10);
			modelMatrix.getTop().translate(0, (float)Math.sqrt(2), 0);
			modelMatrix.getTop().rotateDeg(54.735f, -0.707f, 0, -0.707f);

			drawObject(tetraMesh, "lit-color", store.getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 1, modelMatrix);

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();
			modelMatrix.getTop().translate(88, 5, -80);
			modelMatrix.getTop().scale(4, 4, 4);
			modelMatrix.getTop().scale(4, 9, 1);
			modelMatrix.getTop().translate(0, 0.5f, 0);

			drawObject(cubeMesh, "lit", store.getProgram(LightingProgramTypes.MTL_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 2, modelMatrix);

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();
			modelMatrix.getTop().translate(-52.5f, 14, 65);
			modelMatrix.getTop().rotateDeg(50, 0, 0, 1);
			modelMatrix.getTop().rotateDeg(-10, 0, 1, 0);
			modelMatrix.getTop().scale(20, 20, 20);

			drawObject(cubeMesh, "lit-color", store.getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 3, modelMatrix);

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();
			modelMatrix.getTop().translate(-7, 30, -14);
			modelMatrix.getTop().scale(15, 55, 15);
			modelMatrix.getTop().translate(0, 0.5f, 0);

			drawObject(cylMesh, "lit-color", store.getProgram(LightingProgramTypes.VERT_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 4, modelMatrix);

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();
			modelMatrix.getTop().translate(-83, 14, -77);
			modelMatrix.getTop().scale(20, 20, 20);

			drawObject(sphereMesh, "lit", store.getProgram(LightingProgramTypes.MTL_COLOR_DIFFUSE_SPECULAR), materialBlockIndex, 5, modelMatrix);

			modelMatrix.popMatrix();
		}
	}

	public void drawObject(Mesh mesh, ProgramData prog, int materialBlockIndex, int materialIndex, MatrixStack modelMatrix) {
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, materialIndex * sizeMaterialBlock, MaterialBlock.SIZE);

		prog.program.begin();
		glUniformMatrix4(prog.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
		glUniformMatrix3(prog.normalModelTocameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
		mesh.render();
		prog.program.end();

		glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
	}

	public void drawObject(Mesh mesh, String meshName, ProgramData prog, int materialBlockIndex, int materialIndex, MatrixStack modelMatrix) {
		glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, materialUniformBuffer, materialIndex * sizeMaterialBlock, MaterialBlock.SIZE);

		prog.program.begin();
		glUniformMatrix4(prog.modelToCameraMatrixUniform, false, modelMatrix.getTop().toBuffer());
		glUniformMatrix3(prog.normalModelTocameraMatrixUniform, false, new Matrix3(modelMatrix.getTop()).inverse().transpose().toBuffer());
		mesh.render(meshName);
		prog.program.end();

		glBindBufferBase(GL_UNIFORM_BUFFER, materialBlockIndex, 0);
	}

	public static interface ProgramStore {
		public ProgramData getProgram(LightingProgramTypes type);
	}

	public static enum LightingProgramTypes {
		VERT_COLOR_DIFFUSE_SPECULAR,
		VERT_COLOR_DIFFUSE,

		MTL_COLOR_DIFFUSE_SPECULAR,
		MTL_COLOR_DIFFUSE
	}

	public static class ProgramData {
		public ShaderProgram program;

		public int modelToCameraMatrixUniform;
		public int normalModelTocameraMatrixUniform;

		public ProgramData(ShaderProgram program) {
			this.program = program;
		}
	}

	public static class MaterialBlock {
		public static final int SIZE = 3 * 4 * 4;

		public Vector4 diffuseColor;
		public Vector4 specularColor;
		public float specularShininess;

		public MaterialBlock(Vector4 diffuseColor, Vector4 specularColor, float specularShininess) {
			this.diffuseColor = diffuseColor;
			this.specularColor = specularColor;
			this.specularShininess = specularShininess;
		}
	}
}
