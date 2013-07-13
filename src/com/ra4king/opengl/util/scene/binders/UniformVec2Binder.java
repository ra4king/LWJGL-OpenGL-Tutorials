package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL20.*;

import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.math.Vector2;

public class UniformVec2Binder extends UniformBinderBase {
	private Vector2 value = new Vector2();
	
	public UniformVec2Binder() {}
	
	public UniformVec2Binder(Vector2 vec) {
		setValue(vec);
	}
	
	public void setValue(Vector2 vec) {
		value.set(vec);
	}
	
	public Vector2 getValue() {
		return value.copy();
	}
	
	@Override
	public void bindState(ShaderProgram program) {
		glUniform2(getUniformLocation(program), value.toBuffer());
	}
	
	@Override
	public void unbindState(ShaderProgram program) {}
}
