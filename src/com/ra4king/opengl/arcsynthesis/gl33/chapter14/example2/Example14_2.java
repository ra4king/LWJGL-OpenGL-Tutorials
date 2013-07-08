package com.ra4king.opengl.arcsynthesis.gl33.chapter14.example2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix4;

public class Example14_2 extends GLProgram {
	public static void main(String[] args) {
		new Example14_2().run(true);
	}
	
	private ProgramData smoothInterpolation;
	private ProgramData linearInterpolation;
	
	private Mesh realHallway;
	private Mesh fauxHallway;
	
	private boolean useFakeHallway, useSmoothInterpolation = true;
	
	public Example14_2() {
		super("Example 14.2 - Perspective Interpolation", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		smoothInterpolation = loadProgram("example14.2.SmoothVertexColors.vert", "example14.2.SmoothVertexColors.frag");
		linearInterpolation = loadProgram("example14.2.NoCorrectVertexColors.vert", "example14.2.NoCorrectVertexColors.frag");
		
		Matrix4 perspectiveMatrix = new Matrix4().clearToPerspectiveDeg(60, 1, 1, 1, 1000);
		
		smoothInterpolation.program.begin();
		glUniformMatrix4(smoothInterpolation.cameraToClipMatrixUniform, false, perspectiveMatrix.toBuffer());
		linearInterpolation.program.begin();
		glUniformMatrix4(linearInterpolation.cameraToClipMatrixUniform, false, perspectiveMatrix.toBuffer());
		linearInterpolation.program.end();
		
		try {
			realHallway = new Mesh(getClass().getResource("example14.2.RealHallway.xml"));
			fauxHallway = new Mesh(getClass().getResource("example14.2.FauxHallway.xml"));
		} catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
	}
	
	private ProgramData loadProgram(String vertex, String fragment) {
		ProgramData data = new ProgramData(new ShaderProgram(readFromFile(vertex), readFromFile(fragment)));
		data.cameraToClipMatrixUniform = data.program.getUniformLocation("cameraToClipMatrix");
		
		return data;
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch(key) {
			case Keyboard.KEY_S:
				useFakeHallway = !useFakeHallway;
				if(useFakeHallway)
					System.out.println("Fake Hallway");
				else
					System.out.println("Real Hallway");
				break;
			case Keyboard.KEY_P:
				useSmoothInterpolation = !useSmoothInterpolation;
				if(useSmoothInterpolation)
					System.out.println("Perspective correct interpolation.");
				else
					System.out.println("Just lienar interpolation.");
				break;
		}
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		if(useSmoothInterpolation)
			smoothInterpolation.program.begin();
		else
			linearInterpolation.program.begin();
		
		if(useFakeHallway)
			fauxHallway.render();
		else
			realHallway.render();
		
		glUseProgram(0);
	}
	
	private static class ProgramData {
		private ShaderProgram program;
		
		private int cameraToClipMatrixUniform;
		
		public ProgramData(ShaderProgram program) {
			this.program = program;
		}
	}
}
