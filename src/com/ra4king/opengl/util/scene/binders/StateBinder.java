package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import java.util.HashMap;

import com.ra4king.opengl.util.ShaderProgram;

public interface StateBinder {
	void bindState(ShaderProgram program);
	
	void unbindState(ShaderProgram program);
}

abstract class UniformBinderBase implements StateBinder {
	private HashMap<ShaderProgram,Integer> programUniformLocation = new HashMap<>();
	
	public void associateWithProgram(ShaderProgram program, String uniform) {
		programUniformLocation.put(program, glGetUniformLocation(program.getProgram(), uniform));
	}
	
	protected int getUniformLocation(ShaderProgram program) {
		Integer i = programUniformLocation.get(program);
		if(i == -1)
			throw new IllegalArgumentException("Unassociated program");
		return i;
	}
}
