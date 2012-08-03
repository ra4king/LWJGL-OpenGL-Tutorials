#version 330

layout(location = 0) in vec4 position;
layout(location = 1) in vec4 color;

smooth out vec4 theColor;

uniform vec3 offset;
uniform mat4 perspectiveMatrix;

void main()
{
	gl_Position = perspectiveMatrix * (position + vec4(offset, 0));
	theColor = color;
}
