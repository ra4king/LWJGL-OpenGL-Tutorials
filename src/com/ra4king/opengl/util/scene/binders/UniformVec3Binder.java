package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Vector3;

public class UniformVec3Binder extends UniformBinderBase {
	private Vector3 value = new Vector3();
	
	public UniformVec3Binder() {}
	
	public UniformVec3Binder(Vector3 vec) {
		setValue(vec);
	}
	
	public void setValue(Vector3 vec) {
		value.set(vec);
	}
	
	public Vector3 getValue() {
		return value.copy();
	}
	
	@Override
	public void bindState(ShaderProgram program) {
		glUniform3(getUniformLocation(program), value.toBuffer());
	}
	
	@Override
	public void unbindState(ShaderProgram program) {}
}
