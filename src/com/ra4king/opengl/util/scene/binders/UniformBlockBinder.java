package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

public class UniformBlockBinder implements StateBinder {
	private int blockIndex;
	private int uniformBuffer;
	private int bufferOffset;
	private int bufferSize;
	
	public void setBlock(int blockIndex, int uniformBuffer, int bufferOffset, int bufferSize) {
		this.blockIndex = blockIndex;
		this.uniformBuffer = uniformBuffer;
		this.bufferOffset = bufferOffset;
		this.bufferSize = bufferSize;
	}
	
	@Override
	public void bindState(int program) {
		glBindBufferRange(GL_UNIFORM_BUFFER, blockIndex, uniformBuffer, bufferOffset, bufferSize);
	}
	
	@Override
	public void unbindState(int program) {
		glBindBufferBase(GL_UNIFORM_BUFFER, blockIndex, 0);
	}
}
