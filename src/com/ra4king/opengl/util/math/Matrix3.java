package com.ra4king.opengl.util.math;

import java.nio.FloatBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;

@SuppressWarnings({ "UnusedReturnValue", "PointlessArithmeticExpression" })
public class Matrix3 {
	private float[] matrix;
	
	public Matrix3() {
		matrix = new float[9];
	}
	
	public Matrix3(float[] m) {
		this();
		put(m);
	}
	
	public Matrix3(Matrix3 m) {
		this();
		put(m);
	}
	
	public Matrix3(Matrix4 m) {
		this();
		put(m);
	}
	
	public Matrix3 clear() {
		Arrays.fill(matrix, 0);
		return this;
	}
	
	public Matrix3 clearToIdentity() {
		return clear().put(0, 1).put(4, 1).put(8, 1);
	}
	
	public float get(int index) {
		return matrix[index];
	}
	
	public Matrix3 put(int index, float f) {
		matrix[index] = f;
		return this;
	}
	
	public Matrix3 putColumn(int index, Vector3 v) {
		put(index * 3 + 0, v.x());
		put(index * 3 + 1, v.y());
		put(index * 3 + 2, v.z());
		return this;
	}
	
	public Matrix3 put(float[] m) {
		if(m.length < matrix.length)
			throw new IllegalArgumentException("float array must have at least " + matrix.length + " values.");
		
		System.arraycopy(m, 0, matrix, 0, matrix.length);
		
		return this;
	}
	
	public Matrix3 put(Matrix3 m) {
		return put(m.matrix);
	}
	
	public Matrix3 put(Matrix4 m) {
		for(int a = 0; a < 3; a++) {
			put(a * 3 + 0, m.get(a * 4 + 0));
			put(a * 3 + 1, m.get(a * 4 + 1));
			put(a * 3 + 2, m.get(a * 4 + 2));
		}
		
		return this;
	}
	
	public Matrix3 mult(float f) {
		for(int a = 0; a < matrix.length; a++)
			put(a, get(a) * f);
		
		return this;
	}
	
	public Matrix3 mult(float[] m) {
		float[] newm = new float[matrix.length];
		
		for(int a = 0; a < matrix.length; a += 3) {
			newm[a + 0] = get(0) * m[a] + get(3) * m[a + 1] + get(6) * m[a + 2];
			newm[a + 1] = get(1) * m[a] + get(4) * m[a + 1] + get(7) * m[a + 2];
			newm[a + 2] = get(2) * m[a] + get(5) * m[a + 1] + get(8) * m[a + 2];
		}
		
		put(newm);
		
		return this;
	}
	
	public Matrix3 mult(Matrix3 m) {
		return mult(m.matrix);
	}
	
	public Vector3 mult(Vector3 vec) {
		Vector3 v = new Vector3();
		
		v.x(get(0) * vec.x() + get(3) * vec.y() + get(6) * vec.z());
		v.x(get(1) * vec.x() + get(4) * vec.y() + get(7) * vec.z());
		v.x(get(2) * vec.x() + get(5) * vec.y() + get(8) * vec.z());
		
		return v;
	}
	
	public Matrix3 transpose() {
		float old = get(1);
		put(1, get(3));
		put(3, old);
		
		old = get(2);
		put(2, get(6));
		put(6, old);
		
		old = get(5);
		put(5, get(7));
		put(7, old);
		
		return this;
	}
	
	public float determinant() {
		return +get(0) * get(4) * get(8) + get(3) * get(7) * get(2) + get(6) * get(1) * get(5)
				- get(2) * get(4) * get(6) - get(5) * get(7) * get(0) - get(8) * get(1) * get(3);
	}
	
	public Matrix3 inverse() {
		Matrix3 inv = new Matrix3();
		
		inv.put(0, +(get(4) * get(8) - get(5) * get(7)));
		inv.put(1, -(get(3) * get(8) - get(5) * get(6)));
		inv.put(2, +(get(3) * get(7) - get(4) * get(6)));
		
		inv.put(3, -(get(1) * get(8) - get(2) * get(7)));
		inv.put(4, +(get(0) * get(8) - get(2) * get(6)));
		inv.put(5, -(get(0) * get(7) - get(1) * get(6)));
		
		inv.put(6, +(get(1) * get(5) - get(2) * get(4)));
		inv.put(7, -(get(0) * get(5) - get(2) * get(3)));
		inv.put(8, +(get(0) * get(4) - get(1) * get(3)));
		
		return put(inv.transpose().mult(1 / determinant()));
	}
	
	private final static FloatBuffer direct = BufferUtils.createFloatBuffer(9);
	
	public FloatBuffer toBuffer() {
		direct.clear();
		direct.put(matrix);
		direct.flip();
		return direct;
	}
}
