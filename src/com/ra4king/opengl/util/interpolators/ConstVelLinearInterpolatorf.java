package com.ra4king.opengl.util.interpolators;

import java.util.ArrayList;

public class ConstVelLinearInterpolatorf extends WeightedLinearInterpolatorf {
	private float totalDist;
	
	public float getDistance() {
		return totalDist;
	}
	
	public void setValues(ArrayList<Float> data) {
		setValues(data, true);
	}
	
	public void setValues(ArrayList<Float> data, boolean isLooping) {
		values.clear();
		
		for(Float d : data)
			values.add(new Data(d, 0));
		
		if(isLooping)
			values.add(new Data(values.get(0)));
		
		totalDist = 0;
		for(int a = 1; a < values.size(); a++) {
			totalDist += Math.abs(values.get(a).data - values.get(a - 1).data);
			values.get(a).weight = totalDist;
		}
		
		for(Data d : values)
			d.weight /= totalDist;
	}
}
