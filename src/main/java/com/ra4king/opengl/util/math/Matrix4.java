package com.ra4king.opengl.util.math;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Matrix4 {
	private FloatBuffer matrix;
	
	public Matrix4() {
		matrix = BufferUtils.createFloatBuffer(16);
	}
	
	public Matrix4(float[] m) {
		this();
		put(m);
	}
	
	public Matrix4 clear() {
		matrix.clear();
		return this;
	}
	
	public Matrix4 clearToIdentity() {
		return clear().put(0,1).put(5,1).put(10,1).put(15,1);
	}
	
	public float get(int index) {
		return matrix.get(index);
	}
	
	public Matrix4 put(int index, float f) {
		matrix.put(index, f);
		return this;
	}
	
	public Matrix4 put(float[] m) {
		if(m.length < 16)
			throw new IllegalArgumentException("float array must have at least 16 values.");
		
		matrix.position(0);
		matrix.put(m,0,16).flip();
		
		return this;
	}
	
	public Matrix4 mult(Matrix4 m) {
		Matrix4 n = new Matrix4();
		n.clear();
		
		for(int a = 0; a < 16; a += 4) {
			n.put(a+0, get(0)*m.get(a) + get(4)*m.get(a+1) + get(8)*m.get(a+2) + get(12)*m.get(a+3));
			n.put(a+1, get(1)*m.get(a) + get(5)*m.get(a+1) + get(9)*m.get(a+2) + get(13)*m.get(a+3));
			n.put(a+2, get(2)*m.get(a) + get(6)*m.get(a+1) + get(10)*m.get(a+2) + get(14)*m.get(a+3));
			n.put(a+3, get(3)*m.get(a) + get(7)*m.get(a+1) + get(11)*m.get(a+2) + get(15)*m.get(a+3));
		}
		
		return n;
	}
	
	public FloatBuffer getBuffer() {
		return (FloatBuffer)matrix.limit(16).position(0);
	}
}
