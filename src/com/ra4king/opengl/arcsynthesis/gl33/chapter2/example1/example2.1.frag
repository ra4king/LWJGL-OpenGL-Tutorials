#version 330

out vec4 outputColor;

void main()
{
    float lerpValue = gl_FragCoord.y / 600.0;
        
    outputColor = mix(vec4(1.0, 1.0, 1.0, 1.0), vec4(0.2, 0.2, 0.2, 1.0), lerpValue);
}
