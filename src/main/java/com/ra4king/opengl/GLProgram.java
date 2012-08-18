package com.ra4king.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

public abstract class GLProgram {
	private int fps;
	
	public GLProgram(boolean vsync) {
		try {
			Display.setFullscreen(true);
			Display.setVSyncEnabled(vsync);
		}
		catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public GLProgram(String name, int width, int height, boolean resizable) {
		Display.setTitle(name);
		
		try {
			Display.setDisplayMode(new DisplayMode(width,height));
		}
		catch(Exception exc) {
			exc.printStackTrace();
		}
		
		Display.setResizable(resizable);
		
		fps = 60;
	}
	
	public void setFPS(int fps) {
		this.fps = fps;
	}
	
	public int getFPS() {
		return fps;
	}
	
	public final void run() {
		try {
			Display.create();
		}
		catch(Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		}
		
		gameLoop();
	}
	
	public final void run(boolean core) {
		try {
			Display.create(new PixelFormat(),core ? new ContextAttribs(3,3) : new ContextAttribs());
		}
		catch(Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		}
		
		gameLoop();
	}
	
	public final void run(int major, int minor) {
		try {
			Display.create(new PixelFormat(),new ContextAttribs(major,minor));
		}
		catch(Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		}
		
		gameLoop();
	}
	
	private void gameLoop() {
		try {
			init();
			
			resized();
			
			long lastTime, lastFPS;
			lastTime = lastFPS = System.nanoTime();
			int frames = 0;
			
			while(!Display.isCloseRequested() && !shouldStop()) {
				long deltaTime = System.nanoTime() - lastTime;
				lastTime += deltaTime;
				
				if(Display.wasResized())
					resized();
				
				while(Keyboard.next()) {
					if(Keyboard.getEventKeyState())
						keyPressed(Keyboard.getEventKey(),Keyboard.getEventCharacter(),Keyboard.getEventNanoseconds());
					else
						keyReleased(Keyboard.getEventKey(),Keyboard.getEventCharacter(),Keyboard.getEventNanoseconds());
				}
				
				update(deltaTime);
				render();
				
				Display.update();
				
				checkGLError();
				
				frames++;
				if(System.nanoTime() - lastFPS >= 1e9) {
					System.out.println("FPS: ".concat(String.valueOf(frames)));
					lastFPS += 1e9;
					frames = 0;
				}
				
				Display.sync(fps);
			}
		}
		catch(Throwable exc) {
			exc.printStackTrace();
		}
		finally {
			destroy();
		}
	}
	
	public void checkGLError() {
		int error;
		while((error = glGetError()) != GL_NO_ERROR)
			throw new RuntimeException("OpenGL Error: " + gluErrorString(error));
	}
	
	public int getWidth() {
		return Display.getWidth();
	}
	
	public int getHeight() {
		return Display.getHeight();
	}
	
	public abstract void init();
	
	public void resized() {
		glViewport(0,0,getWidth(),getHeight());
	}
	
	public boolean shouldStop() {
		return Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
	}
	
	public void keyPressed(int key, char c, long nanos) {}
	
	public void keyReleased(int key, char c, long nanos) {}
	
	public void update(long deltaTime) {}
	
	public abstract void render();
	
	public void destroy() {
		Display.destroy();
		System.exit(0);
	}
	
	protected String readFromFile(String file) {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file),"UTF-8"))) {
			StringBuilder s = new StringBuilder();
			String l;
			
			while((l = reader.readLine()) != null)
				s.append(l).append('\n');
			
			return s.toString();
		}
		catch(Exception exc) {
			throw new RuntimeException("Failure reading file: " + file,exc);
		}
	}
}
