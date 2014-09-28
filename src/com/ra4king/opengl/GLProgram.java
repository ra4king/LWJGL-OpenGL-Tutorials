package com.ra4king.opengl;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import com.ra4king.opengl.util.Utils;

/**
 * This class defines an entry point for OpenGL programs as it handles context creation and the game loop.
 * Entry point classes extend GLProgram and must implement the <code>init()</code> and <code>render()</code> methods.
 * The <code>update(long deltaTime)</code> can be overridden for variable time-steps.
 * 
 * @author Roi Atalla
 */
public abstract class GLProgram {
	private int fps;
	
	/**
	 * Initializes the application in fullscreen mode.
	 * 
	 * @param vsync Enables/disables the vertical-sync feature, where the rendering is in sync with the monitor's refresh rate.
	 *        With v-sync off, there is no framerate cap and the gameloop will run as fast as the hardware can handle.
	 *        A framerate can be set with the <code>setFPS(int fps)</code> method.
	 */
	public GLProgram(boolean vsync) {
		try {
			Display.setFullscreen(true);
			Display.setVSyncEnabled(vsync);
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	
	/**
	 * Initializes a windowed application. The framerate is set to 60 and can be modified using <code>setFPS(int fps)</code>.
	 * 
	 * @param name The title of the window.
	 * @param width The width of the window.
	 * @param height The height of the window.
	 * @param resizable Enables/disables the ability to resize the window.
	 */
	public GLProgram(String name, int width, int height, boolean resizable) {
		Display.setTitle(name);
		
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
		} catch(Exception exc) {
			exc.printStackTrace();
		}
		
		Display.setResizable(resizable);
		
		fps = 60;
	}
	
	/**
	 * Sets the framerate of the game loop.
	 * 
	 * @param fps The desired framerate, in frames per second. It must be a positive integer or 0.
	 *        Specifying 0 means no rendering cap. Specifying a negative value will result in undefined behavior.
	 */
	public void setFPS(int fps) {
		this.fps = fps;
	}
	
	/**
	 * Returns the framerate of the game loop.
	 * 
	 * @return The framerate, in frames per second.
	 */
	public int getFPS() {
		return fps;
	}
	
	/**
	 * Initializes the context with default settings, which is generally the latest OpenGL version supported and compatibility
	 * profile if supported. For Mac OS X, the core profile must be specifically requested therefore this method is not
	 * sufficient.
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * Equivalent to <code>run(false)</code>.
	 */
	public final void run() {
		run(false);
	}
	
	/**
	 * Initializes the context depending on the value of <code>core</code>. Supplying <code>false</code> is equivalent to
	 * simply calling <code>run()</code>, while <code>true</code> requests the Core profile if available.
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * @param core <code>True</code> requests the Core profile, while <code>false</code> keeps default settings.
	 */
	public final void run(boolean core) {
		run(core, new PixelFormat());
	}
	
	/**
	 * Initializes the context depending on the value of <code>core</code> and the supplied PixelFormat.
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * @param core <code>True</code> requests the Core profile, while <code>false</code> keeps default settings.
	 * @param format The PixelFormat specifying the buffers.
	 */
	public final void run(boolean core, PixelFormat format) {
		run(format, core ? new ContextAttribs(3, 2).withProfileCore(true) : null);
	}
	
	/**
	 * Initializes the context requesting the specified OpenGL version in the format: <code>major.minor</code>
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * @param major
	 * @param minor
	 */
	public final void run(int major, int minor) {
		run(major, minor, false);
	}
	
	/**
	 * Initializes the context depending on the value of <code>core</code> and the specified OpenGL version in the format: <code>major.minor</code>
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * @param major
	 * @param minor
	 * @param core <code>True</code> requests the Core profile, while <code>false</code> requests the Compatibility profile.
	 */
	public final void run(int major, int minor, boolean core) {
		run(major, minor, core, new PixelFormat());
	}
	
	/**
	 * Initializes the context depending on the value of <code>core</code>, the supplied PixelFormat, and the specified
	 * OpenGL version in the format: <code>major.minor</code>
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * @param major
	 * @param minor
	 * @param core <<code>True</code> requests the Core profile, while <code>false</code> requests the Compatibility profile.
	 * @param format The PixelFormat specifying the buffers.
	 */
	public final void run(int major, int minor, boolean core, PixelFormat format) {
		run(format, core ? new ContextAttribs(major, minor).withProfileCore(core) : new ContextAttribs(major, minor));
	}
	
	/**
	 * Initializes the context with default settings and the supplied PixelFormat.
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * @param format The PixelFormat specifying the buffers.
	 */
	public final void run(PixelFormat format) {
		run(format, null);
	}
	
	/**
	 * Initializes the context with its attributes supplied.
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * @param attribs The context attributes.
	 */
	public final void run(ContextAttribs attribs) {
		run(new PixelFormat(), attribs);
	}
	
	/**
	 * Initializes the context with its attributes and PixelFormat supplied.
	 * 
	 * This method does not return until the game loop ends.
	 * 
	 * @param format The PixelFormat specifying the buffers.
	 * @param attribs The context attributes.
	 */
	public final void run(PixelFormat format, ContextAttribs attribs) {
		try {
			Display.create(format, attribs);
		} catch(Exception exc) {
			exc.printStackTrace();
			System.exit(1);
		}
		
		gameLoop();
	}
	
	private void gameLoop() {
		try {
			init();
			
			Utils.checkGLError("init");
			
			resized();
			
			Utils.checkGLError("resized");
			
			long lastTime, lastFPS;
			lastTime = lastFPS = System.nanoTime();
			int frames = 0;
			
			while(!shouldStop()) {
				long deltaTime = System.nanoTime() - lastTime;
				lastTime += deltaTime;
				
				if(Display.wasResized())
					resized();
				
				while(Keyboard.next()) {
					if(Keyboard.getEventKeyState())
						keyPressed(Keyboard.getEventKey(), Keyboard.getEventCharacter());
					else
						keyReleased(Keyboard.getEventKey(), Keyboard.getEventCharacter());
				}
				
				update(deltaTime);
				
				Utils.checkGLError("update");
				
				render();
				
				Utils.checkGLError("render");
				
				Display.update();
				
				frames++;
				if(System.nanoTime() - lastFPS >= 1e9) {
					System.out.println("FPS: ".concat(String.valueOf(frames)));
					lastFPS += 1e9;
					frames = 0;
				}
				
				Display.sync(fps);
			}
		} catch(Throwable exc) {
			exc.printStackTrace();
		} finally {
			destroy();
		}
	}
	
	/**
	 * Returns the width of the window.
	 * 
	 * @return The width of the window.
	 */
	public int getWidth() {
		return Display.getWidth();
	}
	
	/**
	 * Returns the height of the window.
	 * 
	 * @return The height of the window.
	 */
	public int getHeight() {
		return Display.getHeight();
	}
	
	/**
	 * Called at most once after one of the <code>run</code> methods are called. This method
	 * must be implemented by user code.
	 */
	public abstract void init();
	
	/**
	 * Called when the window is resized. This method updates the <code>glViewport</code> but may be overridden
	 * with custom code. Make sure to call <code>super.resized()</code> if overriding, or remember to manually update
	 * the <code>glViewport</code>!
	 */
	public void resized() {
		glViewport(0, 0, getWidth(), getHeight());
	}
	
	/**
	 * Consistently polled once per frame to test whether the game loop should stop. This
	 * method checks if either the window's close button has been clicked, ALT-F4 has been pressed, or if the ESCAPE key is pressed.
	 * 
	 * @return Returns <code>true</code> if the game loop should stop, otherwise <code>false</code>.
	 */
	public boolean shouldStop() {
		return Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
	}
	
	/**
	 * Called when a key has been pressed.
	 * 
	 * @param key The @see org.lwjgl.input.Keyboard keycode of the pressed key.
	 * @param c The literal character of the pressed key.
	 */
	public void keyPressed(int key, char c) {}
	
	/**
	 * Called when a key has been released.
	 *
	 * @param key The @see org.lwjgl.input.Keyboard keycode of the released key.
	 * @param c The literal character of the released key.
	 */
	public void keyReleased(int key, char c) {}
	
	/**
	 * Called once per frame and given the elapsed time since the last call
	 * to this method.
	 * 
	 * @param deltaTime The elapsed time since the last call to this method, in nanoseconds.
	 */
	public void update(long deltaTime) {}
	
	/**
	 * Called once per frame and must be implemented by user code. Render operations should
	 * occur here.
	 */
	public abstract void render();
	
	/**
	 * Destroys the context.
	 */
	public void destroy() {
		Display.destroy();
	}
	
	/**
	 * Returns the entire contents of the file in a String. The file should be specified as relative to the user class
	 * that extends GLProgram.
	 * 
	 * @param file The file path, relative to the user class extending GLProgram.
	 * @return The entire contents of the file.
	 */
	protected String readFromFile(String file) {
		try {
			return Utils.readFully(getClass().getResourceAsStream(file));
		} catch(Exception exc) {
			throw new RuntimeException("Failure reading file " + file, exc);
		}
	}
}
