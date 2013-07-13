package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.ShaderProgram;

public class UniformIntBinder extends UniformBinderBase {
	private int value;
	
	public UniformIntBinder() {}
	
	public UniformIntBinder(int value) {
		setValue(value);
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public void bindState(ShaderProgram program) {
		glUniform1i(getUniformLocation(program), value);
	}
	
	@Override
	public void unbindState(ShaderProgram program) {}
}
