package com.ra4king.opengl.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL31.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class UniformBlockArray<T extends UniformBlockArray.UniformBlockObject> {
	private final FloatBuffer storage;
	private final int blockOffset;
	private final int arrayCount;
	
	private int maxPosition;
	
	public UniformBlockArray(int uniformBlockObjectSize, int arrayCount) {
		this.arrayCount = arrayCount;
		
		int uniformBufferAlignSize = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
		
		blockOffset = uniformBlockObjectSize + uniformBufferAlignSize - uniformBlockObjectSize % uniformBufferAlignSize;
		
		storage = BufferUtils.createFloatBuffer(blockOffset * arrayCount / 4);
	}
	
	public int size() {
		return arrayCount;
	}
	
	public int getArrayOffset() {
		return blockOffset;
	}
	
	public void setBlockMember(int index, T member) {
		storage.position(index * blockOffset / 4);
		storage.put(member.toBuffer());
		
		if(storage.position() > maxPosition)
			maxPosition = storage.position();
	}
	
	public int createBufferObject() {
		int bufferObject = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, bufferObject);
		glBufferData(GL_UNIFORM_BUFFER, (FloatBuffer)storage.position(maxPosition).flip(), GL_STATIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		
		return bufferObject;
	}
	
	public static interface UniformBlockObject {
		public FloatBuffer toBuffer();
	}
}
