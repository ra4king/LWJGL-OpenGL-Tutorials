package com.ra4king.opengl.util.interpolators;

import java.util.ArrayList;

public class LinearInterpolatorf extends WeightedLinearInterpolatorf {
	public void setValues(ArrayList<Float> data) {
		setValues(data, true);
	}
	
	public void setValues(ArrayList<Float> data, boolean isLooping) {
		values.clear();
		
		for(Float d : data)
			values.add(new Data(d, 0));
		
		if(isLooping && !values.isEmpty())
			values.add(new Data(values.get(values.size() - 1)));
		
		for(int a = 0; a < values.size(); a++)
			values.get(a).weight = a / (float)(values.size() - 1);
	}
}
