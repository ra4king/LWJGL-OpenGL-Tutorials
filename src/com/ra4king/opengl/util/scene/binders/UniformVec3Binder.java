package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.math.Vector3;

public class UniformVec3Binder extends UniformBinderBase {
	private Vector3 value = new Vector3();
	
	public void setValue(Vector3 vec) {
		value.set(vec);
	}
	
	@Override
	public void bindState(int program) {
		glUniform3(getUniformLocation(program), value.toBuffer());
	}
	
	@Override
	public void unbindState(int program) {}
}
