package com.ra4king.opengl.util;

import org.lwjgl.input.Keyboard;

import com.ra4king.opengl.util.math.Matrix4;
import com.ra4king.opengl.util.math.Quaternion;
import com.ra4king.opengl.util.math.Vector2;
import com.ra4king.opengl.util.math.Vector3;

public class MousePoles {
	public enum MouseButton {
		LEFT_BUTTON,
		MIDDLE_BUTTON,
		RIGHT_BUTTON;
		
		public static MouseButton getButton(int button) {
			switch(button) {
				case 0:
					return MouseButton.LEFT_BUTTON;
				case 1:
					return MouseButton.RIGHT_BUTTON;
				case 2:
					return MouseButton.MIDDLE_BUTTON;
				case -1:
					return null;
				default:
					throw new IllegalArgumentException("Invalid button: " + button);
			}
		}
	}
	
	public enum MouseModifier {
		KEY_SHIFT,
		KEY_CTRL,
		KEY_ALT
	}
	
	public static abstract class Pole {
		public abstract void mouseMove(Vector2 position);
		
		public abstract void mouseClick(MouseButton button, boolean isPressed, MouseModifier modifiers, Vector2 position);
		
		public abstract void mouseWheel(int direction, MouseModifier modifiers);
		
		public abstract void charPress(long deltaTime);
	}
	
	private static abstract class ViewProvider extends Pole {
		public abstract Matrix4 calcMatrix();
	}
	
	public static class ObjectData {
		public Vector3 position;
		public Quaternion orientation;
		
		public ObjectData(ObjectData data) {
			position = data.position.copy();
			orientation = data.orientation.copy();
		}
		
		public ObjectData(Vector3 v, Quaternion q) {
			position = v.copy();
			orientation = q.copy();
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
		
		private Vector2 prevMousePos;
		private Vector2 startDragMousePos;
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
			translateMat.putColumn(3, po.position, 1);
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
			
			po.orientation = rot.copy().mult(fromInitial ? startDragOrient : po.orientation).normalize();
		}
		
		public void rotateViewDegrees(Quaternion rot, boolean fromInitial) {
			if(!isDragging)
				fromInitial = false;
			
			if(view == null)
				rotateWorldDegrees(rot, fromInitial);
			else {
				Quaternion viewQuat = view.calcMatrix().toQuaternion();
				Quaternion inViewQuat = viewQuat.copy().conjugate();
				po.orientation = inViewQuat.mult(rot).mult(viewQuat).mult(fromInitial ? startDragOrient : po.orientation).normalize();
			}
		}
		
		@Override
		public void mouseMove(Vector2 position) {
			if(isDragging) {
				Vector2 diff = position.copy().sub(prevMousePos);
				
				switch(rotateMode) {
					case DUAL_AXIS:
						Quaternion rot = calcRotationQuat(Axis.AXIS_Y, diff.x() * rotateScale);
						rot = calcRotationQuat(Axis.AXIS_X, diff.y() * rotateScale).mult(rot).normalize();
						rotateViewDegrees(rot, false);
						break;
					case BIAXIAL: {
						Vector2 initDiff = position.copy().sub(startDragMousePos);
						
						Axis axis;
						float degAngle;
						if(Math.abs(initDiff.x()) > Math.abs(initDiff.y())) {
							axis = Axis.AXIS_Y;
							degAngle = initDiff.x() * rotateScale;
						} else {
							axis = Axis.AXIS_X;
							degAngle = initDiff.y() * rotateScale;
						}
						
						rotateViewDegrees(calcRotationQuat(axis, degAngle), true);
					}
						break;
					case SPIN:
						rotateViewDegrees(calcRotationQuat(Axis.AXIS_Z, -diff.x() * rotateScale), false);
						break;
				}
				
				prevMousePos = position.copy();
			}
		}
		
		@Override
		public void mouseClick(MouseButton button, boolean isPressed, MouseModifier modifiers, Vector2 position) {
			if(isPressed) {
				if(!isDragging) {
					if(button == actionButton) {
						if(modifiers == MouseModifier.KEY_ALT)
							rotateMode = RotateMode.SPIN;
						else if(modifiers == MouseModifier.KEY_CTRL)
							rotateMode = RotateMode.BIAXIAL;
						else
							rotateMode = RotateMode.DUAL_AXIS;
						
						prevMousePos = position.copy();
						
						startDragMousePos = position.copy();
						
						startDragOrient = po.orientation.copy();
						
						isDragging = true;
					}
				}
			} else {
				if(isDragging) {
					if(button == actionButton) {
						mouseMove(position);
						
						isDragging = false;
					}
				}
			}
		}
		
		@Override
		public void mouseWheel(int direction, MouseModifier modifiers) {}
		
		@Override
		public void charPress(long deltaTime) {}
	}
	
	public static class ViewData {
		public Vector3 targetPos;
		public Quaternion orient;
		public float radius;
		public float degSpinRotation;
		
		public ViewData(ViewData data) {
			targetPos = data.targetPos.copy();
			orient = data.orient.copy();
			radius = data.radius;
			degSpinRotation = data.degSpinRotation;
		}
		
		public ViewData(Vector3 v, Quaternion q, float r, float d) {
			targetPos = v;
			orient = q.copy();
			radius = r;
			degSpinRotation = d;
		}
	}
	
	public static class ViewScale {
		public float minRadius;
		public float maxRadius;
		public float largeRadiusDelta;
		public float smallRadiusDelta;
		public float largePosOffset;
		public float smallPosOffset;
		public float rotationScale;
		
		public ViewScale(float min, float max, float large, float small, float largePos, float smallPos, float rot) {
			minRadius = min;
			maxRadius = max;
			largeRadiusDelta = large;
			smallRadiusDelta = small;
			largePosOffset = largePos;
			smallPosOffset = smallPos;
			rotationScale = rot;
		}
		
		public ViewScale(ViewScale v) {
			minRadius = v.minRadius;
			maxRadius = v.maxRadius;
			largeRadiusDelta = v.largeRadiusDelta;
			smallRadiusDelta = v.smallRadiusDelta;
			largePosOffset = v.largePosOffset;
			smallPosOffset = v.smallPosOffset;
			rotationScale = v.rotationScale;
		}
	}
	
	public static class ViewPole extends ViewProvider {
		private enum TargetOffsetDir {
			DIR_UP,
			DIR_DOWN,
			DIR_FORWARD,
			DIR_BACKWARD,
			DIR_RIGHT,
			DIR_LEFT
		}
		
		private enum RotateMode {
			DUAL_AXIS_ROTATE,
			BIAXIAL_ROTATE,
			XZ_AXIS_ROTATE,
			Y_AXIS_ROTATE,
			SPIN_VIEW_AXIS
		}
		
		private Vector3[] offsets = {
				new Vector3(0, 1, 0),
				new Vector3(0, -1, 0),
				new Vector3(0, 0, -1),
				new Vector3(0, 0, 1),
				new Vector3(1, 0, 0),
				new Vector3(-1, 0, 0)
		};
		
		private ViewData currView;
		private ViewScale viewScale;
		
		private ViewData initialView;
		private MouseButton actionButton;
		private boolean rightKeyboardCtrls;
		
		private boolean isDragging;
		private RotateMode rotateMode;
		
		private float degStarDragSpin;
		private Vector2 startDragMouseLoc;
		private Quaternion startDragOrient;
		
		public ViewPole(ViewData initialView, ViewScale viewScale, MouseButton actionButton, boolean rightKeyboardCtrls) {
			this.currView = new ViewData(initialView);
			this.viewScale = new ViewScale(viewScale);
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
			
			mat.translate(currView.targetPos.copy().mult(-1));
			
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
			
			currView.orient = startDragOrient.copy().mult(Utils.angleAxisDeg(degAngleDiff, new Vector3(0, 1, 0)));
		}
		
		public void processYChange(int diffY) {
			float degAngleDiff = diffY * viewScale.rotationScale;
			
			currView.orient = Utils.angleAxisDeg(degAngleDiff, new Vector3(1, 0, 0)).mult(startDragOrient);
		}
		
		public void processXYChange(int diffX, int diffY) {
			float degXAngleDiff = diffX * viewScale.rotationScale;
			float degYAngleDiff = diffY * viewScale.rotationScale;
			
			currView.orient = startDragOrient.copy().mult(Utils.angleAxisDeg(degXAngleDiff, new Vector3(0, 1, 0)));
			currView.orient = Utils.angleAxisDeg(degYAngleDiff, new Vector3(1, 0, 0)).mult(currView.orient);
		}
		
		public void processSpinAxis(int diffX) {
			float degSpinDiff = diffX * viewScale.rotationScale;
			currView.degSpinRotation = degSpinDiff + degStarDragSpin;
		}
		
		public void beginDragRotate(Vector2 start, RotateMode rotMode) {
			rotateMode = rotMode;
			
			startDragMouseLoc = start.copy();
			
			degStarDragSpin = currView.degSpinRotation;
			
			startDragOrient = currView.orient.copy();
			
			isDragging = true;
		}
		
		public void onDragRotate(Vector2 position) {
			int diffX = (int)(position.x() - startDragMouseLoc.x());
			int diffY = -(int)(position.y() - startDragMouseLoc.y());
			
			switch(rotateMode) {
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
					processSpinAxis(diffX);
					break;
			}
		}
		
		public void endDragRotate(Vector2 end, boolean keepResults) {
			if(keepResults)
				onDragRotate(end);
			else
				currView.orient = startDragOrient.copy();
			
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
		public void mouseMove(Vector2 position) {
			if(isDragging)
				onDragRotate(position);
		}
		
		@Override
		public void mouseClick(MouseButton button, boolean isPressed, MouseModifier modifiers, Vector2 position) {
			if(isPressed) {
				if(!isDragging) {
					if(button == actionButton) {
						if(modifiers == MouseModifier.KEY_CTRL)
							beginDragRotate(position, RotateMode.BIAXIAL_ROTATE);
						else if(modifiers == MouseModifier.KEY_ALT)
							beginDragRotate(position, RotateMode.SPIN_VIEW_AXIS);
						else
							beginDragRotate(position, RotateMode.DUAL_AXIS_ROTATE);
					}
				}
			} else {
				if(isDragging) {
					if(button == actionButton) {
						if(rotateMode == RotateMode.DUAL_AXIS_ROTATE ||
								rotateMode == RotateMode.SPIN_VIEW_AXIS ||
								rotateMode == RotateMode.BIAXIAL_ROTATE)
							endDragRotate(position, true);
					}
				}
			}
		}
		
		@Override
		public void mouseWheel(int direction, MouseModifier modifiers) {
			if(direction > 0)
				moveCloser(modifiers != MouseModifier.KEY_SHIFT);
			else
				moveAway(modifiers != MouseModifier.KEY_SHIFT);
		}
		
		private void offsetTargetPos(TargetOffsetDir dir, float worldDistance) {
			offsetTargetPos(offsets[dir.ordinal()].copy().mult(worldDistance));
		}
		
		private void offsetTargetPos(Vector3 cameraOffset) {
			currView.targetPos.add(calcMatrix().toQuaternion().conjugate().mult(cameraOffset));
		}
		
		@Override
		public void charPress(long deltaTime) {
			boolean isShiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
			
			float offset = (isShiftPressed ? viewScale.smallPosOffset : viewScale.largePosOffset) * 10 * deltaTime / (float)1e9;
			
			if(rightKeyboardCtrls) {
				if(Keyboard.isKeyDown(Keyboard.KEY_I))
					offsetTargetPos(TargetOffsetDir.DIR_FORWARD, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_K))
					offsetTargetPos(TargetOffsetDir.DIR_BACKWARD, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_L))
					offsetTargetPos(TargetOffsetDir.DIR_RIGHT, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_J))
					offsetTargetPos(TargetOffsetDir.DIR_LEFT, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_O))
					offsetTargetPos(TargetOffsetDir.DIR_UP, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_U))
					offsetTargetPos(TargetOffsetDir.DIR_DOWN, offset);
			}
			else {
				if(Keyboard.isKeyDown(Keyboard.KEY_W))
					offsetTargetPos(TargetOffsetDir.DIR_FORWARD, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_S))
					offsetTargetPos(TargetOffsetDir.DIR_BACKWARD, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_D))
					offsetTargetPos(TargetOffsetDir.DIR_RIGHT, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_A))
					offsetTargetPos(TargetOffsetDir.DIR_LEFT, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_E))
					offsetTargetPos(TargetOffsetDir.DIR_UP, offset);
				if(Keyboard.isKeyDown(Keyboard.KEY_Q))
					offsetTargetPos(TargetOffsetDir.DIR_DOWN, offset);
			}
		}
	}
}
