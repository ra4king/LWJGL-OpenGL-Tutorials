package com.ra4king.opengl.util.interpolators;

import java.util.ArrayList;

import com.ra4king.opengl.util.math.Vector;

public class LinearInterpolatorVector<T extends Vector<T>> extends WeightedLinearInterpolatorVector<T> {
	public void setValues(ArrayList<T> data) {
		setValues(data, true);
	}

	public void setValues(ArrayList<T> data, boolean isLooping) {
		values.clear();

		for(T d : data)
			values.add(new Data(d, 0));

		if(isLooping && !values.isEmpty())
			values.add(values.get(values.size() - 1));

		for(int a = 0; a < values.size(); a++)
			values.get(a).weight = a / (float)(values.size() - 1);
	}
}
