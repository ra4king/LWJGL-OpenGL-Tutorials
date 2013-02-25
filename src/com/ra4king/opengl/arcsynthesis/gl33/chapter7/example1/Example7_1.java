package com.ra4king.opengl.arcsynthesis.gl33.chapter7.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Vector3;

public class Example7_1 extends GLProgram {
	public static void main(String[] args) {
		new Example7_1().run(true);
	}

	private final TreeData[] forest = {
			new TreeData(-45.0f, -40.0f, 2.0f, 3.0f),
			new TreeData(-42.0f, -35.0f, 2.0f, 3.0f),
			new TreeData(-39.0f, -29.0f, 2.0f, 4.0f),
			new TreeData(-44.0f, -26.0f, 3.0f, 3.0f),
			new TreeData(-40.0f, -22.0f, 2.0f, 4.0f),
			new TreeData(-36.0f, -15.0f, 3.0f, 3.0f),
			new TreeData(-41.0f, -11.0f, 2.0f, 3.0f),
			new TreeData(-37.0f, -6.0f, 3.0f, 3.0f),
			new TreeData(-45.0f, 0.0f, 2.0f, 3.0f),
			new TreeData(-39.0f, 4.0f, 3.0f, 4.0f),
			new TreeData(-36.0f, 8.0f, 2.0f, 3.0f),
			new TreeData(-44.0f, 13.0f, 3.0f, 3.0f),
			new TreeData(-42.0f, 17.0f, 2.0f, 3.0f),
			new TreeData(-38.0f, 23.0f, 3.0f, 4.0f),
			new TreeData(-41.0f, 27.0f, 2.0f, 3.0f),
			new TreeData(-39.0f, 32.0f, 3.0f, 3.0f),
			new TreeData(-44.0f, 37.0f, 3.0f, 4.0f),
			new TreeData(-36.0f, 42.0f, 2.0f, 3.0f),

			new TreeData(-32.0f, -45.0f, 2.0f, 3.0f),
			new TreeData(-30.0f, -42.0f, 2.0f, 4.0f),
			new TreeData(-34.0f, -38.0f, 3.0f, 5.0f),
			new TreeData(-33.0f, -35.0f, 3.0f, 4.0f),
			new TreeData(-29.0f, -28.0f, 2.0f, 3.0f),
			new TreeData(-26.0f, -25.0f, 3.0f, 5.0f),
			new TreeData(-35.0f, -21.0f, 3.0f, 4.0f),
			new TreeData(-31.0f, -17.0f, 3.0f, 3.0f),
			new TreeData(-28.0f, -12.0f, 2.0f, 4.0f),
			new TreeData(-29.0f, -7.0f, 3.0f, 3.0f),
			new TreeData(-26.0f, -1.0f, 2.0f, 4.0f),
			new TreeData(-32.0f, 6.0f, 2.0f, 3.0f),
			new TreeData(-30.0f, 10.0f, 3.0f, 5.0f),
			new TreeData(-33.0f, 14.0f, 2.0f, 4.0f),
			new TreeData(-35.0f, 19.0f, 3.0f, 4.0f),
			new TreeData(-28.0f, 22.0f, 2.0f, 3.0f),
			new TreeData(-33.0f, 26.0f, 3.0f, 3.0f),
			new TreeData(-29.0f, 31.0f, 3.0f, 4.0f),
			new TreeData(-32.0f, 38.0f, 2.0f, 3.0f),
			new TreeData(-27.0f, 41.0f, 3.0f, 4.0f),
			new TreeData(-31.0f, 45.0f, 2.0f, 4.0f),
			new TreeData(-28.0f, 48.0f, 3.0f, 5.0f),

			new TreeData(-25.0f, -48.0f, 2.0f, 3.0f),
			new TreeData(-20.0f, -42.0f, 3.0f, 4.0f),
			new TreeData(-22.0f, -39.0f, 2.0f, 3.0f),
			new TreeData(-19.0f, -34.0f, 2.0f, 3.0f),
			new TreeData(-23.0f, -30.0f, 3.0f, 4.0f),
			new TreeData(-24.0f, -24.0f, 2.0f, 3.0f),
			new TreeData(-16.0f, -21.0f, 2.0f, 3.0f),
			new TreeData(-17.0f, -17.0f, 3.0f, 3.0f),
			new TreeData(-25.0f, -13.0f, 2.0f, 4.0f),
			new TreeData(-23.0f, -8.0f, 2.0f, 3.0f),
			new TreeData(-17.0f, -2.0f, 3.0f, 3.0f),
			new TreeData(-16.0f, 1.0f, 2.0f, 3.0f),
			new TreeData(-19.0f, 4.0f, 3.0f, 3.0f),
			new TreeData(-22.0f, 8.0f, 2.0f, 4.0f),
			new TreeData(-21.0f, 14.0f, 2.0f, 3.0f),
			new TreeData(-16.0f, 19.0f, 2.0f, 3.0f),
			new TreeData(-23.0f, 24.0f, 3.0f, 3.0f),
			new TreeData(-18.0f, 28.0f, 2.0f, 4.0f),
			new TreeData(-24.0f, 31.0f, 2.0f, 3.0f),
			new TreeData(-20.0f, 36.0f, 2.0f, 3.0f),
			new TreeData(-22.0f, 41.0f, 3.0f, 3.0f),
			new TreeData(-21.0f, 45.0f, 2.0f, 3.0f),

			new TreeData(-12.0f, -40.0f, 2.0f, 4.0f),
			new TreeData(-11.0f, -35.0f, 3.0f, 3.0f),
			new TreeData(-10.0f, -29.0f, 1.0f, 3.0f),
			new TreeData(-9.0f, -26.0f, 2.0f, 2.0f),
			new TreeData(-6.0f, -22.0f, 2.0f, 3.0f),
			new TreeData(-15.0f, -15.0f, 1.0f, 3.0f),
			new TreeData(-8.0f, -11.0f, 2.0f, 3.0f),
			new TreeData(-14.0f, -6.0f, 2.0f, 4.0f),
			new TreeData(-12.0f, 0.0f, 2.0f, 3.0f),
			new TreeData(-7.0f, 4.0f, 2.0f, 2.0f),
			new TreeData(-13.0f, 8.0f, 2.0f, 2.0f),
			new TreeData(-9.0f, 13.0f, 1.0f, 3.0f),
			new TreeData(-13.0f, 17.0f, 3.0f, 4.0f),
			new TreeData(-6.0f, 23.0f, 2.0f, 3.0f),
			new TreeData(-12.0f, 27.0f, 1.0f, 2.0f),
			new TreeData(-8.0f, 32.0f, 2.0f, 3.0f),
			new TreeData(-10.0f, 37.0f, 3.0f, 3.0f),
			new TreeData(-11.0f, 42.0f, 2.0f, 2.0f),

			new TreeData(15.0f, 5.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 10.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 15.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 20.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 25.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 30.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 35.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 40.0f, 2.0f, 3.0f),
			new TreeData(15.0f, 45.0f, 2.0f, 3.0f),

			new TreeData(25.0f, 5.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 10.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 15.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 20.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 25.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 30.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 35.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 40.0f, 2.0f, 3.0f),
			new TreeData(25.0f, 45.0f, 2.0f, 3.0f),
	};

	private ProgramData uniformColor, objectColor, uniformColorTint;
	private Matrix4 perspectiveMatrix;

	private final Matrix4 identity = new Matrix4().clearToIdentity();

	private Mesh coneMesh, cylinderMesh, cubeTintMesh, cubeColorMesh, planeMesh;

	private Vector3 camTarget = new Vector3(0, 0.4f, 0);
	private Vector3 sphereCamRelPos = new Vector3(67.5f, -46, 150);

	private boolean drawLookAtPoint = true;

	public Example7_1() {
		super("Example 7.1 - World Scene", 500, 500, true);
	}

	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);

		uniformColor = loadProgram("example7.1.PosOnly.vert", "example7.1.ColorUniform.frag");
		objectColor = loadProgram("example7.1.PosColor.vert", "example7.1.ColorPassthrough.frag");
		uniformColorTint = loadProgram("example7.1.PosColor.vert", "example7.1.ColorMultUniform.frag");

		perspectiveMatrix = new Matrix4();

		try {
			coneMesh = new Mesh(getClass().getResource("example7.1.UnitConeTint.xml"));
			cylinderMesh = new Mesh(getClass().getResource("example7.1.UnitCylinderTint.xml"));
			cubeTintMesh = new Mesh(getClass().getResource("example7.1.UnitCubeTint.xml"));
			cubeColorMesh = new Mesh(getClass().getResource("example7.1.UnitCubeColor.xml"));
			planeMesh = new Mesh(getClass().getResource("example7.1.UnitPlane.xml"));
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
	}

	private ProgramData loadProgram(String vs, String fs) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vs), readFromFile(fs)));
		data.modelToWorldMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToWorldMatrix");
		data.worldToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "worldToCameraMatrix");
		data.cameraToClipMatrixUniform = glGetUniformLocation(data.program.getProgram(), "cameraToClipMatrix");
		data.baseColorUniform = glGetUniformLocation(data.program.getProgram(), "baseColor");
		return data;
	}

	@Override
	public void resized() {
		super.resized();

		perspectiveMatrix.clearToPerspective(45 * (float)Math.PI / 180, getWidth(), getHeight(), 1, 1000);

		uniformColor.program.begin();
		glUniformMatrix4(uniformColor.cameraToClipMatrixUniform, false, perspectiveMatrix.toBuffer());
		objectColor.program.begin();
		glUniformMatrix4(objectColor.cameraToClipMatrixUniform, false, perspectiveMatrix.toBuffer());
		uniformColorTint.program.begin();
		glUniformMatrix4(uniformColorTint.cameraToClipMatrixUniform, false, perspectiveMatrix.toBuffer());
		uniformColorTint.program.end();
	}

	@Override
	public void update(long deltaTime) {
		float delta = deltaTime / (float)1e9;

		float speed1 = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 4f : 16) * delta;
		float speed2 = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 11.25f : 45f) * delta;
		float speed3 = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 5f : 20) * delta;

		if(Keyboard.isKeyDown(Keyboard.KEY_W))
			camTarget.sub(0, 0, speed1);
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
			camTarget.add(0, 0, speed1);

		if(Keyboard.isKeyDown(Keyboard.KEY_D))
			camTarget.add(speed1, 0, 0);
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
			camTarget.sub(speed1, 0, 0);

		if(Keyboard.isKeyDown(Keyboard.KEY_E))
			camTarget.sub(speed1, 0, 0);
		if(Keyboard.isKeyDown(Keyboard.KEY_Q))
			camTarget.add(speed1, 0, 0);

		if(Keyboard.isKeyDown(Keyboard.KEY_I))
			sphereCamRelPos.sub(0, speed2, 0);
		if(Keyboard.isKeyDown(Keyboard.KEY_K))
			sphereCamRelPos.add(0, speed2, 0);

		if(Keyboard.isKeyDown(Keyboard.KEY_J))
			sphereCamRelPos.sub(speed2, 0, 0);
		if(Keyboard.isKeyDown(Keyboard.KEY_L))
			sphereCamRelPos.add(speed2, 0, 0);

		if(Keyboard.isKeyDown(Keyboard.KEY_O))
			sphereCamRelPos.sub(0, 0, speed3);
		if(Keyboard.isKeyDown(Keyboard.KEY_U))
			sphereCamRelPos.add(0, 0, speed3);

		sphereCamRelPos.y(Utils.clamp(sphereCamRelPos.y(), -78.75f, -1));
		camTarget.y(camTarget.y() > 0 ? camTarget.y() : 0);
		sphereCamRelPos.z(sphereCamRelPos.z() > 5 ? sphereCamRelPos.z() : 5);
	}

	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_SPACE:
				drawLookAtPoint = !drawLookAtPoint;
				System.out.printf("Target: %f, %f, %f\n", camTarget.x(), camTarget.y(), camTarget.z());
				System.out.printf("Position: %f,  %f, %f\n", sphereCamRelPos.x(), sphereCamRelPos.y(), sphereCamRelPos.z());
				break;
		}
	}

	private Matrix4 calcLookAtMatrix(Vector3 cameraPoint, Vector3 lookPoint, Vector3 upPoint) {
		Vector3 lookDir = new Vector3(lookPoint).sub(cameraPoint).normalize();
		Vector3 upDir = new Vector3(upPoint).normalize();

		Vector3 rightDir = lookDir.cross(upDir).normalize();
		Vector3 perpUpDir = rightDir.cross(lookDir);

		Matrix4 rotMat = new Matrix4().clearToIdentity();
		rotMat.putColumn(0, rightDir, 0);
		rotMat.putColumn(1, perpUpDir, 0);
		rotMat.putColumn(2, lookDir.mult(-1), 0);

		return rotMat.transpose().translate(new Vector3(cameraPoint).mult(-1));
	}

	private Vector3 resolveCamPosition() {
		double phi = sphereCamRelPos.x() * Math.PI / 180;
		double theta = (sphereCamRelPos.y() + 90) * Math.PI / 180;

		float sinTheta = (float)Math.sin(theta);
		float cosTheta = (float)Math.cos(theta);
		float cosPhi = (float)Math.cos(phi);
		float sinPhi = (float)Math.sin(phi);

		return new Vector3(sinTheta * cosPhi, cosTheta, sinTheta * sinPhi).mult(sphereCamRelPos.z()).add(camTarget);
	}

	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		Vector3 camPos = resolveCamPosition();

		MatrixStack camMatrix = new MatrixStack();
		camMatrix.setTop(calcLookAtMatrix(camPos, camTarget, new Vector3(0, 1, 0)));

		uniformColor.program.begin();
		glUniformMatrix4(uniformColor.worldToCameraMatrixUniform, false, camMatrix.getTop().toBuffer());
		objectColor.program.begin();
		glUniformMatrix4(objectColor.worldToCameraMatrixUniform, false, camMatrix.getTop().toBuffer());
		uniformColorTint.program.begin();
		glUniformMatrix4(uniformColorTint.worldToCameraMatrixUniform, false, camMatrix.getTop().toBuffer());
		uniformColorTint.program.end();

		MatrixStack modelMatrix = new MatrixStack();

		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().scale(100, 1, 100);

			uniformColor.program.begin();
			glUniformMatrix4(uniformColor.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4f(uniformColor.baseColorUniform, 0.302f, 0.416f, 0.0589f, 1.0f);
			planeMesh.render();
			uniformColor.program.end();

			modelMatrix.popMatrix();
		}

		drawForest(modelMatrix);

		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().translate(20, 0, -10);

			drawParthenon(modelMatrix);

			modelMatrix.popMatrix();
		}

		if(drawLookAtPoint) {
			glDisable(GL_DEPTH_TEST);

			modelMatrix.pushMatrix();

			modelMatrix.getTop().translate(0, 0, -new Vector3(camTarget).sub(camPos).length()).scale(1, 1, 1);

			objectColor.program.begin();
			glUniformMatrix4(objectColor.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniformMatrix4(objectColor.worldToCameraMatrixUniform, false, identity.toBuffer());
			cubeColorMesh.render();
			objectColor.program.end();

			modelMatrix.popMatrix();

			glEnable(GL_DEPTH_TEST);
		}
	}

	private void drawForest(MatrixStack modelMatrix) {
		for(TreeData tree : forest) {
			modelMatrix.pushMatrix();
			modelMatrix.getTop().translate(tree.x, 0, tree.z);
			drawTree(modelMatrix, tree.trunkHeight, tree.coneHeight);
			modelMatrix.popMatrix();
		}
	}

	private void drawTree(MatrixStack modelMatrix, float trunkHeight, float coneHeight) {
		modelMatrix.pushMatrix();

		modelMatrix.getTop().scale(1, trunkHeight, 1).translate(0, 0.5f, 0);

		uniformColorTint.program.begin();
		glUniformMatrix4(uniformColorTint.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
		glUniform4f(uniformColorTint.baseColorUniform, 0.694f, 0.4f, 0.106f, 1);
		cylinderMesh.render();
		uniformColorTint.program.end();

		modelMatrix.popMatrix();

		modelMatrix.pushMatrix();

		modelMatrix.getTop().translate(0, trunkHeight, 0).scale(3, coneHeight, 3);

		uniformColorTint.program.begin();
		glUniformMatrix4(uniformColorTint.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
		glUniform4f(uniformColorTint.baseColorUniform, 0, 1, 0, 1);
		coneMesh.render();
		uniformColorTint.program.end();

		modelMatrix.popMatrix();
	}

	private final float parthenonWidth = 14;
	private final float parthenonLength = 20;
	private final float parthenonColumnHeight = 5;
	private final float parthenonBaseHeight = 1;
	private final float parthenonTopHeight = 2;

	private final float frontZVal = parthenonLength / 2 - 1;
	private final float rightXVal = parthenonWidth / 2 - 1;

	private void drawParthenon(MatrixStack modelMatrix) {
		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().scale(parthenonWidth, parthenonBaseHeight, parthenonLength).translate(0, 0.5f, 0);

			uniformColorTint.program.begin();
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4f(uniformColorTint.baseColorUniform, 0.9f, 0.9f, 0.9f, 0.9f);
			cubeTintMesh.render();
			uniformColorTint.program.end();

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().translate(0, parthenonColumnHeight + parthenonBaseHeight, 0)
					.scale(parthenonWidth, parthenonTopHeight, parthenonLength)
					.translate(0, 0.5f, 0);

			uniformColorTint.program.begin();
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4f(uniformColorTint.baseColorUniform, 0.9f, 0.9f, 0.9f, 0.9f);
			cubeTintMesh.render();
			uniformColorTint.program.end();

			modelMatrix.popMatrix();
		}

		for(int a = 0; a < parthenonWidth / 2; a++) {
			{
				modelMatrix.pushMatrix();
				modelMatrix.getTop().translate(2 * a - parthenonWidth / 2 + 1, parthenonBaseHeight, frontZVal);
				drawColumn(modelMatrix);
				modelMatrix.popMatrix();
			}

			{
				modelMatrix.pushMatrix();
				modelMatrix.getTop().translate(2 * a - parthenonWidth / 2 + 1, parthenonBaseHeight, -frontZVal);
				drawColumn(modelMatrix);
				modelMatrix.popMatrix();
			}
		}

		for(int a = 1; a < (parthenonLength - 2) / 2; a++) {
			{
				modelMatrix.pushMatrix();
				modelMatrix.getTop().translate(rightXVal, parthenonBaseHeight, 2 * a - parthenonLength / 2 + 1);
				drawColumn(modelMatrix);
				modelMatrix.popMatrix();
			}

			{
				modelMatrix.pushMatrix();
				modelMatrix.getTop().translate(-rightXVal, parthenonBaseHeight, 2 * a - parthenonLength / 2 + 1);
				drawColumn(modelMatrix);
				modelMatrix.popMatrix();
			}
		}

		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().translate(0, 1, 0).scale(parthenonWidth - 6, parthenonColumnHeight, parthenonLength - 6).translate(0, 0.5f, 0);

			objectColor.program.begin();
			glUniformMatrix4(objectColor.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			cubeColorMesh.render();
			objectColor.program.end();

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().translate(0, parthenonColumnHeight + parthenonBaseHeight + parthenonTopHeight / 2, parthenonLength / 2)
					.rotate(-135 * (float)Math.PI / 180, 1, 0, 0)
					.rotate(45 * (float)Math.PI / 180, 0, 1, 0);

			objectColor.program.begin();
			glUniformMatrix4(objectColor.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			cubeColorMesh.render();
			objectColor.program.end();

			modelMatrix.popMatrix();
		}
	}

	private final float columnBaseHeight = 0.25f;

	private void drawColumn(MatrixStack modelMatrix) {
		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().scale(1, columnBaseHeight, 1).translate(0, 0.5f, 0);

			uniformColorTint.program.begin();
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4f(uniformColorTint.baseColorUniform, 1, 1, 1, 1);
			cubeTintMesh.render();
			uniformColorTint.program.end();

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().translate(0, parthenonColumnHeight - columnBaseHeight, 0).scale(1, columnBaseHeight, 1).translate(0, 0.5f, 0);

			uniformColorTint.program.begin();
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4f(uniformColorTint.baseColorUniform, 0.9f, 0.9f, 0.9f, 0.9f);
			cubeTintMesh.render();
			uniformColorTint.program.end();

			modelMatrix.popMatrix();
		}

		{
			modelMatrix.pushMatrix();

			modelMatrix.getTop().translate(0, columnBaseHeight, 0).scale(0.8f, parthenonColumnHeight - columnBaseHeight * 2, 0.8f).translate(0, 0.5f, 0);

			uniformColorTint.program.begin();
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUniform, false, modelMatrix.getTop().toBuffer());
			glUniform4f(uniformColorTint.baseColorUniform, 0.9f, 0.9f, 0.9f, 0.9f);
			cylinderMesh.render();
			uniformColorTint.program.end();

			modelMatrix.popMatrix();
		}
	}

	private static class ProgramData {
		private ShaderProgram program;
		private int modelToWorldMatrixUniform;
		private int worldToCameraMatrixUniform;
		private int cameraToClipMatrixUniform;
		private int baseColorUniform;

		public ProgramData(ShaderProgram program) {
			this.program = program;
		}
	}

	private static class TreeData {
		private float x, z, trunkHeight, coneHeight;

		public TreeData(float x, float z, float trunkHeight, float coneHeight) {
			this.x = x;
			this.z = z;
			this.trunkHeight = trunkHeight;
			this.coneHeight = coneHeight;
		}
	}
}
