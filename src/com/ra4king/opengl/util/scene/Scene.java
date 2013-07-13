package com.ra4king.opengl.util.scene;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import rosick.jglsdk.glimg.DdsLoader;
import rosick.jglsdk.glimg.ImageSet;
import rosick.jglsdk.glimg.TextureGenerator;
import rosick.jglsdk.glimg.TextureGenerator.ForcedConvertFlags;

import com.ra4king.opengl.util.Mesh;
import com.ra4king.opengl.util.ShaderProgram;
import com.ra4king.opengl.util.Utils;
import com.ra4king.opengl.util.math.Matrix3;
import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;
import com.ra4king.opengl.util.scene.Scene.SceneNode.Variant;
import com.ra4king.opengl.util.scene.binders.StateBinder;

/**
 * @author ra4king
 */
public class Scene {
	private HashMap<String,Mesh> meshes;
	private HashMap<String,SceneTexture> textures;
	private HashMap<String,SceneProgram> programs;
	private HashMap<String,SceneNode> nodes;
	
	public Scene(URL url, Class<?> clazz, String prefix) throws IOException, XmlPullParserException {
		meshes = new HashMap<>();
		textures = new HashMap<>();
		programs = new HashMap<>();
		nodes = new HashMap<>();
		
		try(InputStream is = url.openStream()) {
			XmlPullParser xml = XmlPullParserFactory.newInstance().newPullParser();
			xml.setInput(is, "UTF-8");
			
			xml.next();
			xml.require(XmlPullParser.START_TAG, null, "scene");
			
			xml.nextTag();
			xml.require(XmlPullParser.START_TAG, null, "mesh");
			
			int count = 0;
			do {
				String id = xml.getAttributeValue(null, "xml:id");
				String file = xml.getAttributeValue(null, "file");
				
				throwIfNull(id, "id", "mesh " + count);
				throwIfNull(file, "file", "mesh " + count);
				
				if(meshes.containsKey(id))
					throw new IllegalArgumentException("Mesh named '" + id + "' already exists.");
				
				try {
					meshes.put(id, new Mesh(clazz.getResource(prefix + file)));
				} catch(Exception exc) {
					throw new IllegalArgumentException("Invalid mesh file for '" + id + "'.", exc);
				}
				
				xml.next();
				xml.require(XmlPullParser.END_TAG, null, "mesh");
				
				count++;
			} while(xml.nextTag() == XmlPullParser.START_TAG && xml.getName().equals("mesh"));
			
			xml.require(XmlPullParser.START_TAG, null, "texture");
			
			count = 0;
			do {
				String id = xml.getAttributeValue(null, "xml:id");
				String file = xml.getAttributeValue(null, "file");
				
				throwIfNull(id, "id", "texture " + count);
				throwIfNull(file, "file", "texture " + count);
				
				String s = xml.getAttributeValue(null, "srgb");
				throwIfNull(s, "srgb", "texture " + count);
				
				boolean srgb;
				try {
					srgb = Boolean.parseBoolean(s);
				} catch(Exception exc) {
					throw new IllegalArgumentException("srgb should be only true or false, invalid value at texture " + count + ".");
				}
				
				if(textures.containsKey(id))
					throw new IllegalArgumentException("Texture named '" + id + "' already exists.");
				
				try {
					textures.put(id, new SceneTexture(clazz.getResourceAsStream(prefix + file), srgb ? ForcedConvertFlags.FORCE_SRGB_COLORSPACE_FMT : 0));
				} catch(Exception exc) {
					throw new IllegalArgumentException("Texture named '" + id + "' is invalid.", exc);
				}
				
				xml.next();
				xml.require(XmlPullParser.END_TAG, null, "texture");
				
				count++;
			} while(xml.nextTag() == XmlPullParser.START_TAG && xml.getName().equals("texture"));
			
			xml.require(XmlPullParser.START_TAG, null, "prog");
			
			count = 0;
			do {
				String id = xml.getAttributeValue(null, "xml:id");
				String vertexFile = xml.getAttributeValue(null, "vert");
				String fragmentFile = xml.getAttributeValue(null, "frag");
				String modelMatrix = xml.getAttributeValue(null, "model-to-camera");
				
				throwIfNull(id, "id", "prog " + count);
				throwIfNull(vertexFile, "vert", "prog " + count);
				throwIfNull(fragmentFile, "frag", "prog " + count);
				throwIfNull(modelMatrix, "model-to-camera", "prog " + count);
				
				String normalModelMatrix = xml.getAttributeValue(null, "normal-model-to-camera");
				String invNormalModelMatrix = xml.getAttributeValue(null, "normal-camera-to-model");
				String geometryFile = xml.getAttributeValue(null, "geom");
				
				if(programs.containsKey(id))
					throw new IllegalArgumentException("Program named '" + id + "' already exists.");
				
				ShaderProgram program;
				try {
					if(geometryFile != null)
						program = new ShaderProgram(Utils.readFully(clazz.getResourceAsStream(prefix + vertexFile)),
								Utils.readFully(clazz.getResourceAsStream(prefix + geometryFile)),
								Utils.readFully(clazz.getResourceAsStream(prefix + fragmentFile)));
					else
						program = new ShaderProgram(Utils.readFully(clazz.getResourceAsStream(prefix + vertexFile)),
								Utils.readFully(clazz.getResourceAsStream(prefix + fragmentFile)));
				} catch(Exception exc) {
					throw new IllegalArgumentException("Invalid shaders for program " + id, exc);
				}
				
				int matrixLocation = program.getUniformLocation(modelMatrix);
				if(matrixLocation == -1)
					throw new IllegalArgumentException("Program shader '" + id + "' does not have a matrix uniform.");
				
				int normalMatrixLocation = -1;
				if(normalModelMatrix != null) {
					normalMatrixLocation = program.getUniformLocation(normalModelMatrix);
					if(normalMatrixLocation == -1)
						throw new IllegalArgumentException("Program shader '" + id + "' does not have a normal matrix uniform.");
				}
				
				int invNormalMatrixLocation = -1;
				if(invNormalModelMatrix != null) {
					invNormalMatrixLocation = program.getUniformLocation(invNormalModelMatrix);
					if(normalMatrixLocation == -1)
						throw new IllegalArgumentException("Program shader '" + id + "' does not have a inverse normal matrix uniform.");
				}
				
				programs.put(id, new SceneProgram(program, matrixLocation, normalMatrixLocation, invNormalMatrixLocation));
				
				ArrayList<String> blocks = new ArrayList<>();
				ArrayList<String> samplers = new ArrayList<>();
				
				while(xml.nextTag() == XmlPullParser.START_TAG) {
					switch(xml.getName()) {
						case "block": {
							String name = xml.getAttributeValue(null, "name");
							String binding = xml.getAttributeValue(null, "binding");
							
							throwIfNull(name, "name", "block in program '" + id + "'");
							throwIfNull(binding, "binding", "block in program '" + id + "'");
							
							if(blocks.contains(name))
								throw new IllegalArgumentException("Block '" + name + "' is used more than once in program '" + id + "'.");
							
							blocks.add(name);
							
							int blockIndex = program.getUniformBlockIndex(name);
							if(blockIndex == GL_INVALID_INDEX)
								throw new IllegalArgumentException("Block '" + name + "' cannot be found in program '" + id + "'.");
							
							int bindPoint;
							try {
								bindPoint = Integer.parseInt(binding);
							} catch(Exception exc) {
								throw new IllegalArgumentException("Binding in block '" + name + "' is invalid value; must be integer.");
							}
							
							glUniformBlockBinding(program.getProgram(), blockIndex, bindPoint);
							
							xml.next();
							xml.require(XmlPullParser.END_TAG, null, "block");
							
							break;
						}
						case "sampler": {
							String name = xml.getAttributeValue(null, "name");
							String texUnit = xml.getAttributeValue(null, "unit");
							
							throwIfNull(name, "name", "sampler in program '" + id + "'");
							throwIfNull(texUnit, "unit", "sampler in program '" + id + "'");
							
							if(samplers.contains(name))
								throw new IllegalArgumentException("Sampler '" + name + "' is used more than once in program '" + id + "'.");
							
							samplers.add(name);
							
							int samplerLocation = program.getUniformLocation(name);
							if(samplerLocation == -1)
								throw new IllegalArgumentException("Sampler '" + name + "' cannot be found in program '" + id + "'.");
							
							int textureUnit;
							try {
								textureUnit = Integer.parseInt(texUnit);
							} catch(Exception exc) {
								throw new IllegalArgumentException("Texture unit in sampler '" + name + "' is invalid value; must be integer.");
							}
							
							program.begin();
							glUniform1i(samplerLocation, textureUnit);
							program.end();
							
							xml.next();
							xml.require(XmlPullParser.END_TAG, null, "sampler");
							
							break;
						}
						default:
							throw new IllegalArgumentException("Invalid element in program '" + id + "'.");
					}
				}
				
				xml.require(XmlPullParser.END_TAG, null, "prog");
				
				count++;
			} while(xml.nextTag() == XmlPullParser.START_TAG && xml.getName().equals("prog"));
			
			xml.require(XmlPullParser.START_TAG, null, "node");
			
			count = 0;
			do {
				String name = xml.getAttributeValue(null, "name");
				String mesh = xml.getAttributeValue(null, "mesh");
				
				throwIfNull(name, "name", "node " + count);
				throwIfNull(mesh, "mesh", "node " + count);
				
				String prog = xml.getAttributeValue(null, "prog");
				String position = xml.getAttributeValue(null, "pos");
				String orient = xml.getAttributeValue(null, "orient");
				String scale = xml.getAttributeValue(null, "scale");
				
				throwIfNull(position, "pos", "node " + count);
				
				if(nodes.containsKey(name))
					throw new IllegalArgumentException("Node named '" + name + "' already exists.");
				
				if(!meshes.containsKey(mesh))
					throw new IllegalArgumentException("Mesh named '" + mesh + "' in node '" + name + "' does not exist.");
				
				if(prog != null) {
					if(!programs.containsKey(prog))
						throw new IllegalArgumentException("Program named '" + prog + "' in node '" + name + "' does not exist.");
				}
				
				Vector3 nodePos;
				try {
					nodePos = Utils.parseVector3(position);
				} catch(Exception exc) {
					throw new IllegalArgumentException("Invalid Vector3 for position in node '" + name + "'.");
				}
				
				ArrayList<TextureBinding> texBindings = readNodeTextures(name, xml);
				
				HashMap<String,Variant> variants = new HashMap<>();
				
				int variantCount = 0;
				while(xml.getName().equals("variant")) {
					String variantName = xml.getAttributeValue(null, "name");
					String variantProg = xml.getAttributeValue(null, "prog");
					String variantBase = xml.getAttributeValue(null, "base");
					
					throwIfNull(variantName, "name", "variant " + variantCount + " in node '" + name + "'");
					if(variantProg == null && variantBase == null)
						throw new IllegalArgumentException("Variant " + variantCount + " missing base or program in node '" + name + "'.");
					if(variantProg != null && variantBase != null)
						throw new IllegalArgumentException("Variant " + variantCount + " cannot have both base and program in node '" + name + "'.");
					
					if(variants.containsKey(variantName))
						throw new IllegalArgumentException("Variant named '" + variantName + "' already exists in node '" + name + "'.");
					
					ArrayList<TextureBinding> variantTexBindings = readNodeTextures(variantName, xml);
					
					Variant variant;
					if(prog != null) {
						if(!programs.containsKey(prog))
							throw new IllegalArgumentException("Program named '" + prog + "' in variant '" + variantName + "' in node '" + name + "' does not exist.");
						
						variant = new Variant(programs.get(prog), variantTexBindings);
					}
					else
						variant = new Variant(null, variantTexBindings);
					
					variants.put(name, variant);
					
					xml.require(XmlPullParser.END_TAG, null, "variant");
					
					xml.nextTag();
				}
				
				SceneNode node = new SceneNode(meshes.get(mesh), programs.get(prog), nodePos, texBindings, variants);
				nodes.put(name, node);
				
				if(orient != null) {
					try {
						node.setOrient(Utils.parseQuaternion(orient));
					} catch(Exception exc) {
						throw new IllegalArgumentException("Invalid quaternion for orient at node '" + node + "'.", exc);
					}
				}
				
				if(scale != null) {
					try {
						node.setScale(Utils.parseVector3(scale));
					} catch(Exception exc) {
						try {
							node.setScale(new Vector3(Float.parseFloat(scale)));
						} catch(Exception exc2) {
							throw new IllegalArgumentException("Invalid Vector3 or float for scale at node '" + node + "'.");
						}
					}
				}
				
				xml.require(XmlPullParser.END_TAG, null, "node");
				
				count++;
			} while(xml.nextTag() == XmlPullParser.START_TAG && xml.getName().equals("node"));
		}
	}
	
	private ArrayList<TextureBinding> readNodeTextures(String name, XmlPullParser xml) throws IOException, XmlPullParserException {
		ArrayList<TextureBinding> texBindings = new ArrayList<>();
		ArrayList<Integer> texUnits = new ArrayList<>();
		
		while(xml.nextTag() == XmlPullParser.START_TAG && xml.getName().equals("texture")) {
			String texName = xml.getAttributeValue(null, "name");
			String unit = xml.getAttributeValue(null, "unit");
			
			throwIfNull(texName, "name", "texture in node '" + name + "'");
			throwIfNull(unit, "unit", "texture in node '" + name + "'");
			
			if(!textures.containsKey(texName))
				throw new IllegalArgumentException("Texture named '" + texName + "' in node '" + name + "' does not exist.");
			
			int texUnit;
			try {
				texUnit = Integer.parseInt(unit);
			} catch(Exception exc) {
				throw new IllegalArgumentException("Invalid value for unit at texture '" + texName + "' at node '" + name + "'.");
			}
			
			if(texUnits.contains(texUnit))
				throw new IllegalArgumentException("Texture unit used more than once at texture '" + texName + "' at node '" + name + "'.");
			
			texUnits.add(texUnit);
			
			final int[] magFilterGLEnums = {
					GL_NEAREST,
					GL_LINEAR,
					GL_NEAREST,
					GL_LINEAR
			};
			
			final int[] minFilterGLEnums = {
					GL_NEAREST,
					GL_LINEAR,
					GL_NEAREST_MIPMAP_NEAREST,
					GL_NEAREST_MIPMAP_LINEAR
			};
			
			final int[] edgeGLEnums = {
					GL_CLAMP_TO_EDGE,
					GL_CLAMP_TO_BORDER,
					GL_REPEAT,
					GL_MIRRORED_REPEAT
			};
			
			final String[] filterNames = {
					"nearest",
					"linear",
					"mipmap nearest",
					"mipmap linear"
			};
			
			final String[] edgeNames = {
					"clamp edge",
					"clamp border",
					"repeat",
					"mirror repeat"
			};
			
			String filterMode = xml.getAttributeValue(null, "sampler-filter");
			throwIfNull(filterMode, "sampler-filter", "texture in node '" + name + "'");
			
			int filter = -1;
			for(int a = 0; a < filterNames.length; a++)
				if(filterNames[a].equals(filterMode.toLowerCase())) {
					filter = a;
					break;
				}
			
			if(filter == -1)
				throw new IllegalArgumentException("Invalid sampler-filter value '" + filterMode + "' at texture in node '" + name + "'.");
			
			String edgeMode = xml.getAttributeValue(null, "sampler-edge");
			
			int edge;
			if(edgeMode == null)
				edge = 2;
			else {
				edge = -1;
				for(int a = 0; a < edgeNames.length; a++)
					if(edgeNames[a].equals(edgeMode.toLowerCase())) {
						edge = a;
						break;
					}
				
				if(edge == -1)
					throw new IllegalArgumentException("Invalid sampler-edge value '" + edgeMode + "' at texture in node '" + name + "'.");
			}
			
			String anisoMode = xml.getAttributeValue(null, "sampler-aniso");
			
			float aniso;
			if(anisoMode == null)
				aniso = 0;
			else {
				anisoMode = anisoMode.toLowerCase();
				
				float max = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
				
				switch(anisoMode) {
					case "none":
						aniso = 0;
						break;
					case "half":
						aniso = max / 2;
						break;
					case "max":
						aniso = max;
						break;
					default:
						try {
							aniso = Integer.parseInt(anisoMode);
							
							if(aniso > max)
								aniso = max;
						} catch(Exception exc) {
							throw new IllegalArgumentException("Invalid value for sampler-aniso '" + anisoMode + "' at texture in node '" + name + "'.");
						}
				}
			}
			
			int sampler = glGenSamplers();
			glSamplerParameteri(sampler, GL_TEXTURE_WRAP_S, edgeGLEnums[edge]);
			glSamplerParameteri(sampler, GL_TEXTURE_WRAP_T, edgeGLEnums[edge]);
			glSamplerParameteri(sampler, GL_TEXTURE_WRAP_R, edgeGLEnums[edge]);
			
			glSamplerParameteri(sampler, GL_TEXTURE_MAG_FILTER, magFilterGLEnums[filter]);
			glSamplerParameteri(sampler, GL_TEXTURE_MIN_FILTER, minFilterGLEnums[filter]);
			
			if(aniso > 0)
				glSamplerParameterf(sampler, GL_TEXTURE_MAX_ANISOTROPY_EXT, aniso);
			
			texBindings.add(new TextureBinding(textures.get(texName), texUnit, sampler));
			
			xml.next();
			xml.require(XmlPullParser.END_TAG, null, "texture");
		}
		
		return texBindings;
	}
	
	private void throwIfNull(String s, String name, String type) {
		if(s == null)
			throw new IllegalArgumentException("No " + name + " at " + type + ".");
	}
	
	public SceneNode findNode(String nodeName) {
		return nodes.get(nodeName);
	}
	
	public ShaderProgram findProgram(String programName) {
		return programs.get(programName).program;
	}
	
	public Mesh findMesh(String meshName) {
		return meshes.get(meshName);
	}
	
	public SceneTexture findTexture(String textureName) {
		return textures.get(textureName);
	}
	
	public void render(Matrix4 cameraMatrix) {
		for(SceneNode node : nodes.values())
			node.render(cameraMatrix);
	}
	
	public static class Transform {
		private Quaternion orient = new Quaternion();
		private Vector3 scale = new Vector3(1);
		private Vector3 translate = new Vector3();
		
		public Matrix4 getMatrix() {
			return new Matrix4().clearToIdentity().translate(translate).mult(orient.toMatrix()).scale(scale);
		}
	}
	
	public static class SceneTexture {
		public int texture;
		public int type;
		
		public SceneTexture(InputStream is, int creationFlags) throws IOException {
			if(is == null)
				throw new IOException("Invalid InputStream");
			
			ImageSet imageSet = DdsLoader.load(is);
			texture = TextureGenerator.createTexture(imageSet, creationFlags);
			type = TextureGenerator.getTextureType(imageSet, creationFlags);
		}
	}
	
	public static class SceneProgram {
		private ShaderProgram program;
		private int matrixUniform;
		private int normalMatrixUniform;
		private int invNormalMatrixUniform;
		
		public SceneProgram(ShaderProgram program, int matrixUniform, int normalMatrixUniform, int invNormalMatrixUniform) {
			this.program = program;
			this.matrixUniform = matrixUniform;
			this.normalMatrixUniform = normalMatrixUniform;
			this.invNormalMatrixUniform = invNormalMatrixUniform;
		}
	}
	
	public static class SceneNode {
		private Mesh mesh;
		private Variant baseVariant;
		
		private HashMap<String,Variant> variants;
		private ArrayList<StateBinder> binders;
		
		private Transform nodeTransform = new Transform();
		private Transform objectTransform = new Transform();
		
		public SceneNode(Mesh mesh, SceneProgram program, Vector3 nodePos, ArrayList<TextureBinding> texBindings, HashMap<String,Variant> variants) {
			this.mesh = mesh;
			
			baseVariant = new Variant(program, texBindings);
			
			this.variants = variants;
			
			binders = new ArrayList<>();
			
			nodeTransform.translate.set(nodePos);
		}
		
		public void setScale(Vector3 scale) {
			nodeTransform.scale.set(scale);
		}
		
		public void rotate(Quaternion orient) {
			nodeTransform.orient.mult(orient);
		}
		
		public void setOrient(Quaternion orient) {
			nodeTransform.orient.set(orient).normalize();
		}
		
		public Quaternion getOrient() {
			return nodeTransform.orient.copy();
		}
		
		public void offset(Vector3 offset) {
			nodeTransform.translate.add(offset);
		}
		
		public void setTranslate(Vector3 translate) {
			nodeTransform.translate.set(translate);
		}
		
		public void setStateBinder(StateBinder binder) {
			binders.add(binder);
		}
		
		public ShaderProgram getProgram() {
			return baseVariant.program.program;
		}
		
		public void render(Matrix4 baseMatrix) {
			if(baseVariant.program != null)
				render(baseVariant, baseMatrix);
		}
		
		public void render(String variation, Matrix4 baseMatrix) {
			Variant v = variants.get(variation);
			if(v == null)
				throw new IllegalArgumentException("Invalid variation");
			
			render(v, baseMatrix);
		}
		
		private void render(Variant variant, Matrix4 baseMatrix) {
			baseMatrix = baseMatrix.copy().mult(nodeTransform.getMatrix());
			Matrix4 objectMatrix = baseMatrix.copy().mult(objectTransform.getMatrix());
			
			variant.program.program.begin();
			glUniformMatrix4(variant.program.matrixUniform, false, objectMatrix.toBuffer());
			
			if(variant.program.normalMatrixUniform != -1)
				glUniformMatrix3(variant.program.normalMatrixUniform, false, new Matrix3(objectMatrix.inverse().transpose()).toBuffer());
			
			if(variant.program.invNormalMatrixUniform != -1)
				glUniformMatrix3(variant.program.invNormalMatrixUniform, false, new Matrix3(objectMatrix.inverse().transpose()).inverse().toBuffer());
			
			for(StateBinder binder : binders)
				binder.bindState(variant.program.program);
			
			for(TextureBinding binding : variant.texBindings) {
				glActiveTexture(GL_TEXTURE0 + binding.texUnit);
				glBindTexture(binding.tex.type, binding.tex.texture);
				glBindSampler(binding.texUnit, binding.sampler);
			}
			
			mesh.render();
			
			for(TextureBinding binding : variant.texBindings) {
				glActiveTexture(GL_TEXTURE0 + binding.texUnit);
				glBindTexture(binding.tex.type, 0);
				glBindSampler(binding.texUnit, 0);
			}
			
			for(StateBinder binder : binders)
				binder.unbindState(variant.program.program);
			
			variant.program.program.end();
		}
		
		public static class Variant {
			private SceneProgram program;
			private ArrayList<TextureBinding> texBindings;
			
			public Variant(SceneProgram program, ArrayList<TextureBinding> texBindings) {
				this.program = program;
				this.texBindings = texBindings;
			}
		}
	}
	
	public static class TextureBinding {
		private SceneTexture tex;
		private int texUnit;
		private int sampler;
		
		public TextureBinding(SceneTexture tex, int texUnit, int sampler) {
			this.tex = tex;
			this.texUnit = texUnit;
			this.sampler = sampler;
		}
	}
}
