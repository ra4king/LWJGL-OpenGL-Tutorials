package com.ra4king.opengl.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.util.Map;

public class ShaderProgram {
	private int program;
	
	public ShaderProgram(String vertexShader, String fragmentShader) {
		this(vertexShader,fragmentShader,null);
	}
	
	public ShaderProgram(String vertexShader, String fragmentShader, Map<Integer,String> attributes) {
		int vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, vertexShader);
		
		glCompileShader(vs);
		
		if(glGetShader(vs, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in compiling vertex shader. Error log:\n" + glGetShaderInfoLog(vs, glGetShader(vs, GL_INFO_LOG_LENGTH)));
		
		int fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, fragmentShader);
		
		glCompileShader(fs);
		
		if(glGetShader(fs, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in compiling fragment shader. Error log:\n" + glGetShaderInfoLog(fs, glGetShader(fs, GL_INFO_LOG_LENGTH)));
		
		program = glCreateProgram();
		glAttachShader(program, vs);
		glAttachShader(program, fs);
		
		if(attributes != null)
			for(Integer i : attributes.keySet())
				glBindAttribLocation(program, i, attributes.get(i));
		
		glLinkProgram(program);
		
		if(glGetProgram(program, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in linking program. Error log:\n" + glGetProgramInfoLog(program, glGetProgram(program, GL_INFO_LOG_LENGTH)));
		
		glDetachShader(program, vs);
		glDetachShader(program, fs);
		
		glDeleteShader(vs);
		glDeleteShader(fs);
	}
	
	public int getProgram() {
		return program;
	}
	
	public void begin() {
		glUseProgram(program);
	}
	
	public void end() {
		glUseProgram(0);
	}
	
	public void destroy() {
		glDeleteProgram(program);
	}
}
