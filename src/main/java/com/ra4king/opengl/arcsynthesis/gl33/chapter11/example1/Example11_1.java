package com.ra4king.opengl.arcsynthesis.gl33.chapter11.example1;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.ObjectData;
import com.ra4king.opengl.util.MousePoles.ObjectPole;
import com.ra4king.opengl.util.MousePoles.ViewData;
import com.ra4king.opengl.util.MousePoles.ViewPole;
import com.ra4king.opengl.util.MousePoles.ViewScale;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Example11_1 extends GLProgram {
	public static void main(String[] args) {
		new Example11_1().run(true);
	}
	
	private static enum LightingModel {
		PURE_DIFFUSE, DIFFUSE_AND_SPECULAR, SPECULAR_ONLY
	}
	
	private ProgramData whiteNoPhong;
	private ProgramData colorNoPhong;
	
	private ProgramData whitePhong;
	private ProgramData colorPhong;
	
	private ProgramData whitePhongOnly;
	private ProgramData colorPhongOnly;
	
	private UnlitProgramData unlit;
	
	private int projectionUniformBuffer;
	private final int projectionBlockIndex = 2;
	
	private Mesh cylinderMesh;
	private Mesh planeMesh;
	private Mesh cubeMesh;
	
	private ViewPole viewPole;
	private ObjectPole objectPole;
	
	private Timer lightTimer = new Timer(Timer.Type.LOOP, 5);
	private float lightHeight = 1.5f, lightRadius = 1;
	
	private LightingModel lightModel = LightingModel.DIFFUSE_AND_SPECULAR;
	
	private boolean useFragmentLighting = true, drawColoredCyl, drawLightSource, scaleCyl, drawDark;
	
	private float lightAttenuation = 1.2f, shininessFactor = 4;
	
	private Vector4 darkColor = new Vector4(0.2f, 0.2f, 0.2f, 1);
	private Vector4 lightColor = new Vector4(1);
	
	public Example11_1() {
		super("Example 11.1", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		ViewData initialViewData = new ViewData(new Vector3(0, 0.5f, 0), new Quaternion(0.3826834f, 0, 0, 0.92387953f), 5, 0);
		ViewScale viewScale = new ViewScale(3, 20, 1.5f, 0.5f, 0, 0, 90f / 250f);
		ObjectData initialObjectData = new ObjectData(new Vector3(0, 0.5f, 0), new Quaternion());
		
		viewPole = new ViewPole(initialViewData, viewScale, MouseButton.LEFT_BUTTON, false);
		objectPole = new ObjectPole(initialObjectData, 90f / 250f, MouseButton.RIGHT_BUTTON, viewPole);
		
		whiteNoPhong = loadLitProgram("example11.1.PN.vert", "example11.1.NoPhong.frag");
		colorNoPhong = loadLitProgram("example11.1.PCN.vert", "example11.1.NoPhong.frag");
		
		whitePhong = loadLitProgram("example11.1.PN.vert", "example11.1.PhongLighting.frag");
		colorPhong = loadLitProgram("example11.1.PCN.vert", "example11.1.PhongLighting.frag");
		
		whitePhongOnly = loadLitProgram("example11.1.PN.vert", "example11.1.PhongOnly.frag");
		colorPhongOnly = loadLitProgram("example11.1.PCN.vert", "example11.1.PhongOnly.frag");
		
		unlit = loadUnlitProgram("example11.1.PosTransform.vert", "example11.1.UniformColor.frag");
		
		try {
			cylinderMesh = new Mesh(getClass().getResource("example11.1.UnitCylinder.xml"));
			planeMesh = new Mesh(getClass().getResource("example11.1.LargePlane.xml"));
			cubeMesh = new Mesh(getClass().getResource("example11.1.UnitCube.xml"));
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
	}
	
	private ProgramData loadLitProgram(String vertFile, String fragFile) {
		ProgramData data = new ProgramData(new ShaderProgram(vertFile, fragFile));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.lightIntensityUniform = glGetUniformLocation(data.program.getProgram(), "lightIntensity");
		data.ambientIntensityUniform = glGetUniformLocation(data.program.getProgram(), "ambientIntensity");
		
		data.normalModelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "normalModelToCameraMatrix");
		data.cameraSpaceLightPosUniform = glGetUniformLocation(data.program.getProgram(), "cameraSpaceLightPos");
		data.lightAttenuationUniform = glGetUniformLocation(data.program.getProgram(), "lightAttenuation");
		data.shininessFactorUniform = glGetUniformLocation(data.program.getProgram(), "shininessFactor");
		data.baseDiffuseColorUniform = glGetUniformLocation(data.program.getProgram(), "baseDiffuseColor");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
	}
	
	private UnlitProgramData loadUnlitProgram(String vertFile, String fragFile) {
		UnlitProgramData data = new UnlitProgramData(new ShaderProgram(vertFile, fragFile));
		data.modelToCameraMatrixUniform = glGetUniformLocation(data.program.getProgram(), "modelToCameraMatrix");
		data.objectColorUniform = glGetUniformLocation(data.program.getProgram(), "objectColor");
		
		int projectionBlock = glGetUniformBlockIndex(data.program.getProgram(), "Projection");
		glUniformBlockBinding(data.program.getProgram(), projectionBlock, projectionBlockIndex);
		
		return data;
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
		Utils.updateMousePoles(viewPole, objectPole);
		
		lightTimer.update(deltaTime);
	}
	
	private Vector4 calcLightPosition() {
		float currTime = lightTimer.getAlpha();
		
		Vector4 ret = new Vector4(0, lightHeight, 0, 1);
		ret.x((float)Math.cos(currTime * 2 * Math.PI) * lightRadius);
		ret.z((float)Math.sin(currTime * 2 * Math.PI) * lightRadius);
		
		return ret;
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		
	}
	
	private static class ProgramData {
		private ShaderProgram program;
		
		private int modelToCameraMatrixUniform;
		
		private int lightIntensityUniform;
		private int ambientIntensityUniform;
		
		private int normalModelToCameraMatrixUniform;
		private int cameraSpaceLightPosUniform;
		private int lightAttenuationUniform;
		private int shininessFactorUniform;
		private int baseDiffuseColorUniform;
		
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
}
