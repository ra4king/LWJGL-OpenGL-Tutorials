#version 150

out vec4 outputColor;

uniform float fragLoopDuration;
uniform float time;

const vec4 firstColor = vec4(1.0, 1.0, 1.0, 1.0);
const vec4 secondColor = vec4(0.0, 1.0, 0.0, 1.0);

void main()
{
	float currTime = mod(time, fragLoopDuration);
	float currLerp = currTime/ fragLoopDuration;
	
	outputColor = mix(firstColor, secondColor, currLerp);
}
