package com.ra4king.opengl.util.scene;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author ra4king
 */
public class Scene {
	private HashMap<String,SceneMesh> meshes;
	private HashMap<String,SceneTexture> textures;
	private HashMap<String,SceneProgram> programs;
	private HashMap<String,SceneNode> nodes;
	
	private ArrayList<SceneNode> rootNodes;
	private ArrayList<Integer> samplers;
	
	public Scene(URL url) throws Exception {
		try(InputStream is = url.openStream()) {
			XmlPullParser xml = XmlPullParserFactory.newInstance().newPullParser();
			xml.setInput(is, "UTF-8");
			
			xml.next();
			
			xml.require(XmlPullParser.START_TAG, null, "scene");
			
		}
	}
	
	public static class SceneMesh {
		
	}
	
	public static class SceneTexture {
		
	}
	
	public static class SceneProgram {
		
	}
	
	public static class SceneNode {
		
	}
}
