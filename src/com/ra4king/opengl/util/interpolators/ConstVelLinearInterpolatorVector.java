package com.ra4king.opengl.util.interpolators;

import java.util.ArrayList;

import com.ra4king.opengl.util.math.Vector;

public class ConstVelLinearInterpolatorVector<T extends Vector<T>> extends WeightedLinearInterpolatorVector<T> {
	private float totalDist;
	
	public float getDistance() {
		return totalDist;
	}
	
	public void setValues(ArrayList<T> data) {
		setValues(data, true);
	}
	
	public void setValues(ArrayList<T> data, boolean isLooping) {
		values.clear();
		
		for(T d : data)
			values.add(new Data(d.copy(), 0));
		
		if(isLooping)
			values.add(new Data(values.get(0)));
		
		totalDist = 0;
		for(int a = 1; a < values.size(); a++) {
			totalDist += values.get(a).data.copy().sub(values.get(a - 1).data).length();
			values.get(a).weight = totalDist;
		}
		
		for(Data d : values)
			d.weight /= totalDist;
	}
}
