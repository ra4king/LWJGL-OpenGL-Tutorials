#version 330

layout(location = 0) in vec4 position;
layout(location = 1) in vec4 color;

smooth out vec4 interpColor;

layout(std140) uniform GlobalMatrices
{
	mat4 cameraToClipMatrix;
	mat4 worldToCameraMatrix;
};

uniform mat4 modelToWorldMatrix;

void main()
{
	gl_Position = cameraToClipMatrix * worldToCameraMatrix * modelToWorldMatrix * position;
	interpColor = color;
}
