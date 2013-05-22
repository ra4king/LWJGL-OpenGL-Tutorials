#version 150

in vec4 position;
uniform float loopDuration;
uniform float time;

void main()
{
	float timeScale = 3.14159 * 2.0 / loopDuration;
	
	float currTime = mod(time, loopDuration);
	
	gl_Position = position + vec4(cos(currTime * timeScale) * 0.5,
								  sin(currTime * timeScale) * 0.5,
								  0.0,
								  0.0);
}
