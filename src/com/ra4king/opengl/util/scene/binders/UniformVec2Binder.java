package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.math.Vector2;

public class UniformVec2Binder extends UniformBinderBase {
	private Vector2 value = new Vector2();
	
	public void setValue(Vector2 vec) {
		value.set(vec);
	}
	
	@Override
	public void bindState(int program) {
		glUniform2(getUniformLocation(program), value.toBuffer());
	}
	
	@Override
	public void unbindState(int program) {}
}
