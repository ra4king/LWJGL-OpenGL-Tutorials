#version 330

layout(location = 0) in vec4 position;
layout(location = 1) in vec4 color;

smooth out vec4 theColor;

uniform mat4 perspectiveMatrix;
uniform mat4 modelViewMatrix;

void main()
{
	gl_Position = perspectiveMatrix * (modelViewMatrix * position);
	theColor = color;
}
