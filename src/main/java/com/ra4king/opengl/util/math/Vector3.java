package com.ra4king.opengl.util.math;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Vector3 {
	private float x, y, z;
	
	public Vector3() {
		set(0,0,0);
	}
	
	public Vector3(float x, float y, float z) {
		set(x,y,z);
	}
	
	public Vector3(Vector3 vec) {
		set(vec);
	}
	
	public float x() {
		return x;
	}
	
	public Vector3 x(float x) {
		this.x = x;
		return this;
	}
	
	public float y() {
		return y;
	}
	
	public Vector3 y(float y) {
		this.y = y;
		return this;
	}
	
	public float z() {
		return z;
	}
	
	public Vector3 z(float z) {
		this.z = z;
		return this;
	}
	
	public Vector3 set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vector3 set(Vector3 vec) {
		return set(vec.x,vec.y,vec.z);
	}
	
	public Vector3 reset() {
		x = y = z = 0;
		return this;
	}
	
	public float length() {
		return (float)Math.sqrt(x*x + y*y + z*z);
	}
	
	public Vector3 normalize() {
		float length = length();
		x /= length;
		y /= length;
		z /= length;
		return this;
	}
	
	public float dot(Vector3 vec) {
		return x*vec.x + y*vec.y + z*vec.z;
	}
	
	public Vector3 cross(Vector3 vec) {
		return new Vector3(y*vec.z - vec.y*z, z*vec.x - vec.z*x, x*vec.y - vec.x*y);
	}
	
	public Vector3 add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vector3 add(Vector3 vec) {
		return add(vec.x,vec.y,vec.z);
	}
	
	public Vector3 sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public Vector3 sub(Vector3 vec) {
		return sub(vec.x,vec.y,vec.z);
	}
	
	public Vector3 mult(float f) {
		return mult(f,f,f);
	}
	
	public Vector3 mult(float x, float y, float z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}
	
	public Vector3 mult(Vector3 vec) {
		return mult(vec.x,vec.y,vec.z);
	}
	
	private final static FloatBuffer direct = BufferUtils.createFloatBuffer(3);
	
	public FloatBuffer toBuffer() {
		direct.clear();
		direct.put(x).put(y).put(z);
		direct.flip();
		return direct;
	}
}
