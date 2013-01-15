package com.ra4king.opengl.arcsynthesis.gl33.chapter12;

import com.ra4king.opengl.util.math.Vector4;

public class LightManager {
	public final int NUMBER_OF_LIGHTS = 4;
	public final int NUMBER_OF_POINT_LIGHTS = NUMBER_OF_LIGHTS - 1;
	
	private class PerLight {
		private Vector4 cameraSpaceLightPos;
		private Vector4 lightIntensity;
	}
	
	private class LightBlock {
		private Vector4 ambientIntensity;
		private float lightAttenuation;
		private float[] padding = new float[3];
		private PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
	}
	
	private class LightBlockHDR {
		private Vector4 ambientIntensity;
		private float lightAttenuation;
		private float maxIntensity;
		private float[] padding = new float[2];
		private PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
	}
	
	private class LightBlockGamma {
		private Vector4 ambientIntensity;
		private float lightAttenuation;
		private float maxIntensity;
		private float gamma;
		private float padding;
		private PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
	}
	
	private class SunlightValue {
		private float normTime;
		private Vector4 ambient;
		private Vector4 sunlightIntensity;
		private Vector4 backgroundColor;
	}
	
	private class SunlightValueHDR {
		private float normTime;
		private Vector4 ambient;
		private Vector4 sunlightIntensity;
		private Vector4 backgroundColor;
		private float maxIntensity;
	}
	
	private enum TimerTypes {
		TIMER_SUN, TIMER_LIGHTS, TIMER_ALL
	}
}
