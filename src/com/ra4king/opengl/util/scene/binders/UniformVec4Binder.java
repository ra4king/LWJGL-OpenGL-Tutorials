package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.math.Vector4;

public class UniformVec4Binder extends UniformBinderBase {
	private Vector4 value = new Vector4();
	
	public void setValue(Vector4 vec) {
		value.set(vec);
	}
	
	@Override
	public void bindState(int program) {
		glUniform4(getUniformLocation(program), value.toBuffer());
	}
	
	@Override
	public void unbindState(int program) {}
}
