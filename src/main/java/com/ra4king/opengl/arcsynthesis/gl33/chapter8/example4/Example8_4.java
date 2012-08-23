package com.ra4king.opengl.arcsynthesis.gl33.chapter8.example4;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.GLProgram;
import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.MatrixStack;
import com.ra4king.opengl.util.math.Quaternion;

public class Example8_4 extends GLProgram {
	public static void main(String[] args) {
		new Example8_4().run(true);
	}
	
	private ShaderProgram program;
	
	private int modelToCameraMatrixUniform;
	private int cameraToClipMatrixUniform;
	private int baseColorUniform;
	
	private Mesh ship;
	
	private Orientation orientation;
	
	private final Quaternion[] orients = {
			new Quaternion(0.7071f, 0, 0, 0.7071f),
			new Quaternion(0.5f, -0.5f, 0.5f, 0.5f),
			new Quaternion(-0.7892f, -0.37f, -0.02514f, -0.4895f),
			new Quaternion(0.7892f, 0.37f, 0.02514f, 0.4895f),
			
			new Quaternion(-0.1591f, -0.7991f, -0.4344f, 0.384f),
			new Quaternion(0.5208f, 0.6483f, 0.041f, 0.5537f),
			new Quaternion(0, 1, 0, 0)
	};
	
	private final int[] orientKeys = {
			Keyboard.KEY_Q,
			Keyboard.KEY_W,
			Keyboard.KEY_E,
			Keyboard.KEY_R,
			
			Keyboard.KEY_T,
			Keyboard.KEY_Y,
			Keyboard.KEY_U
	};
	
	public Example8_4() {
		super("Example 8.4", 500, 500, true);
	}
	
	@Override
	public void init() {
		glClearColor(0, 0, 0, 0);
		glClearDepth(1);
		
		program = new ShaderProgram(readFromFile("example8.4.vert"), readFromFile("example8.4.frag"));
		
		modelToCameraMatrixUniform = glGetUniformLocation(program.getProgram(), "modelToCameraMatrix");
		cameraToClipMatrixUniform = glGetUniformLocation(program.getProgram(), "cameraToClipMatrix");
		baseColorUniform = glGetUniformLocation(program.getProgram(), "baseColor");
		
		try {
			ship = new Mesh(getClass().getResource("example8.4.Ship.xml"));
		}
		catch(Exception exc) {
			exc.printStackTrace();
			destroy();
		}
		
		orientation = new Orientation();
		
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
		
		program.begin();
		glUniformMatrix4(cameraToClipMatrixUniform, false, new Matrix4().clearToPerspectiveDeg(20, getWidth(), getHeight(), 1, 600).toBuffer());
		program.end();
	}
	
	private void applyOrientation(int index) {
		if(!orientation.isAnimating())
			orientation.animateToOrient(index);
	}
	
	@Override
	public void update(long deltaTime) {
		orientation.updateTime(deltaTime);
	}
	
	@Override
	public void keyPressed(int key, char c, long nanos) {
		if(key == Keyboard.KEY_SPACE) {
			boolean slerp = orientation.toggleSlerp();
			System.out.println(slerp ? "Slerp" : "Lerp");
		}
		
		for(int a = 0; a < orientKeys.length; a++)
			if(key == orientKeys[a])
				applyOrientation(a);
	}
	
	@Override
	public void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		MatrixStack stack = new MatrixStack();
		stack.getTop().translate(0, 0, -200).mult(orientation.getOrient().toMatrix());
		
		program.begin();
		stack.getTop().scale(3, 3, 3).rotateDeg(-90, 1, 0, 0);
		glUniform4f(baseColorUniform, 1, 1, 1, 1);
		glUniformMatrix4(modelToCameraMatrixUniform, false, stack.getTop().toBuffer());
		ship.render("tint");
		program.end();
	}
	
	private class Orientation {
		private Animation animation;
		
		private boolean isAnimating;
		private boolean slerp;
		private int currentOrient;
		
		public Orientation() {
			animation = new Animation();
		}
		
		public boolean toggleSlerp() {
			slerp = !slerp;
			return slerp;
		}
		
		public Quaternion getOrient() {
			if(isAnimating)
				return animation.getOrient(orients[currentOrient], slerp);
			else
				return orients[currentOrient];
		}
		
		public boolean isAnimating() {
			return isAnimating;
		}
		
		public void updateTime(long deltaTime) {
			if(isAnimating && animation.updateTime(deltaTime)) {
				isAnimating = false;
				currentOrient = animation.getFinalOrient();
			}
		}
		
		public void animateToOrient(int destination) {
			if(currentOrient == destination)
				return;
			
			animation.startAnimation(destination, 1);
			isAnimating = true;
		}
		
		private class Animation {
			private Timer timer;
			private int finalOrient;
			
			public boolean updateTime(long deltaTime) {
				return timer.update(deltaTime);
			}
			
			public Quaternion getOrient(Quaternion initial, boolean slerp) {
				if(slerp)
					return slerp(initial, orients[finalOrient], timer.getAlpha());
				else
					return lerp(initial, orients[finalOrient], timer.getAlpha());
			}
			
			private float clamp(float value, float low, float high) {
				return Math.min(Math.max(value, low), high);
			}
			
			private Quaternion slerp(Quaternion q0, Quaternion q1, float alpha) {
				q0 = new Quaternion(q0);
				q1 = new Quaternion(q1);
				
				float dot = q0.dot(q1);
				
				float DOT_THRESHOLD = 0.9995f;
				if(dot > DOT_THRESHOLD)
					return lerp(q0, q1, alpha);
				
				dot = clamp(dot, -1, 1);
				float theta = (float)Math.acos(dot) * alpha;
				
				Quaternion q2 = q1.add(new Quaternion(q0).mult(-dot)).normalize();
				
				return q0.mult((float)Math.cos(theta)).add(q2.mult((float)Math.sin(theta)));
			}
			
			private Quaternion lerp(Quaternion q0, Quaternion q1, float alpha) {
				return new Quaternion(q0.x() + (q1.x() - q0.x()) * alpha,
									  q0.y() + (q1.y() - q0.y()) * alpha,
									  q0.z() + (q1.z() - q0.z()) * alpha,
									  q0.w() + (q1.w() - q0.w()) * alpha).normalize();
			}
			
			public void startAnimation(int destination, float duration) {
				finalOrient = destination;
				timer = new Timer(Type.SINGLE, duration);
			}
			
			public int getFinalOrient() {
				return finalOrient;
			}
		}
	}
}
