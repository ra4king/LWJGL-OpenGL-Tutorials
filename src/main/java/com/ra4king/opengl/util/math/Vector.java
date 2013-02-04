package com.ra4king.opengl.util.math;

import java.nio.FloatBuffer;

public interface Vector<V> {
	V add(V v);
	V sub(V v);
	V mult(V v);
	V mult(float f);
	float length();
	V copy();
	FloatBuffer toBuffer();
}
