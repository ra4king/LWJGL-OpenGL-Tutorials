package com.ra4king.opengl.util;

import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector3;

public class MousePoles {
	public enum MouseButton {
		LEFT_BUTTON,
		MIDDLE_BUTTON,
		RIGHT_BUTTON;
		
		public static MouseButton getButton(int button) {
			switch(button) {
				case 0: return MouseButton.LEFT_BUTTON;
				case 1: return MouseButton.RIGHT_BUTTON;
				case 2: return MouseButton.MIDDLE_BUTTON;
				case -1: return null;
				default: throw new IllegalArgumentException("Invalid button: " + button);
			}
		}
	}
	
	public enum MouseModifier {
		KEY_SHIFT,
		KEY_CTRL,
		KEY_ALT
	}
	
	public static abstract class Pole {
		public abstract void mouseMove(int x, int y);
		public abstract void mouseClick(MouseButton button, boolean isPressed, MouseModifier modifiers, int positionX, int positionY);
		public abstract void mouseWheel(int direction, MouseModifier modifiers, int positionX, int positionY);
		public abstract void charPress(char key, boolean isShiftPressed, float lastFrameDuration);
	}
	
	private static abstract class ViewProvider extends Pole {
		public abstract Matrix4 calcMatrix();
	}
	
	public static class ObjectData {
		private Vector3 position;
		private Quaternion orientation;
		
		public ObjectData(ObjectData data) {
			position = new Vector3(data.position);
			orientation = new Quaternion(data.orientation);
		}
		
		public ObjectData(Vector3 v, Quaternion q) {
			position = v;
			orientation = q;
		}
	}
	
	public static class ObjectPole extends Pole {
		private enum Axis {
			AXIS_X,
			AXIS_Y,
			AXIS_Z
		}
		
		private enum RotateMode {
			DUAL_AXIS,
			BIAXIAL,
			SPIN
		}
		
		private Vector3[] axisVectors = {
				new Vector3(1, 0, 0),
				new Vector3(0, 1, 0),
				new Vector3(0, 0, 1)
		};
		
		private ViewProvider view;
		private ObjectData po;
		private ObjectData initialPo;
		
		private float rotateScale;
		private MouseButton actionButton;
		
		private RotateMode rotateMode;
		private boolean isDragging;
		
		private int prevMousePosX, prevMousePosY;
		private int startDragMousePosX, startDragMousePosY;
		private Quaternion startDragOrient;
		
		public ObjectPole(ObjectData initialData, float rotateScale, MouseButton actionButton, ViewProvider lookAtProvider) {
			this.view = lookAtProvider;
			this.po = new ObjectData(initialData);
			this.initialPo = initialData;
			this.rotateScale = rotateScale;
			this.actionButton = actionButton;
		}
		
		public Matrix4 calcMatrix() {
			Matrix4 translateMat = new Matrix4().clearToIdentity();
			translateMat.put(3, po.position);
			return translateMat.mult(po.orientation.toMatrix());
		}
		
		public void setRotationScale(float rotateScale) {
			this.rotateScale = rotateScale;
		}
		
		public float getRotationScale() {
			return rotateScale;
		}
		
		public ObjectData getPosOrient() {
			return po;
		}
		
		public boolean isDragging() {
			return isDragging;
		}
		
		public void reset() {
			if(!isDragging)
				po = new ObjectData(initialPo);
		}
		
		private Quaternion calcRotationQuat(Axis axis, float angle) {
			return calcRotationQuat(axis.ordinal(), angle);
		}
		
		private Quaternion calcRotationQuat(int axis, float angle) {
			return Utils.angleAxisDeg(angle, axisVectors[axis]);
		}
		
		private void rotateWorldDegrees(Quaternion rot, boolean fromInitial) {
			if(!isDragging)
				fromInitial = false;
			
			po.orientation = new Quaternion(rot).mult(fromInitial ? startDragOrient : po.orientation).normalize();
		}
		
		public void rotateViewDegrees(Quaternion rot, boolean fromInitial) {
			if(!isDragging)
				fromInitial = false;
			
			if(view == null)
				rotateWorldDegrees(rot, fromInitial);
			else {
				Quaternion viewQuat = view.calcMatrix().toQuaternion();
				Quaternion inViewQuat = new Quaternion(viewQuat).conjugate();
				po.orientation = inViewQuat.mult(rot).mult(viewQuat).mult(fromInitial ? startDragOrient : po.orientation).normalize();
			}
		}
		
		@Override
		public void mouseMove(int positionX, int positionY) {
			if(isDragging) {
				int diffX = positionX - prevMousePosX;
				int diffY = -(positionY - prevMousePosY);
				
				switch(rotateMode) {
					case DUAL_AXIS:
						{
							Quaternion rot = calcRotationQuat(Axis.AXIS_Y, diffX * rotateScale);
							rot = calcRotationQuat(Axis.AXIS_X, diffY * rotateScale).mult(rot).normalize();
							rotateViewDegrees(rot, false);
						}
						break;
					case BIAXIAL:
						{
							int initDiffX = positionX - startDragMousePosX;
							int initDiffY = positionY - startDragMousePosY;
							
							Axis axis;
							float degAngle;
							if(Math.abs(initDiffX) > Math.abs(initDiffY)) {
								axis = Axis.AXIS_Y;
								degAngle = initDiffX * rotateScale;
							}
							else {
								axis = Axis.AXIS_X;
								degAngle = initDiffY * rotateScale;
							}
							
							rotateViewDegrees(calcRotationQuat(axis, degAngle), true);
						}
						break;
					case SPIN:
						rotateViewDegrees(calcRotationQuat(Axis.AXIS_Z, -diffX * rotateScale), false);
						break;
				}
				
				prevMousePosX = positionX;
				prevMousePosY = positionY;
			}
		}
		
		@Override
		public void mouseClick(MouseButton button, boolean isPressed, MouseModifier modifiers, int positionX, int positionY) {
			if(isPressed) {
				if(!isDragging) {
					if(button == actionButton) {
						if(modifiers == MouseModifier.KEY_ALT)
							rotateMode = RotateMode.SPIN;
						else if(modifiers == MouseModifier.KEY_CTRL)
							rotateMode = RotateMode.BIAXIAL;
						else
							rotateMode = RotateMode.DUAL_AXIS;
						
						prevMousePosX = positionX;
						prevMousePosY = positionY;
						
						startDragMousePosX = positionX;
						startDragMousePosY = positionY;
						
						startDragOrient = po.orientation;
						
						isDragging = true;
					}
				}
			}
			else {
				if(isDragging) {
					if(button == actionButton) {
						mouseMove(positionX, positionY);
						
						isDragging = false;
					}
				}
			}
		}
		
		@Override
		public void mouseWheel(int direction, MouseModifier modifiers, int positionX, int positionY) {}
		
		@Override
		public void charPress(char key, boolean isShiftPressed, float lastFrameDuration) {}
	}
	
	public static class ViewData {
		private Vector3 targetPos;
		private Quaternion orient;
		private float radius;
		private float degSpinRotation;
		
		public ViewData(ViewData data) {
			targetPos = new Vector3(data.targetPos);
			orient = new Quaternion(data.orient);
			radius = data.radius;
			degSpinRotation = data.degSpinRotation;
		}
		
		public ViewData(Vector3 v, Quaternion q, float r, float d) {
			targetPos = v;
			orient = q;
			radius = r;
			degSpinRotation = d;
		}
	}
	
	public static class ViewScale {
		private float minRadius;
		private float maxRadius;
		private float largeRadiusDelta;
		private float smallRadiusDelta;
		private float largePosOffset;
		private float smallPosOffset;
		private float rotationScale;
		
		public ViewScale(float min, float max, float large, float small, float largePos, float smallPos, float rot) {
			minRadius = min;
			maxRadius = max;
			largeRadiusDelta = large;
			smallRadiusDelta = small;
			largePosOffset = largePos;
			smallPosOffset = smallPos;
			rotationScale = rot;
		}
	}
	
	public static class ViewPole extends ViewProvider {
		private enum TargetOffsetDir {
			DIR_UP,
			DIR_DOWN,
			DIR_FORWARD,
			DIR_BACKWARD,
			DIR_RIGHT,
			DIR_LEFT;
		}
		
		private enum RotateMode {
			DUAL_AXIS_ROTATE,
			BIAXIAL_ROTATE,
			XZ_AXIS_ROTATE,
			Y_AXIS_ROTATE,
			SPIN_VIEW_AXIS;
		}
		
		private Vector3[] offsets = {
				new Vector3( 0,  1,  0),
				new Vector3( 0, -1,  0),
				new Vector3( 0,  0, -1),
				new Vector3( 0,  0,  1),
				new Vector3( 1,  0,  0),
				new Vector3(-1,  0,  0)
		};
		
		private ViewData currView;
		private ViewScale viewScale;
		
		private ViewData initialView;
		private MouseButton actionButton;
		private boolean rightKeyboardCtrls;
		
		private boolean isDragging;
		private RotateMode rotateMode;
		
		private float degStarDragSpin;
		private int startDragMouseLocX, startDragMouseLocY;
		private Quaternion startDragOrient;
		
		public ViewPole(ViewData initialView, ViewScale viewScale, MouseButton actionButton, boolean rightKeyboardCtrls) {
			this.currView = new ViewData(initialView);
			this.viewScale = viewScale;
			this.initialView = initialView;
			this.actionButton = actionButton;
			this.rightKeyboardCtrls = rightKeyboardCtrls;
		}
		
		@Override
		public Matrix4 calcMatrix() {
			Matrix4 mat = new Matrix4().clearToIdentity();
			
			mat.translate(0, 0, -currView.radius);
			
			Quaternion fullRotation = Utils.angleAxisDeg(currView.degSpinRotation, new Vector3(0, 0, 1)).mult(currView.orient);
			mat.mult(fullRotation.toMatrix());
			
			mat.translate(new Vector3(currView.targetPos).mult(-1));
			
			return mat;
		}
		
		public void reset() {
			if(!isDragging)
				currView = new ViewData(initialView);
		}
		
		public void setRotationScale(float rotateScale) {
			viewScale.rotationScale = rotateScale;
		}
		
		public float getRotationScale() {
			return viewScale.rotationScale;
		}
		
		public ViewData getView() {
			return currView;
		}
		
		public boolean isDragging() {
			return isDragging;
		}
		
		public void processXChange(int diffX) {
			float degAngleDiff = diffX * viewScale.rotationScale;
			
			currView.orient = new Quaternion(startDragOrient).mult(Utils.angleAxisDeg(degAngleDiff, new Vector3(0, 1, 0)));
		}
		
		public void processYChange(int diffY) {
			float degAngleDiff = diffY * viewScale.rotationScale;
			
			currView.orient = Utils.angleAxisDeg(degAngleDiff, new Vector3(1, 0, 0)).mult(startDragOrient);
		}
		
		public void processXYChange(int diffX, int diffY) {
			float degXAngleDiff = diffX * viewScale.rotationScale;
			float degYAngleDiff = diffY * viewScale.rotationScale;
			
			currView.orient = new Quaternion(startDragOrient).mult(Utils.angleAxisDeg(degXAngleDiff, new Vector3(0, 1, 0)));
			currView.orient = Utils.angleAxisDeg(degYAngleDiff, new Vector3(1, 0, 0)).mult(currView.orient);
		}
		
		public void processSpinAxis(int diffX, int diffY) {
			float degSpinDiff = diffX * viewScale.rotationScale;
			currView.degSpinRotation = degSpinDiff + degStarDragSpin;
		}
		
		public void beginDragRotate(int startX, int startY, RotateMode rotMode) {
			rotateMode = rotMode;
			
			startDragMouseLocX = startX;
			startDragMouseLocY = startY;
			
			degStarDragSpin = currView.degSpinRotation;
			
			startDragOrient = currView.orient;
			
			isDragging = true;
		}
		
		public void onDragRotate(int currX, int currY) {
			int diffX = currX - startDragMouseLocX;
			int diffY = -(currY - startDragMouseLocY);
			
			switch (rotateMode) {
				case DUAL_AXIS_ROTATE:
					processXYChange(diffX, diffY);
					break;
				case BIAXIAL_ROTATE:
					if(Math.abs(diffX) > Math.abs(diffY))
						processXChange(diffX);
					else
						processYChange(diffY);
					break;
				case XZ_AXIS_ROTATE:
					processXChange(diffX);
					break;
				case Y_AXIS_ROTATE:
					processYChange(diffY);
					break;
				case SPIN_VIEW_AXIS:
					processSpinAxis(diffX, diffY);
					break;
			}
		}
		
		public void endDragRotate(int endX, int endY, boolean keepResults) {
			if(keepResults)
				onDragRotate(endX, endY);
			else
				currView.orient  = startDragOrient;
			
			isDragging = false;
		}
		
		public void moveCloser(boolean largeStep) {
			if(largeStep)
				currView.radius -= viewScale.largeRadiusDelta;
			else
				currView.radius -= viewScale.smallRadiusDelta;
			
			if(currView.radius < viewScale.minRadius)
				currView.radius = viewScale.minRadius;
		}
		
		public void moveAway(boolean largeStep) {
			if(largeStep)
				currView.radius += viewScale.largeRadiusDelta;
			else
				currView.radius += viewScale.smallRadiusDelta;
			
			if(currView.radius > viewScale.maxRadius)
				currView.radius = viewScale.maxRadius;
		}
		
		@Override
		public void mouseMove(int positionX, int positionY) {
			if(isDragging)
				onDragRotate(positionX, positionY);
		}
		
		@Override
		public void mouseClick(MouseButton button, boolean isPressed, MouseModifier modifiers, int positionX, int positionY) {
			if(isPressed) {
				if(!isDragging) {
					if(button == actionButton) {
						if(modifiers == MouseModifier.KEY_CTRL)
							beginDragRotate(positionX, positionY, RotateMode.BIAXIAL_ROTATE);
						else if(modifiers == MouseModifier.KEY_ALT)
							beginDragRotate(positionX, positionY, RotateMode.SPIN_VIEW_AXIS);
						else
							beginDragRotate(positionX, positionY, RotateMode.DUAL_AXIS_ROTATE);
					}
				}
			}
			else {
				if(isDragging) {
					if(button == actionButton) {
						if(rotateMode == RotateMode.DUAL_AXIS_ROTATE ||
						   rotateMode == RotateMode.SPIN_VIEW_AXIS ||
						   rotateMode == RotateMode.BIAXIAL_ROTATE)
							endDragRotate(positionX, positionY, true);
					}
				}
			}
		}
		
		public void mouseWheel(int direction, MouseModifier modifiers, int positionX, int positionY) {
			if(direction > 0)
				moveCloser(modifiers != MouseModifier.KEY_SHIFT);
			else
				moveAway(modifiers != MouseModifier.KEY_SHIFT);
		}
		
		private void offsetTargetPos(TargetOffsetDir dir, float worldDistance) {
			offsetTargetPos(new Vector3(offsets[dir.ordinal()]).mult(worldDistance));
		}
		
		private void offsetTargetPos(Vector3 cameraOffset) {
			currView.targetPos.add(calcMatrix().toQuaternion().conjugate().mult(cameraOffset));
		}
		
		@Override
		public void charPress(char key, boolean isShiftPressed, float lastFrameDuration) {
			float offset = isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset;
			
			if(rightKeyboardCtrls) {
				switch(key) {
					case 'i': offsetTargetPos(TargetOffsetDir.DIR_FORWARD, offset); break;
					case 'k': offsetTargetPos(TargetOffsetDir.DIR_BACKWARD, offset); break;
					case 'l': offsetTargetPos(TargetOffsetDir.DIR_RIGHT, offset); break;
					case 'j': offsetTargetPos(TargetOffsetDir.DIR_LEFT, offset); break;
					case 'o': offsetTargetPos(TargetOffsetDir.DIR_UP, offset); break;
					case 'u': offsetTargetPos(TargetOffsetDir.DIR_DOWN, offset); break;

					case 'I': offsetTargetPos(TargetOffsetDir.DIR_FORWARD, offset); break;
					case 'K': offsetTargetPos(TargetOffsetDir.DIR_BACKWARD, offset); break;
					case 'L': offsetTargetPos(TargetOffsetDir.DIR_RIGHT, offset); break;
					case 'J': offsetTargetPos(TargetOffsetDir.DIR_LEFT, offset); break;
					case 'O': offsetTargetPos(TargetOffsetDir.DIR_UP, offset); break;
					case 'U': offsetTargetPos(TargetOffsetDir.DIR_DOWN, offset); break;
				}
			}
			else {
				switch(key) {
					case 'w': offsetTargetPos(TargetOffsetDir.DIR_FORWARD, offset); break;
					case 's': offsetTargetPos(TargetOffsetDir.DIR_BACKWARD, offset); break;
					case 'd': offsetTargetPos(TargetOffsetDir.DIR_RIGHT, offset); break;
					case 'a': offsetTargetPos(TargetOffsetDir.DIR_LEFT, offset); break;
					case 'e': offsetTargetPos(TargetOffsetDir.DIR_UP, offset); break;
					case 'q': offsetTargetPos(TargetOffsetDir.DIR_DOWN, offset); break;

					case 'W': offsetTargetPos(TargetOffsetDir.DIR_FORWARD, offset); break;
					case 'S': offsetTargetPos(TargetOffsetDir.DIR_BACKWARD, offset); break;
					case 'D': offsetTargetPos(TargetOffsetDir.DIR_RIGHT, offset); break;
					case 'A': offsetTargetPos(TargetOffsetDir.DIR_LEFT, offset); break;
					case 'E': offsetTargetPos(TargetOffsetDir.DIR_UP, offset); break;
					case 'Q': offsetTargetPos(TargetOffsetDir.DIR_DOWN, offset); break;
				}
			}
		}
	}
}
