package com.ra4king.opengl.arcsynthesis.gl33.chapter12;

import java.util.ArrayList;
import java.util.HashMap;

import javax.security.auth.x500.X500Principal;

import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.interpolators.ConstVelLinearInterpolatorVector;
import com.ra4king.opengl.util.interpolators.TimedLinearInterpolatorVector;
import com.ra4king.opengl.util.interpolators.TimedLinearInterpolatorf;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

public class LightManager {
	public final int NUMBER_OF_LIGHTS = 4;
	public final int NUMBER_OF_POINT_LIGHTS = NUMBER_OF_LIGHTS - 1;
	
	private Timer sunTimer;
	private TimedLinearInterpolatorVector<Vector4> ambientInterpolator;
	private TimedLinearInterpolatorVector<Vector4> backgroundInterpolator;
	private TimedLinearInterpolatorVector<Vector4> sunlightInterpolator;
	private TimedLinearInterpolatorf maxIntensityInterpolator;
	
	private ArrayList<ConstVelLinearInterpolatorVector<Vector3>> lightPos;
	private ArrayList<Vector4> lightIntensity;
	private ArrayList<Timer> lightTimers;
	private HashMap<String,Timer> extraTimers;
	
	private final float halfLightDistance = 70;
	private float lightAttenuation = 1f / (halfLightDistance * halfLightDistance);
	
	private float lightHeight = 10.5f;
	private float lightRadius = 70;
	
	public LightManager() {
		sunTimer = new Timer(Type.LOOP, 30);
		ambientInterpolator = new TimedLinearInterpolatorVector<>();
		backgroundInterpolator = new TimedLinearInterpolatorVector<>();
		sunlightInterpolator = new TimedLinearInterpolatorVector<>();
		maxIntensityInterpolator = new TimedLinearInterpolatorf();
		
		lightTimers = new ArrayList<>();
		
		lightPos = new ArrayList<>();
		for(int a = 0; a < NUMBER_OF_POINT_LIGHTS; a++)
			lightPos.add(new ConstVelLinearInterpolatorVector<Vector3>());
		
		lightIntensity = new ArrayList<>();
		for(int a = 0; a < NUMBER_OF_POINT_LIGHTS; a++)
			lightIntensity.add(new Vector4(0.2f, 0.2f, 0.2f, 1));
		
		ArrayList<Vector3> posValues = new ArrayList<>();
		posValues.add(new Vector3(-50, 30, 70));
		posValues.add(new Vector3(-70, 30, 50));
		posValues.add(new Vector3(-70, 30, -50));
		posValues.add(new Vector3(-50, 30, -70));
		posValues.add(new Vector3(50, 30, -70));
		posValues.add(new Vector3(70, 30, -50));
		posValues.add(new Vector3(70, 30, 50));
		posValues.add(new Vector3(50, 30, 70));
		lightPos.get(0).setValues(posValues);
		lightTimers.add(new Timer(Timer.Type.LOOP, 15));
		
		posValues.clear();
		posValues.add(new Vector3(100, 6, 75));
		posValues.add(new Vector3(90, 8, 90));
		posValues.add(new Vector3(75, 10, 100));
		posValues.add(new Vector3(60, 12, 90));
		posValues.add(new Vector3(50, 14, 75));
		posValues.add(new Vector3(60, 16, 60));
		posValues.add(new Vector3(75, 18, 50));
		posValues.add(new Vector3(90, 20, 60));
		posValues.add(new Vector3(100, 22, 75));
		posValues.add(new Vector3(90, 24, 90));
		posValues.add(new Vector3(75, 26, 100));
		posValues.add(new Vector3(60, 28, 90));
		posValues.add(new Vector3(50, 30, 75));
		
		posValues.add(new Vector3(105, 9, -70));
		posValues.add(new Vector3(105, 10, -90));
		posValues.add(new Vector3(72, 20, -90));
		posValues.add(new Vector3(72, 22, -70));
		posValues.add(new Vector3(105, 32, -70));
		posValues.add(new Vector3(105, 34, -90));
		posValues.add(new Vector3(72, 44, -90));
		
		lightPos.get(1).setValues(posValues);
		lightTimers.add(new Timer(Timer.Type.LOOP, 25));
		
		posValues.clear();
		posValues.add(new Vector3(-7, 35, 1));
		posValues.add(new Vector3(8, 40, -14));
		posValues.add(new Vector3(-7, 45, -29));
		posValues.add(new Vector3(-22, 50, -14));
		posValues.add(new Vector3(-7, 55, 1));
		posValues.add(new Vector3(8, 60, -14));
		posValues.add(new Vector3(-7, 65, -29));
		
		posValues.add(new Vector3(-83, 30, -92));
		posValues.add(new Vector3(-98, 27, -77));
		posValues.add(new Vector3(-83, 24, -62));
		posValues.add(new Vector3(-68, 21, -77));
		posValues.add(new Vector3(-83, 18, -92));
		posValues.add(new Vector3(-98, 15, -77));
		
		posValues.add(new Vector3(-50, 8, 25));
		posValues.add(new Vector3(-59.5f, 4, 65));
		posValues.add(new Vector3(-59.5f, 4, 78));
		posValues.add(new Vector3(-45, 4, 82));
		posValues.add(new Vector3(-40, 4, 50));
		posValues.add(new Vector3(-70, 20, 40));
		posValues.add(new Vector3(-60, 20, 90));
		posValues.add(new Vector3(-40, 25, 90));
		
		lightPos.get(2).setValues(posValues);
		lightTimers.add(new Timer(Timer.Type.LOOP, 15));
	}
	
	public Vector4 calcLightPosition(Timer timer, float alphaOffset) {
		float currTime = timer.getAlpha() + alphaOffset;
		
		Vector4 ret = new Vector4(0, lightHeight, 0, 1);
		ret.x((float)Math.cos(currTime * 2 * Math.PI) * lightRadius);
		ret.z((float)Math.sin(currTime * 2 * Math.PI) * lightRadius);
		
		return ret;
	}
	
	public Vector4 getMaxIntensityValue(Pair<Vector4, Float> data) {
		return data.first;
	}
	
	public float getMaxIntensityTime(Pair<Vector4, Float> data) {
		return data.second;
	}
	
	public float getLightVectorValue(Pair<Float, Float> data) {
		return data.first;
	}
	
	public float getLightVectorTime(Pair<Float, Float> data) {
		return data.second;
	}
	
	public void setSunlightValues(SunlightValue[] values) {
		ArrayList<TimedLinearInterpolatorVector<Vector4>.Data> ambient = new ArrayList<>();
		ArrayList<TimedLinearInterpolatorVector<Vector4>.Data> light = new ArrayList<>();
		ArrayList<TimedLinearInterpolatorVector<Vector4>.Data> background = new ArrayList<>();
		
		for(SunlightValue v : values) {
			ambient.add(ambientInterpolator.new Data(v.ambient, v.normTime));
			light.add(sunlightInterpolator.new Data(v.sunlightIntensity, v.normTime));
			background.add(backgroundInterpolator.new Data(v.backgroundColor, v.normTime));
		}
		
		ambientInterpolator.setValues(ambient);
		sunlightInterpolator.setValues(light);
		backgroundInterpolator.setValues(background);
		
		ArrayList<TimedLinearInterpolatorf.Data> maxIntensity = new ArrayList<>();
		maxIntensity.add(maxIntensityInterpolator.new Data(1, 0));
		maxIntensityInterpolator.setValues(maxIntensity, false);
	}
	
	public void setSunlightValues(SunlightValueHDR[] values) {
		ArrayList<TimedLinearInterpolatorVector<Vector4>.Data> ambient = new ArrayList<>();
		ArrayList<TimedLinearInterpolatorVector<Vector4>.Data> light = new ArrayList<>();
		ArrayList<TimedLinearInterpolatorVector<Vector4>.Data> background = new ArrayList<>();
		ArrayList<TimedLinearInterpolatorf.Data> maxIntensity = new ArrayList<>();
		
		for(SunlightValueHDR v : values) {
			ambient.add(ambientInterpolator.new Data(v.ambient, v.normTime));
			light.add(sunlightInterpolator.new Data(v.sunlightIntensity, v.normTime));
			background.add(backgroundInterpolator.new Data(v.backgroundColor, v.normTime));
			maxIntensity.add(maxIntensityInterpolator.new Data(v.maxIntensity, v.normTime));
		}
		
		ambientInterpolator.setValues(ambient);
		sunlightInterpolator.setValues(light);
		backgroundInterpolator.setValues(background);
		maxIntensityInterpolator.setValues(maxIntensity);
	}
	
	
	
	private static class Pair<F, S> {
		private F first;
		private S second;
		
		public Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}
	}
	
	private class PerLight {
		private Vector4 cameraSpaceLightPos;
		private Vector4 lightIntensity;
		
		public PerLight(Vector4 cameraSpaceLightPos, Vector4 lightIntensity) {
			this.cameraSpaceLightPos = cameraSpaceLightPos;
			this.lightIntensity = lightIntensity;
		}
	}
	
	private class LightBlock {
		private Vector4 ambientIntensity;
		private float lightAttenuation;
		private PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
		
		public LightBlock(Vector4 ambientIntensity, float lightAttenuation) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
		}
	}
	
	private class LightBlockHDR {
		private Vector4 ambientIntensity;
		private float lightAttenuation;
		private float maxIntensity;
		private PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
		
		public LightBlockHDR(Vector4 ambientIntensity, float lightAttenuation, float maxIntensity) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
			this.maxIntensity = maxIntensity;
		}
	}
	
	private class LightBlockGamma {
		private Vector4 ambientIntensity;
		private float lightAttenuation;
		private float maxIntensity;
		private float gamma;
		private PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
		
		public LightBlockGamma(Vector4 ambientIntensity, float lightAttenuation, float maxIntensity, float gamma) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
			this.maxIntensity = maxIntensity;
			this.gamma = gamma;
		}
	}
	
	private class SunlightValue {
		private float normTime;
		private Vector4 ambient;
		private Vector4 sunlightIntensity;
		private Vector4 backgroundColor;
		
		public SunlightValue(float normTime, Vector3 ambient, Vector4 sunlightIntensity, Vector4 backgroundColor) {
			this.normTime = normTime;
			this.ambient = this.ambient;
			this.sunlightIntensity = sunlightIntensity;
			this.backgroundColor = backgroundColor;
		}
	}
	
	private class SunlightValueHDR {
		private float normTime;
		private Vector4 ambient;
		private Vector4 sunlightIntensity;
		private Vector4 backgroundColor;
		private float maxIntensity;
		
		public SunlightValueHDR(float normTime, Vector4 ambient, Vector4 sunlightIntensity, Vector4 backgroundColor, float maxIntensity) {
			this.normTime = normTime;
			this.ambient = ambient;
			this.sunlightIntensity = sunlightIntensity;
			this.backgroundColor = backgroundColor;
			this.maxIntensity = maxIntensity;
		}
	}
	
	private enum TimerTypes {
		TIMER_SUN, TIMER_LIGHTS, TIMER_ALL
	}
}
