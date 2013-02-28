package com.ra4king.opengl.util.interpolators;

import java.util.ArrayList;

import com.ra4king.opengl.util.math.Vector;

public class WeightedLinearInterpolatorVector<T extends Vector<T>> {
	public class Data {
		public T data;
		public float weight;
		
		public Data(T data, float weight) {
			this.data = data;
			this.weight = weight;
		}
		
		public Data(Data other) {
			data = other.data.copy();
			weight = other.weight;
		}
	}
	
	protected ArrayList<Data> values = new ArrayList<>();
	
	public int numSegments() {
		return values.isEmpty() ? 0 : values.size() - 1;
	}
	
	public T interpolate(float alpha) {
		if(values.isEmpty())
			return null;
		if(values.size() == 1)
			return values.get(0).data.copy();
		
		int segment = 1;
		for(; segment < values.size(); segment++)
			if(alpha < values.get(segment).weight)
				break;
		
		if(segment == values.size())
			return values.get(values.size() - 1).data.copy();
		
		float sectionAlpha = alpha - values.get(segment - 1).weight;
		sectionAlpha /= values.get(segment).weight - values.get(segment - 1).weight;
		
		float invSecAlpha = 1 - sectionAlpha;
		
		return values.get(segment - 1).data.copy().mult(invSecAlpha).add(values.get(segment).data.copy().mult(sectionAlpha));
	}
}
