package com.ra4king.opengl.util.scene.binders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL33.*;

import com.ra4king.opengl.util.ShaderProgram;

public class TextureBinder implements StateBinder {
	public int texUnit;
	public int texType = GL_TEXTURE_2D;
	public int tex;
	public int sampler;
	
	public TextureBinder() {}
	
	public TextureBinder(int texUnit, int texType, int tex, int sampler) {
		setValue(texUnit, texType, tex, sampler);
	}
	
	public void setValue(int texUnit, int texType, int tex, int sampler) {
		this.texUnit = texUnit;
		this.texType = texType;
		this.tex = tex;
		this.sampler = sampler;
	}
	
	@Override
	public void bindState(ShaderProgram program) {
		glActiveTexture(GL_TEXTURE0 + texUnit);
		glBindTexture(texType, tex);
		glBindSampler(texUnit, sampler);
	}
	
	@Override
	public void unbindState(ShaderProgram program) {
		glActiveTexture(GL_TEXTURE0 + texUnit);
		glBindTexture(texType, 0);
		glBindSampler(texUnit, 0);
	}
}
