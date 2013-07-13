package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.ShaderProgram;

public class UniformFloatBinder extends UniformBinderBase {
	private float value;
	
	public UniformFloatBinder() {}
	
	public UniformFloatBinder(float value) {
		setValue(value);
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
	public float getValue() {
		return value;
	}
	
	@Override
	public void bindState(ShaderProgram program) {
		glUniform1f(getUniformLocation(program), value);
	}
	
	@Override
	public void unbindState(ShaderProgram program) {}
}
