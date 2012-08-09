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
	
	public void pushMatrix() {
		stack.push(current);
		current = new Matrix4(current);
	}
	
	public void popMatrix() {
		current = stack.pop();
	}
}
