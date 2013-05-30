package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

public class UniformIntBinder extends UniformBinderBase {
	private int value;
	
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public void bindState(int program) {
		glUniform1i(getUniformLocation(program), value);
	}
	
	@Override
	public void unbindState(int program) {}
}
