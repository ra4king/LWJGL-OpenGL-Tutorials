package com.ra4king.opengl.util.interpolators;

import java.util.ArrayList;

public class TimedLinearInterpolatorf extends WeightedLinearInterpolatorf {
	public void setValues(ArrayList<Data> data) {
		setValues(data, true);
	}
	
	public void setValues(ArrayList<Data> data, boolean isLooping) {
		values.clear();
		for(Data d : data) {
			if(d.weight < 0 || d.weight > 1)
				throw new IllegalArgumentException("weight is out of bounds.");
			values.add(new Data(d));
		}
		
		if(isLooping && !values.isEmpty())
			values.add(new Data(values.get(0)));
		
		if(!values.isEmpty()) {
			values.get(0).weight = 0;
			values.get(values.size() - 1).weight = 1;
		}
	}
}
