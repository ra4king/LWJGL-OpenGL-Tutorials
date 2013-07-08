package com.ra4king.opengl.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.MouseModifier;
import com.ra4king.opengl.util.MousePoles.ObjectPole;
import com.ra4king.opengl.util.MousePoles.ViewPole;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector2;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class Utils {
	public static void checkGLError(String event) {
		int error;
		if((error = glGetError()) != GL_NO_ERROR)
			throw new RuntimeException("OpenGL Error during " + event + ": " + gluErrorString(error));
	}
	
	public static Quaternion angleAxisDeg(float angle, Vector3 vec) {
		return new Quaternion((float)Math.toRadians(angle), vec);
	}
	
	public static float clamp(float value, float low, float high) {
		return Math.min(Math.max(value, low), high);
	}
	
	public static float mix(float f1, float f2, float a) {
		return f1 + (f2 - f1) * a;
	}
	
	public static Matrix4 lookAt(Vector3 eye, Vector3 center, Vector3 up) {
		Vector3 f = center.copy().sub(eye).normalize();
		up = up.copy().normalize();
		
		Vector3 s = f.cross(up);
		Vector3 u = s.cross(f);
		
		return new Matrix4(new float[] {
				s.x(), u.x(), -f.x(), 0,
				s.y(), u.y(), -f.y(), 0,
				s.z(), u.z(), -f.z(), 0,
				0, 0, 0, 1
		}).translate(eye.copy().mult(-1));
	}
	
	public static void updateMousePoles(ViewPole... viewPoles) {
		updateMousePoles(viewPoles, null);
	}
	
	public static void updateMousePoles(ObjectPole... objectPoles) {
		updateMousePoles(null, objectPoles);
	}
	
	public static void updateMousePoles(ViewPole viewPole, ObjectPole objectPole) {
		updateMousePoles(viewPole == null ? null : new ViewPole[] { viewPole }, objectPole == null ? null : new ObjectPole[] { objectPole });
	}
	
	public static void updateMousePoles(ViewPole[] viewPoles, ObjectPole[] objectPoles) {
		while(Mouse.next()) {
			MouseButton button = MouseButton.getButton(Mouse.getEventButton());
			if(button != null) {
				boolean pressed = Mouse.getEventButtonState();
				if(viewPoles != null)
					for(ViewPole v : viewPoles)
						v.mouseClick(button, pressed, getModifier(), new Vector2(Mouse.getX(), Mouse.getY()));
				if(objectPoles != null)
					for(ObjectPole o : objectPoles)
						o.mouseClick(button, pressed, getModifier(), new Vector2(Mouse.getX(), Mouse.getY()));
			} else {
				int dwheel = Mouse.getDWheel();
				
				if(dwheel != 0) {
					if(viewPoles != null)
						for(ViewPole v : viewPoles)
							v.mouseWheel(dwheel, getModifier());
					if(objectPoles != null)
						for(ObjectPole o : objectPoles)
							o.mouseWheel(dwheel, getModifier());
				} else {
					if(viewPoles != null)
						for(ViewPole v : viewPoles)
							v.mouseMove(new Vector2(Mouse.getX(), Mouse.getY()));
					if(objectPoles != null)
						for(ObjectPole o : objectPoles)
							o.mouseMove(new Vector2(Mouse.getX(), Mouse.getY()));
				}
			}
		}
	}
	
	private static MouseModifier getModifier() {
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			return MouseModifier.KEY_SHIFT;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
			return MouseModifier.KEY_CTRL;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			return MouseModifier.KEY_ALT;
		
		return null;
	}
	
	public static String readFully(InputStream is) {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			StringBuilder s = new StringBuilder();
			String l;
			
			while((l = reader.readLine()) != null)
				s.append(l).append('\n');
			
			return s.toString();
		} catch(Exception exc) {
			throw new RuntimeException("Failure reading input stream", exc);
		}
	}
	
	public static Quaternion parseQuaternion(String s) {
		String[] comp = StringUtil.split(s, ' ');
		if(comp.length != 4)
			throw new IllegalArgumentException("invalid Quaternion");
		
		Quaternion quat = new Quaternion();
		quat.x(Float.parseFloat(comp[0]));
		quat.y(Float.parseFloat(comp[1]));
		quat.z(Float.parseFloat(comp[2]));
		quat.w(Float.parseFloat(comp[3]));
		
		return quat;
	}
	
	public static Vector4 parseVector4(String s) {
		String[] comp = StringUtil.split(s, ' ');
		if(comp.length != 4)
			throw new IllegalArgumentException("invalid Vector4");
		
		Vector4 vec = new Vector4();
		vec.x(Float.parseFloat(comp[0]));
		vec.y(Float.parseFloat(comp[1]));
		vec.z(Float.parseFloat(comp[2]));
		vec.w(Float.parseFloat(comp[3]));
		
		return vec;
	}
	
	public static Vector3 parseVector3(String s) throws NumberFormatException {
		String[] comp = StringUtil.split(s, ' ');
		if(comp.length != 3)
			throw new IllegalArgumentException("invalid Vector3");
		
		Vector3 vec = new Vector3();
		vec.x(Float.parseFloat(comp[0]));
		vec.y(Float.parseFloat(comp[1]));
		vec.z(Float.parseFloat(comp[2]));
		
		return vec;
	}
	
	public static Vector2 parseVector2(String s) throws NumberFormatException {
		String[] comp = StringUtil.split(s, ' ');
		if(comp.length != 2)
			throw new IllegalArgumentException("invalid Vector2");
		
		Vector2 vec = new Vector2();
		vec.x(Float.parseFloat(comp[0]));
		vec.y(Float.parseFloat(comp[1]));
		
		return vec;
	}
}
