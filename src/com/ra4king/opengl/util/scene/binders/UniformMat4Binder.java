package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.math.Matrix4;

public class UniformMat4Binder extends UniformBinderBase {
	private Matrix4 value = new Matrix4().clearToIdentity();
	
	public void setValue(Matrix4 mat) {
		value.set(mat);
	}
	
	@Override
	public void bindState(int program) {
		glUniformMatrix4(getUniformLocation(program), false, value.toBuffer());
	}
	
	@Override
	public void unbindState(int program) {}
}
