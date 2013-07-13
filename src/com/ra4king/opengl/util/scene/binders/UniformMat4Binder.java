package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Matrix4;

public class UniformMat4Binder extends UniformBinderBase {
	private Matrix4 value = new Matrix4().clearToIdentity();
	
	public UniformMat4Binder() {}
	
	public UniformMat4Binder(Matrix4 mat) {
		setValue(mat);
	}
	
	public void setValue(Matrix4 mat) {
		value.set(mat);
	}
	
	public Matrix4 getValue() {
		return value.copy();
	}
	
	@Override
	public void bindState(ShaderProgram program) {
		glUniformMatrix4(getUniformLocation(program), false, value.toBuffer());
	}
	
	@Override
	public void unbindState(ShaderProgram program) {}
}
