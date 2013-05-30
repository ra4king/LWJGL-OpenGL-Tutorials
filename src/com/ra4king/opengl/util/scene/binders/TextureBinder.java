package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL33.*;

public class TextureBinder implements StateBinder {
	private int texUnit;
	private int texType = GL_TEXTURE_2D;
	private int tex;
	private int sampler;
	
	public void setTexture(int texUnit, int texType, int tex, int sampler) {
		this.texUnit = texUnit;
		this.texType = texType;
		this.tex = tex;
		this.sampler = sampler;
	}
	
	@Override
	public void bindState(int program) {
		glActiveTexture(GL_TEXTURE0 + texUnit);
		glBindTexture(texType, tex);
		glBindSampler(texUnit, sampler);
	}
	
	@Override
	public void unbindState(int program) {
		glActiveTexture(GL_TEXTURE0 + texUnit);
		glBindTexture(texType, 0);
		glBindSampler(texUnit, 0);
	}
}
