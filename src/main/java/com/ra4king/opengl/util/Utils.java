package com.ra4king.opengl.util;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.ra4king.opengl.util.MousePoles.MouseButton;
import com.ra4king.opengl.util.MousePoles.MouseModifier;
import com.ra4king.opengl.util.MousePoles.ObjectPole;
import com.ra4king.opengl.util.MousePoles.ViewPole;

public class Utils {
	public static float clamp(float value, float low, float high) {
		return Math.min(Math.max(value, low), high);
	}
	
	public static void updateMousePoles(ViewPole viewPole, ObjectPole objectPole) {
		while(Mouse.next()) {
			viewPole.mouseMove(Mouse.getX(), Mouse.getY());
			objectPole.mouseMove(Mouse.getX(), Mouse.getY());
			
			MouseButton button = MouseButton.getButton(Mouse.getEventButton());
			if(button != null) {
				viewPole.mouseClick(button, Mouse.getEventButtonState(), Utils.getModifier(), Mouse.getX(), Mouse.getY());
				objectPole.mouseClick(button, Mouse.getEventButtonState(), Utils.getModifier(), Mouse.getX(), Mouse.getY());
			}
			
			viewPole.mouseWheel(Mouse.getEventDWheel(), Utils.getModifier(), Mouse.getX(), Mouse.getY());
		}
	}
	
	public static MouseModifier getModifier() {
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			return MouseModifier.KEY_SHIFT;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
			return MouseModifier.KEY_CTRL;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))
			return MouseModifier.KEY_ALT;
		
		return null;
	}
}
