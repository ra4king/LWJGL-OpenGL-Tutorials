package com.ra4king.opengl.util.math;

import java.util.Stack;

public class MatrixStack {
	private Stack<Matrix4> stack;
	private Matrix4 current;
	
	public MatrixStack() {
		current = new Matrix4().clearToIdentity();
		stack = new Stack<Matrix4>();
	}
	
	public Matrix4 getTop() {
		return current;
	}
	
	public void rotateX(float angle) {
		current.rotate(angle * (float)Math.PI/180, 1, 0, 0);
	}
	
	public void rotateY(float angle) {
		current.rotate(angle * (float)Math.PI/180, 0, 1, 0);
	}
	
	public void rotateZ(float angle) {
		current.rotate(angle * (float)Math.PI/180, 0, 0, 1);
	}
	
	public void scale(Vector3 vec) {
		current.scale(vec);
	}
	
	public void translate(Vector3 vec) {
		current.translate(vec);
	}
	
	public void pushMatrix() {
		stack.push(current);
		current = new Matrix4(current);
	}
	
	public void popMatrix() {
		current = stack.pop();
	}
}