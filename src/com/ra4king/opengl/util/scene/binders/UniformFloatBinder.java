package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

public class UniformFloatBinder extends UniformBinderBase {
	private float value;
	
	public void setValue(float value) {
		this.value = value;
	}
	
	@Override
	public void bindState(int program) {
		glUniform1f(getUniformLocation(program), value);
	}
	
	@Override
	public void unbindState(int program) {}
}
