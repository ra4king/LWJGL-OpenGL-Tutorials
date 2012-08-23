package com.ra4king.opengl.util;

public class Timer {
	public enum Type {
		LOOP,
		SINGLE,
		INFINITE
	}
	
	private Type type;
	private float secDuration;
	
	private boolean hasUpdated;
	private boolean isPaused;
	
	private float absPrevTime;
	private float secAccumTime;
	
	private long elapsedTime;
	
	public Timer() {
		this(Type.INFINITE, 1);
	}
	
	public Timer(Type type, float duration) {
		this.type = type;
		this.secDuration = duration;
		
		if(type != Type.INFINITE && duration <= 0)
			throw new IllegalArgumentException("duration cannot be less than or equal to 0");
	}
	
	public void reset() {
		hasUpdated = false;
		secAccumTime = 0;
	}
	
	public boolean togglePause() {
		isPaused = !isPaused;
		return isPaused;
	}
	
	public boolean isPaused() {
		return isPaused;
	}
	
	public void setPause(boolean pause) {
		isPaused = pause;
	}
	
	public boolean update(long deltaTime) {
		elapsedTime += deltaTime;
		
		float currTime = elapsedTime / (float)1e9;
		
		if(!hasUpdated) {
			absPrevTime = currTime;
			hasUpdated = true;
		}
		
		if(isPaused) {
			absPrevTime = currTime;
			return false;
		}
		
		float delta = currTime - absPrevTime;
		secAccumTime += delta;
		
		absPrevTime = currTime;
		if(type == Type.SINGLE)
			return secAccumTime > secDuration;
		
		return false;
	}
	
	public void rewind(float secRewind) {
		secAccumTime -= secRewind;
		if(secAccumTime < 0)
			secAccumTime = 0;
	}
	
	public void fastForward(float secFF) {
		secAccumTime += secFF;
	}
	
	public float getAlpha() {
		switch(type) {
			case LOOP:
				return (secAccumTime%secDuration) / secDuration;
			case SINGLE:
				return clamp(secAccumTime/secDuration, 0, 1);
			case INFINITE:
			default:
				return -1;
		}
	}
	
	public float getProgression() {
		switch(type) {
			case LOOP:
				return secAccumTime%secDuration;
			case SINGLE:
				return clamp(secAccumTime, 0, secDuration);
			case INFINITE:
			default:
				return -1;
		}
	}
	
	public float getTimeSinceStart() {
		return secAccumTime;
	}
	
	private float clamp(float value, float low, float high) {
		return Math.min(Math.max(value, low), high);
	}
}
