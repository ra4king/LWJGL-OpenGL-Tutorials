package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

import com.ra4king.opengl.util.ShaderProgram;

public class UniformBlockBinder implements StateBinder {
	public int blockIndex;
	public int uniformBuffer;
	public int bufferOffset;
	public int bufferSize;
	
	public UniformBlockBinder() {}
	
	public UniformBlockBinder(int blockIndex, int uniformBuffer, int bufferOffset, int bufferSize) {
		setValue(blockIndex, uniformBuffer, bufferOffset, bufferSize);
	}
	
	public void setValue(int blockIndex, int uniformBuffer, int bufferOffset, int bufferSize) {
		this.blockIndex = blockIndex;
		this.uniformBuffer = uniformBuffer;
		this.bufferOffset = bufferOffset;
		this.bufferSize = bufferSize;
	}
	
	@Override
	public void bindState(ShaderProgram program) {
		glBindBufferRange(GL_UNIFORM_BUFFER, blockIndex, uniformBuffer, bufferOffset, bufferSize);
	}
	
	@Override
	public void unbindState(ShaderProgram program) {
		glBindBufferBase(GL_UNIFORM_BUFFER, blockIndex, 0);
	}
}
