package com.ra4king.opengl.util;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.MouseModifier;
import com.ra4king.opengl.util.MousePoles.ObjectPole;
import com.ra4king.opengl.util.MousePoles.ViewPole;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;

public class Utils {
	public static Quaternion angleAxisDeg(float angle, Vector3 vec) {
		return new Quaternion((float)Math.toRadians(angle), vec);
	}
	
	public static float clamp(float value, float low, float high) {
		return Math.min(Math.max(value, low), high);
	}
	
	public static float mix(float f1, float f2, float a) {
		return f1 + (f2-f1) * a; 
	}
	
	public static void updateMousePoles(ViewPole viewPole, ObjectPole objectPole) {
		while(Mouse.next()) {
			MouseButton button = MouseButton.getButton(Mouse.getEventButton());
			if(button != null) {
				boolean pressed = Mouse.getEventButtonState();
				viewPole.mouseClick(button, pressed, getModifier(), Mouse.getX(), Mouse.getY());
				objectPole.mouseClick(button, pressed, getModifier(), Mouse.getX(), Mouse.getY());
			}
			else {
				int dwheel = Mouse.getDWheel();
				
				if(dwheel != 0) {
					viewPole.mouseWheel(dwheel, getModifier(), Mouse.getX(), Mouse.getY());
					objectPole.mouseWheel(dwheel, getModifier(), Mouse.getX(), Mouse.getY());
				}
				else {
					viewPole.mouseMove(Mouse.getX(), Mouse.getY());
					objectPole.mouseMove(Mouse.getX(), Mouse.getY());
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
}
