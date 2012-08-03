#version 150

in vec4 position;
in vec4 color;

smooth out vec4 theColor;

uniform vec2 offset;
uniform mat4 perspectiveMatrix;

void main()
{
	gl_Position = perspectiveMatrix * (position + vec4(offset, 0, 0));
	theColor = color;
}
