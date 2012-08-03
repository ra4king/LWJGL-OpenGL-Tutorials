#version 330

layout(location = 0) in vec4 position;
uniform float loopDuration;
uniform float time;

void main()
{
	float timeScale = 3.14159f * 2.0f / loopDuration;
	
	float currTime = mod(time, loopDuration);
	
	gl_Position = position + vec4(cos(currTime * timeScale) * 0.5f,
								  sin(currTime * timeScale) * 0.5f,
								  0.0f,
								  0.0f);
}
