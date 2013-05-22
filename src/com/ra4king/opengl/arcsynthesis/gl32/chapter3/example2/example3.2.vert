#version 150

in vec4 position;
uniform vec2 offset;

void main()
{
	gl_Position = position + vec4(offset, 0.0, 0.0);
}
