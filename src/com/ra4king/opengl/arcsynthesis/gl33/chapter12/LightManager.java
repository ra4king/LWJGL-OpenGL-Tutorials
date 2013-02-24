package com.ra4king.opengl.arcsynthesis.gl33.chapter12;

import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.interpolators.ConstVelLinearInterpolatorVector;
import com.ra4king.opengl.util.interpolators.TimedLinearInterpolatorVector;
import com.ra4king.opengl.util.interpolators.TimedLinearInterpolatorf;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class LightManager {
	public static final int NUMBER_OF_LIGHTS = 4;
	public static final int NUMBER_OF_POINT_LIGHTS = NUMBER_OF_LIGHTS - 1;
	
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

		extraTimers = new HashMap<>();
		
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
	
	// public Vector4 getMaxIntensityValue(Pair<Vector4,Float> data) {
	// return data.first;
	// }
	//
	// public float getMaxIntensityTime(Pair<Vector4,Float> data) {
	// return data.second;
	// }
	//
	// public float getLightVectorValue(Pair<Float,Float> data) {
	// return data.first;
	// }
	//
	// public float getLightVectorTime(Pair<Float,Float> data) {
	// return data.second;
	// }
	
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
	
	public void updateTime(long deltaTime) {
		sunTimer.update(deltaTime);
		for(Timer t : lightTimers)
			t.update(deltaTime);
		for(Timer t : extraTimers.values())
			t.update(deltaTime);
	}
	
	public void setPause(TimerTypes timer, boolean pause) {
		if(timer == TimerTypes.TIMER_ALL || timer == TimerTypes.TIMER_LIGHTS) {
			for(Timer t : lightTimers)
				t.setPause(pause);
			for(Timer t : extraTimers.values())
				t.setPause(pause);
		}
		
		if(timer == TimerTypes.TIMER_ALL || timer == TimerTypes.TIMER_SUN)
			sunTimer.togglePause();
	}
	
	public void togglePause(TimerTypes timer) {
		setPause(timer, !isPaused(timer));
	}
	
	public boolean isPaused(TimerTypes timer) {
		if(timer == TimerTypes.TIMER_ALL || timer == TimerTypes.TIMER_SUN)
			return sunTimer.isPaused();
		
		return lightTimers.get(0).isPaused();
	}
	
	public void rewindTime(TimerTypes timer, float secRewind) {
		if(timer == TimerTypes.TIMER_ALL || timer == TimerTypes.TIMER_SUN)
			sunTimer.rewind(secRewind);
		
		if(timer == TimerTypes.TIMER_ALL || timer == TimerTypes.TIMER_LIGHTS) {
			for(Timer t : lightTimers)
				t.rewind(secRewind);
			for(Timer t : extraTimers.values())
				t.rewind(secRewind);
		}
	}
	
	public void fastForwardTime(TimerTypes timer, float secFF) {
		if(timer == TimerTypes.TIMER_ALL || timer == TimerTypes.TIMER_SUN)
			sunTimer.fastForward(secFF);
		
		if(timer == TimerTypes.TIMER_ALL || timer == TimerTypes.TIMER_LIGHTS) {
			for(Timer t : lightTimers)
				t.fastForward(secFF);
			for(Timer t : extraTimers.values())
				t.fastForward(secFF);
		}
	}
	
	public LightBlock getLightInformation(Matrix4 worldToCameraMatrix) {
		LightBlock lightData = new LightBlock(ambientInterpolator.interpolate(sunTimer.getAlpha()), lightAttenuation);
		lightData.lights[0] = new PerLight(worldToCameraMatrix.mult(getSunlightDirection()), sunlightInterpolator.interpolate(sunTimer.getAlpha()));
		
		for(int light = 0; light < NUMBER_OF_POINT_LIGHTS; light++) {
			Vector4 worldLightPos = new Vector4(lightPos.get(light).interpolate(lightTimers.get(light).getAlpha()), 1);
			Vector4 lightPosCameraSpace = worldToCameraMatrix.mult(worldLightPos);
			
			lightData.lights[light + 1] = new PerLight(lightPosCameraSpace, lightIntensity.get(light));
		}
		
		return lightData;
	}
	
	public LightBlockHDR getLightInformationHDR(Matrix4 worldToCameraMatrix) {
		LightBlockHDR lightData = new LightBlockHDR(ambientInterpolator.interpolate(sunTimer.getAlpha()), lightAttenuation, maxIntensityInterpolator.interpolate(sunTimer.getAlpha()));
		lightData.lights[0] = new PerLight(worldToCameraMatrix.mult(getSunlightDirection()), sunlightInterpolator.interpolate(sunTimer.getAlpha()));
		
		for(int light = 0; light < NUMBER_OF_POINT_LIGHTS; light++) {
			Vector4 worldLightPos = new Vector4(lightPos.get(light).interpolate(lightTimers.get(light).getAlpha()), 1);
			Vector4 lightPosCameraSpace = worldToCameraMatrix.mult(worldLightPos);
			
			lightData.lights[light + 1] = new PerLight(lightPosCameraSpace, lightIntensity.get(light));
		}
		
		return lightData;
	}
	
	public LightBlockGamma getLightInformationGamma(Matrix4 worldToCameraMatrix) {
		LightBlockHDR lightDataHDR = getLightInformationHDR(worldToCameraMatrix);
		LightBlockGamma lightData = new LightBlockGamma(lightDataHDR.ambientIntensity, lightDataHDR.lightAttenuation, lightDataHDR.maxIntensity, 0);
		lightData.lights = lightDataHDR.lights;
		
		return lightData;
	}
	
	public Vector4 getSunlightDirection() {
		float angle = 2 * (float)Math.PI * sunTimer.getAlpha();
		Vector4 sunDirection = new Vector4(0);
		sunDirection.x((float)Math.sin(angle));
		sunDirection.y((float)Math.cos(angle));
		
		return new Matrix4().clearToIdentity().rotateDeg(5, 0, 1, 0).mult(sunDirection);
	}
	
	public Vector4 getSunlightIntensity() {
		return sunlightInterpolator.interpolate(sunTimer.getAlpha());
	}
	
	public int getNumberOfPointLights() {
		return lightPos.size();
	}
	
	public Vector3 getWorldLightPosition(int lightIndex) {
		return lightPos.get(lightIndex).interpolate(lightTimers.get(lightIndex).getAlpha());
	}
	
	public void setPointLightIntensity(int lightIndex, Vector4 intensity) {
		lightIntensity.set(lightIndex, intensity.copy());
	}
	
	public Vector4 getPointLightIntensity(int lightIndeX) {
		return lightIntensity.get(lightIndeX);
	}
	
	public void createTimer(String timerName, Timer.Type type, float duration) {
		extraTimers.put(timerName, new Timer(type, duration));
	}
	
	public float getTimerValue(String timerName) {
		Timer t = extraTimers.get(timerName);
		
		if(t == null)
			return -1;
		
		return t.getAlpha();
	}
	
	public Vector4 getBackgroundColor() {
		return backgroundInterpolator.interpolate(sunTimer.getAlpha());
	}
	
	public float getMaxIntensity() {
		return maxIntensityInterpolator.interpolate(sunTimer.getAlpha());
	}
	
	public float getSunTime() {
		return sunTimer.getAlpha();
	}
	
	public static class PerLight {
		public Vector4 cameraSpaceLightPos;
		public Vector4 lightIntensity;
		
		public static final int SIZE = 2 * 4 * 4;
		
		public PerLight(Vector4 cameraSpaceLightPos, Vector4 lightIntensity) {
			this.cameraSpaceLightPos = cameraSpaceLightPos;
			this.lightIntensity = lightIntensity;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(cameraSpaceLightPos.toBuffer());
			buffer.put(lightIntensity.toBuffer());
			buffer.flip();
			return buffer;
		}
	}
	
	public static class LightBlock {
		public Vector4 ambientIntensity;
		public float lightAttenuation;
		public PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
		
		public static final int SIZE = 2 * 4 * 4 + NUMBER_OF_LIGHTS * PerLight.SIZE;
		
		public LightBlock(Vector4 ambientIntensity, float lightAttenuation) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(ambientIntensity.toBuffer());
			buffer.put(lightAttenuation);
			buffer.put(0).put(0).put(0);
			
			for(PerLight light : lights)
				buffer.put(light.toBuffer());
			
			buffer.flip();
			return buffer;
		}
	}
	
	public static class LightBlockHDR {
		public Vector4 ambientIntensity;
		public float lightAttenuation;
		public float maxIntensity;
		public PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
		
		public static final int SIZE = 2 * 4 * 4 + NUMBER_OF_LIGHTS * PerLight.SIZE;
		
		public LightBlockHDR(Vector4 ambientIntensity, float lightAttenuation, float maxIntensity) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
			this.maxIntensity = maxIntensity;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(ambientIntensity.toBuffer());
			buffer.put(lightAttenuation);
			buffer.put(maxIntensity);
			buffer.put(0).put(0);
			
			for(PerLight light : lights)
				buffer.put(light.toBuffer());
			
			buffer.flip();
			return buffer;
		}
	}
	
	public static class LightBlockGamma {
		public Vector4 ambientIntensity;
		public float lightAttenuation;
		public float maxIntensity;
		public float gamma;
		public PerLight[] lights = new PerLight[NUMBER_OF_LIGHTS];
		
		public static final int SIZE = 2 * 4 * 4 + NUMBER_OF_LIGHTS * PerLight.SIZE;
		
		public LightBlockGamma(Vector4 ambientIntensity, float lightAttenuation, float maxIntensity, float gamma) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
			this.maxIntensity = maxIntensity;
			this.gamma = gamma;
		}
		
		private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(ambientIntensity.toBuffer());
			buffer.put(lightAttenuation);
			buffer.put(maxIntensity);
			buffer.put(gamma);
			buffer.put(0);
			
			for(PerLight light : lights)
				buffer.put(light.toBuffer());
			
			buffer.flip();
			return buffer;
		}
	}
	
	public static class SunlightValue {
		public float normTime;
		public Vector4 ambient;
		public Vector4 sunlightIntensity;
		public Vector4 backgroundColor;
		
		public SunlightValue(float normTime, Vector4 ambient, Vector4 sunlightIntensity, Vector4 backgroundColor) {
			this.normTime = normTime;
			this.ambient = ambient;
			this.sunlightIntensity = sunlightIntensity;
			this.backgroundColor = backgroundColor;
		}
	}
	
	public static class SunlightValueHDR {
		public float normTime;
		public Vector4 ambient;
		public Vector4 sunlightIntensity;
		public Vector4 backgroundColor;
		public float maxIntensity;
		
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
