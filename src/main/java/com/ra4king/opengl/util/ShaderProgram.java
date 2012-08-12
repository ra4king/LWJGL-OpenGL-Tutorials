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
		
		String infoLog = glGetShaderInfoLog(vs, glGetShader(vs, GL_INFO_LOG_LENGTH));
		
		if(glGetShader(vs, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in compiling vertex shader. Error log:\n" + infoLog);
		else {
			System.out.print("Compiling vertex shader successfull.");
			if(infoLog != null)
				System.out.println(" Log: " + infoLog);
			else
				System.out.println();
		}
		
		int fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, fragmentShader);
		
		glCompileShader(fs);
		
		infoLog = glGetShaderInfoLog(fs, glGetShader(fs, GL_INFO_LOG_LENGTH));
		
		if(glGetShader(fs, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in compiling fragment shader. Error log:\n" + infoLog);
		else {
			System.out.print("Compiling fragment shader successfull.");
			if(infoLog != null)
				System.out.println(" Log: " + infoLog);
			else
				System.out.println();
		}
		
		program = glCreateProgram();
		glAttachShader(program, vs);
		glAttachShader(program, fs);
		
		if(attributes != null)
			for(Integer i : attributes.keySet())
				glBindAttribLocation(program, i, attributes.get(i));
		
		glLinkProgram(program);
		
		infoLog = glGetProgramInfoLog(program, glGetProgram(program, GL_INFO_LOG_LENGTH));
		
		if(glGetProgram(program, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Failure in linking program. Error log:\n" + infoLog);
		else {
			System.out.print("Linking program successfull.");
			if(infoLog != null)
				System.out.println(" Log: " + infoLog);
			else
				System.out.println();
		}
		
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
