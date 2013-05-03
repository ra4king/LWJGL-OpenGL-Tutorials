package com.ra4king.opengl.util.math;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Vector2 implements Vector<Vector2> {
	private float x, y;
	
	public Vector2() {
		set(0, 0);
	}
	
	public Vector2(float v) {
		this(v, v);
	}
	
	public Vector2(float x, float y) {
		set(x, y);
	}
	
	public Vector2(Vector2 vec) {
		set(vec);
	}
	
	public Vector2(Vector3 vec) {
		set(vec);
	}
	
	public Vector2(Vector4 vec) {
		set(vec);
	}
	
	@Override
	public Vector2 copy() {
		return new Vector2(this);
	}
	
	public float x() {
		return x;
	}
	
	public Vector2 x(float x) {
		this.x = x;
		return this;
	}
	
	public float y() {
		return y;
	}
	
	public Vector2 y(float y) {
		this.y = y;
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Vector2) {
			Vector2 v = (Vector2)o;
			return x == v.x && y == v.y;
		}
		
		return false;
	}
	
	public Vector2 set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vector2 set(Vector2 vec) {
		return set(vec.x, vec.y);
	}
	
	public Vector2 set(Vector3 vec) {
		return set(vec.x(), vec.y());
	}
	
	public Vector2 set(Vector4 vec) {
		return set(vec.x(), vec.y());
	}
	
	public Vector2 reset() {
		x = y = 0;
		return this;
	}
	
	@Override
	public float length() {
		return (float)Math.sqrt(x * x + y * y);
	}
	
	public Vector2 normalize() {
		float length = length();
		x /= length;
		y /= length;
		return this;
	}
	
	public float dot(Vector2 vec) {
		return x * vec.x + y * vec.y;
	}
	
	public Vector2 add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	@Override
	public Vector2 add(Vector2 vec) {
		return add(vec.x, vec.y);
	}
	
	public Vector2 sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	@Override
	public Vector2 sub(Vector2 vec) {
		return sub(vec.x, vec.y);
	}
	
	@Override
	public Vector2 mult(float f) {
		return mult(f, f);
	}
	
	public Vector2 mult(float x, float y) {
		this.x *= x;
		this.y *= y;
		return this;
	}
	
	@Override
	public Vector2 mult(Vector2 vec) {
		return mult(vec.x, vec.y);
	}
	
	@Override
	public Vector2 divide(float f) {
		return divide(f, f);
	}
	
	public Vector2 divide(float x, float y) {
		this.x /= x;
		this.y /= y;
		return this;
	}
	
	@Override
	public Vector2 divide(Vector2 vec) {
		return divide(vec.x, vec.y);
	}
	
	private final static FloatBuffer direct = BufferUtils.createFloatBuffer(2);
	
	@Override
	public FloatBuffer toBuffer() {
		direct.clear();
		direct.put(x).put(y);
		direct.flip();
		return direct;
	}
}
