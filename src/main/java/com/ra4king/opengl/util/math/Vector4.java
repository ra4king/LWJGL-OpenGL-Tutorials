package com.ra4king.opengl.util.math;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Vector4 {
	private float x, y, z, w;
	
	public Vector4() {
		set(0, 0, 0, 0);
	}
	
	public Vector4(float v) {
		this(v, v, v, v);
	}
	
	public Vector4(float x, float y, float z, float w) {
		set(x, y, z, w);
	}
	
	public Vector4(Vector4 vec) {
		set(vec);
	}
	
	public Vector4(Vector3 vec) {
		set(vec);
	}
	
	public Vector4(Vector3 vec, float w) {
		set(vec, w);
	}
	
	public float x() {
		return x;
	}
	
	public Vector4 x(float x) {
		this.x = x;
		return this;
	}
	
	public float y() {
		return y;
	}
	
	public Vector4 y(float y) {
		this.y = y;
		return this;
	}
	
	public float z() {
		return z;
	}
	
	public Vector4 z(float z) {
		this.z = z;
		return this;
	}
	
	public float w() {
		return w;
	}
	
	public Vector4 w(float w) {
		this.w = w;
		return this;
	}
	
	public Vector4 set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	public Vector4 set(Vector4 vec) {
		return set(vec.x, vec.y, vec.z, vec.w);
	}
	
	public Vector4 set(Vector3 vec) {
		return set(vec, 0);
	}
	
	public Vector4 set(Vector3 vec, float w) {
		return set(vec.x(), vec.y(), vec.z(), w);
	}
	
	public Vector4 reset() {
		x = y = z = w = 0;
		return this;
	}
	
	public float length() {
		return (float)Math.sqrt(x * x + y * y + z * z + w * w);
	}
	
	public Vector4 normalize() {
		float length = length();
		x /= length;
		y /= length;
		z /= length;
		w /= length;
		return this;
	}
	
	public float dot(Vector4 vec) {
		return x * vec.x + y * vec.y + z * vec.z + w * vec.w;
	}
	
	public Vector4 add(float x, float y, float z, float w) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.w += w;
		return this;
	}
	
	public Vector4 add(Vector4 vec) {
		return add(vec.x, vec.y, vec.z, vec.w);
	}
	
	public Vector4 sub(float x, float y, float z, float w) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		this.w -= w;
		return this;
	}
	
	public Vector4 sub(Vector4 vec) {
		return sub(vec.x, vec.y, vec.z, vec.w);
	}
	
	public Vector4 mult(float f) {
		return mult(f, f, f, f);
	}
	
	public Vector4 mult(float x, float y, float z, float w) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		this.w *= w;
		return this;
	}
	
	public Vector4 mult(Vector4 vec) {
		return mult(vec.x, vec.y, vec.z, vec.w);
	}
	
	private final static FloatBuffer direct = BufferUtils.createFloatBuffer(4);
	
	public FloatBuffer toBuffer() {
		direct.clear();
		direct.put(x).put(y).put(z).put(w);
		direct.flip();
		return direct;
	}
}
