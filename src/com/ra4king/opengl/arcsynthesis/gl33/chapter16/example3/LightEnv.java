package com.ra4king.opengl.arcsynthesis.gl33.chapter16.example3;

import java.io.InputStream;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.ra4king.opengl.util.StringUtil;
import com.ra4king.opengl.util.Timer;
import com.ra4king.opengl.util.Timer.Type;
import com.ra4king.opengl.util.interpolators.ConstVelLinearInterpolatorVector;
import com.ra4king.opengl.util.interpolators.TimedLinearInterpolatorVector;
import com.ra4king.opengl.util.interpolators.TimedLinearInterpolatorf;
import com.ra4king.opengl.util.interpolators.WeightedLinearInterpolatorVector;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.math.Vector4;

/**
 * @author ra4king
 */
public class LightEnv {
	private static final int MAX_NUMBER_OF_LIGHTS = 4;
	
	private float lightAttenuation;
	
	private Timer sunTimer;
	private TimedLinearInterpolatorVector<Vector4> ambientInterpolator;
	private TimedLinearInterpolatorVector<Vector4> backgroundInterpolator;
	private TimedLinearInterpolatorVector<Vector4> sunlightInterpolator;
	private TimedLinearInterpolatorf maxIntensityInterpolator;
	
	private ArrayList<ConstVelLinearInterpolatorVector<Vector3>> lightPos;
	private ArrayList<Vector4> lightIntensity;
	private ArrayList<Timer> lightTimers;
	
	public LightEnv(URL url) throws Exception {
		lightAttenuation = 40;
		
		try(InputStream is = url.openStream()) {
			XmlPullParser xml = XmlPullParserFactory.newInstance().newPullParser();
			xml.setInput(is, "UTF-8");
			
			xml.next();
			
			xml.require(XmlPullParser.START_TAG, null, "lightenv");
			
			try {
				lightAttenuation = Float.parseFloat(xml.getAttributeValue(null, "atten"));
			} catch(NumberFormatException exc) {
				System.err.println("'atten' attribute at 'lightenv' tag is invalid value");
				throw exc;
			}
			
			lightAttenuation = 1.0f / (lightAttenuation * lightAttenuation);
			
			xml.nextTag();
			xml.require(XmlPullParser.START_TAG, null, "sun");
			
			try {
				sunTimer = new Timer(Type.LOOP, Float.parseFloat(xml.getAttributeValue(null, "time")));
			} catch(NumberFormatException exc) {
				System.err.println("'time' attribute at 'sun' tag is invalid value");
				throw exc;
			}
			
			ambientInterpolator = new TimedLinearInterpolatorVector<>();
			sunlightInterpolator = new TimedLinearInterpolatorVector<>();
			backgroundInterpolator = new TimedLinearInterpolatorVector<>();
			maxIntensityInterpolator = new TimedLinearInterpolatorf();
			
			ArrayList<WeightedLinearInterpolatorVector<Vector4>.Data> ambient = new ArrayList<>();
			ArrayList<WeightedLinearInterpolatorVector<Vector4>.Data> light = new ArrayList<>();
			ArrayList<WeightedLinearInterpolatorVector<Vector4>.Data> background = new ArrayList<>();
			ArrayList<TimedLinearInterpolatorf.Data> maxIntensity = new ArrayList<>();
			
			while(xml.nextTag() == XmlPullParser.START_TAG) {
				xml.require(XmlPullParser.START_TAG, null, "key");
				
				float keyTime;
				try {
					keyTime = Float.parseFloat(xml.getAttributeValue(null, "time")) / 24f;
				} catch(Exception exc) {
					System.err.println("'time' attribute at 'key' tag is invalid value");
					throw exc;
				}
				
				try {
					ambient.add(ambientInterpolator.new Data(parseVector4(xml.getAttributeValue(null, "ambient")), keyTime));
				} catch(IllegalArgumentException exc) {
					System.err.println("'ambient' attribute at 'key' tag is invalid value");
					throw exc;
				}
				
				try {
					light.add(sunlightInterpolator.new Data(parseVector4(xml.getAttributeValue(null, "intensity")), keyTime));
				} catch(IllegalArgumentException exc) {
					System.err.println("'intensity' attribute at 'key' tag is invalid value");
					throw exc;
				}
				
				try {
					background.add(backgroundInterpolator.new Data(parseVector4(xml.getAttributeValue(null, "background")), keyTime));
				} catch(IllegalArgumentException exc) {
					System.err.println("'background' attribute at 'key' tag is invalid value");
					throw exc;
				}
				
				try {
					maxIntensity.add(maxIntensityInterpolator.new Data(Float.parseFloat(xml.getAttributeValue(null, "max-intensity")), keyTime));
				} catch(IllegalArgumentException exc) {
					System.err.println("'max-intensity' attribute at 'key' tag is invalid value");
					throw exc;
				}
				
				xml.nextTag();
				xml.require(XmlPullParser.END_TAG, null, "key");
			}
			
			xml.require(XmlPullParser.END_TAG, null, "sun");
			
			if(ambient.isEmpty())
				throw new IllegalArgumentException("'sun' element must have at least one 'key' element child");
			
			ambientInterpolator.setValues(ambient);
			sunlightInterpolator.setValues(light);
			backgroundInterpolator.setValues(background);
			maxIntensityInterpolator.setValues(maxIntensity);
			
			lightTimers = new ArrayList<>();
			lightIntensity = new ArrayList<>();
			
			lightPos = new ArrayList<>();
			
			while(xml.nextTag() == XmlPullParser.START_TAG) {
				xml.require(XmlPullParser.START_TAG, null, "light");
				
				if(lightPos.size() + 1 == MAX_NUMBER_OF_LIGHTS)
					throw new RuntimeException("Too many lights specified.");
				
				try {
					lightTimers.add(new Timer(Type.LOOP, Float.parseFloat(xml.getAttributeValue(null, "time"))));
				} catch(IllegalArgumentException exc) {
					System.err.println("'time' attribute in 'light' tag is invalid value");
					throw exc;
				}
				
				try {
					lightIntensity.add(parseVector4(xml.getAttributeValue(null, "intensity")));
				} catch(IllegalArgumentException exc) {
					System.err.println("'intensity' attribute in 'light' tag is invalid value");
					throw exc;
				}
				
				ArrayList<Vector3> posValues = new ArrayList<>();
				
				while(xml.nextTag() == XmlPullParser.START_TAG && xml.getName().equals("key")) {
					xml.next();
					xml.require(XmlPullParser.TEXT, null, null);
					
					try {
						posValues.add(parseVector3(xml.getText()));
					} catch(IllegalArgumentException exc) {
						System.err.println("contents of 'key' tag invalid value");
						throw exc;
					}
					
					xml.next();
					xml.require(XmlPullParser.END_TAG, null, "key");
				}
				
				if(posValues.isEmpty())
					throw new RuntimeException("'light' elements must have at least one 'key' element child");
				
				ConstVelLinearInterpolatorVector<Vector3> interpolator = new ConstVelLinearInterpolatorVector<>();
				interpolator.setValues(posValues);
				lightPos.add(interpolator);
				
				xml.require(XmlPullParser.END_TAG, null, "light");
			}
		}
	}
	
	private Vector4 parseVector4(String s) {
		String[] comp = StringUtil.split(s, ' ');
		if(comp.length != 4)
			throw new IllegalArgumentException("invalid Vector4");
		
		Vector4 vec = new Vector4();
		vec.x(Float.parseFloat(comp[0]));
		vec.y(Float.parseFloat(comp[1]));
		vec.z(Float.parseFloat(comp[2]));
		vec.w(Float.parseFloat(comp[3]));
		
		return vec;
	}
	
	private Vector3 parseVector3(String s) throws NumberFormatException {
		String[] comp = StringUtil.split(s, ' ');
		if(comp.length != 3)
			throw new IllegalArgumentException("invalid Vector3");
		
		Vector3 vec = new Vector3();
		vec.x(Float.parseFloat(comp[0]));
		vec.y(Float.parseFloat(comp[1]));
		vec.z(Float.parseFloat(comp[2]));
		
		return vec;
	}
	
	public void updateTime(long deltaTime) {
		sunTimer.update(deltaTime);
		
		for(Timer t : lightTimers)
			t.update(deltaTime);
	}
	
	public void togglePause() {
		boolean isPaused = sunTimer.togglePause();
		
		for(Timer t : lightTimers)
			t.setPause(isPaused);
	}
	
	public void setPause() {
		setPause(true);
	}
	
	public void setPause(boolean pause) {
		sunTimer.setPause(pause);
		
		for(Timer t : lightTimers)
			t.setPause(pause);
	}
	
	public boolean isPaused() {
		return sunTimer.isPaused();
	}
	
	public void rewindTime(float secRewind) {
		sunTimer.rewind(secRewind);
		
		for(Timer t : lightTimers)
			t.rewind(secRewind);
	}
	
	public void fastForwardTime(float secFF) {
		sunTimer.fastForward(secFF);
		
		for(Timer t : lightTimers)
			t.fastForward(secFF);
	}
	
	public Vector4 getBackgroundColor() {
		return backgroundInterpolator.interpolate(sunTimer.getAlpha());
	}
	
	public float getMaxIntensity() {
		return maxIntensityInterpolator.interpolate(sunTimer.getAlpha());
	}
	
	public Vector4 getSunlightDirection() {
		float angle = (float)(2 * Math.PI * sunTimer.getAlpha());
		Vector4 sunDirection = new Vector4((float)Math.sin(angle), (float)Math.cos(angle), 0, 0);
		return new Matrix4().clearToIdentity().rotateDeg(5, 0, 1, 0).mult(sunDirection);
	}
	
	public Vector4 getSunlightIntensity() {
		return sunlightInterpolator.interpolate(sunTimer.getAlpha());
	}
	
	public Vector4 getSunlightScaledIntensity() {
		return sunlightInterpolator.interpolate(sunTimer.getAlpha()).divide(maxIntensityInterpolator.interpolate(sunTimer.getAlpha()));
	}
	
	public float getElapsedTime() {
		return sunTimer.getProgression();
	}
	
	public int getNumLights() {
		return getNumPointLights() + 1;
	}
	
	public LightBlock getLightBlock(Matrix4 worldToCamera) {
		LightBlock lightData = new LightBlock(ambientInterpolator.interpolate(sunTimer.getAlpha()), lightAttenuation, maxIntensityInterpolator.interpolate(sunTimer.getAlpha()));
		lightData.lights[0] = new PerLight(worldToCamera.mult(getSunlightDirection()), sunlightInterpolator.interpolate(sunTimer.getAlpha()));
		
		for(int light = 0; light < lightPos.size(); light++)
			lightData.lights[light + 1] = new PerLight(worldToCamera.mult(new Vector4(getPointLightWorldPos(light), 1)), lightIntensity.get(light).copy());
		
		return lightData;
	}
	
	public int getNumPointLights() {
		return lightPos.size();
	}
	
	public Vector4 getPointLightIntensity(int pointLightIdx) {
		return lightIntensity.get(pointLightIdx).copy();
	}
	
	public Vector4 getPointLightScaledIntensity(int pointLightIdx) {
		return lightIntensity.get(pointLightIdx).copy().divide(maxIntensityInterpolator.interpolate(sunTimer.getAlpha()));
	}
	
	public Vector3 getPointLightWorldPos(int pointLightIdx) {
		return lightPos.get(pointLightIdx).interpolate(lightTimers.get(pointLightIdx).getAlpha());
	}
	
	public static class PerLight {
		private Vector4 cameraSpaceLightPos;
		private Vector4 lightIntensity;
		
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
		private Vector4 ambientIntensity;
		private float lightAttenuation;
		private float maxIntensity;
		private float[] padding = new float[2];
		private PerLight[] lights = new PerLight[MAX_NUMBER_OF_LIGHTS];
		
		public static final int SIZE = 2 * 4 * 4 + MAX_NUMBER_OF_LIGHTS * PerLight.SIZE;
		
		public LightBlock(Vector4 ambientIntensity, float lightAttenuation, float maxIntensity) {
			this.ambientIntensity = ambientIntensity;
			this.lightAttenuation = lightAttenuation;
			this.maxIntensity = maxIntensity;
		}
		
		private static FloatBuffer buffer = BufferUtils.createFloatBuffer(SIZE / 4);
		
		public FloatBuffer toBuffer() {
			buffer.clear();
			buffer.put(ambientIntensity.toBuffer());
			buffer.put(lightAttenuation);
			buffer.put(maxIntensity);
			buffer.put(padding);
			
			for(PerLight light : lights)
				if(light != null)
					buffer.put(light.toBuffer());
			
			buffer.flip();
			return buffer;
		}
	}
}
