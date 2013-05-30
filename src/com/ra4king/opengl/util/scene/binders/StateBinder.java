package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import java.util.HashMap;

public interface StateBinder {
	void bindState(int program);
	
	void unbindState(int program);
}

abstract class UniformBinderBase implements StateBinder {
	private HashMap<Integer,Integer> programUniformLocation = new HashMap<>();
	
	public void associateWithProgram(int program, String uniform) {
		programUniformLocation.put(program, glGetUniformLocation(program, uniform));
	}
	
	protected int getUniformLocation(int program) {
		Integer i = programUniformLocation.get(program);
		if(i == null)
			return -1;
		return i;
	}
}
