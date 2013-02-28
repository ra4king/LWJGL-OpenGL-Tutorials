package com.ra4king.opengl.util.interpolators;

import java.util.ArrayList;

public class WeightedLinearInterpolatorf {
	public class Data {
		public float data;
		public float weight;
		
		public Data(float data, float weight) {
			this.data = data;
			this.weight = weight;
		}
		
		public Data(Data other) {
			data = other.data;
			weight = other.weight;
		}
	}
	
	protected ArrayList<Data> values = new ArrayList<>();
	
	public int numSegments() {
		return values.isEmpty() ? 0 : values.size() - 1;
	}
	
	public float interpolate(float alpha) {
		if(values.isEmpty())
			return 0;
		if(values.size() == 1)
			return values.get(0).data;
		
		int segment = 1;
		for(; segment < values.size(); segment++)
			if(alpha < values.get(segment).weight)
				break;
		
		if(segment == values.size())
			return values.get(values.size() - 1).data;
		
		float sectionAlpha = alpha - values.get(segment - 1).weight;
		sectionAlpha /= values.get(segment).weight - values.get(segment - 1).weight;
		
		float invSecAlpha = 1 - sectionAlpha;
		
		return values.get(segment - 1).data * invSecAlpha + values.get(segment).data * sectionAlpha;
	}
}
