package com.ra4king.opengl.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

import java.util.Map;

public class ShaderProgram {
	private int program;
	
	public ShaderProgram(String vertexShader, String fragmentShader) {
		this(vertexShader, null, fragmentShader, null);
	}
	
	public ShaderProgram(String vertexShader, String fragmentShader, Map<Integer,String> attributes) {
		this(vertexShader, null, fragmentShader, attributes);
	}
	
	public ShaderProgram(String vertexShader, String geometryShader, String fragmentShader) {
		this(vertexShader, geometryShader, fragmentShader, null);
	}
	
	public ShaderProgram(String vertexShader, String geometryShader, String fragmentShader, Map<Integer,String> attributes) {
		int vs = compileShader(vertexShader, GL_VERTEX_SHADER);
		int gs = compileShader(geometryShader, GL_GEOMETRY_SHADER);
		int fs = compileShader(fragmentShader, GL_FRAGMENT_SHADER);
		
		program = glCreateProgram();
		glAttachShader(program, vs);
		if(gs != -1)
			glAttachShader(program, gs);
		glAttachShader(program, fs);
		
		if(attributes != null)
			for(int i : attributes.keySet())
				glBindAttribLocation(program, i, attributes.get(i));
		
		glLinkProgram(program);
		
		String infoLog = glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));
		
		if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in linking program. Error log:\n" + infoLog);
		else {
			System.out.print("Linking program successful.");
			if(infoLog != null && !(infoLog = infoLog.trim()).isEmpty())
				System.out.println(" Log:\n" + infoLog);
			else
				System.out.println();
		}
		
		glDetachShader(program, vs);
		if(gs != -1)
			glDetachShader(program, gs);
		glDetachShader(program, fs);
		
		glDeleteShader(vs);
		if(gs != -1)
			glDeleteShader(gs);
		glDeleteShader(fs);
	}
	
	private int compileShader(String source, int type) {
		if(source == null)
			return -1;
		
		int shader = glCreateShader(type);
		glShaderSource(shader, source);
		
		glCompileShader(shader);
		
		String infoLog = glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
		
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in compiling " + getName(type) + " shader. Error log:\n" + infoLog);
		else {
			System.out.print("Compiling " + getName(type) + " shader successful.");
			if(infoLog != null && !(infoLog = infoLog.trim()).isEmpty())
				System.out.println(" Log:\n" + infoLog);
			else
				System.out.println();
		}
		
		return shader;
	}
	
	private String getName(int shaderType) {
		if(shaderType == GL_VERTEX_SHADER)
			return "vertex";
		if(shaderType == GL_GEOMETRY_SHADER)
			return "geometry";
		if(shaderType == GL_FRAGMENT_SHADER)
			return "fragment";
		
		throw new IllegalArgumentException("Invalid shaderType, must be either GL_VERTEX_SHADER, GL_GEOMETRY_SHADER, or GL_FRAGMENT_SHADER");
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
