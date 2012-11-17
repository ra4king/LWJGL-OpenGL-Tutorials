#version 330

in vec4 diffuseColor;
in vec3 vertexNormal;

out vec4 outputColor;

uniform vec3 modelSpaceLightPos;

uniform vec4 lightIntensity;
uniform vec4 ambientIntensity;

uniform vec3 cameraSpaceLightPos;

uniform float lightAttenuation;
uniform bool useRSquare;

uniform UnProjection {
	mat4 clipToCameraMatrix;
	ivec2 windowSize;
};

vec3 CalcCameraSpacePosition()
{
	vec4 ndcPos;
	ndcPos.xy = ((gl_FragCoord.xy / windowSize.xy) * 2) - 1;
	ndcPos.z = (2 * gl_FragCoord.z - gl_DepthRange.near - gl_DepthRange.far) / (gl_DepthRange.far - gl_DepthRange.near);
	ndcPos.w = 1;
	
	vec4 clipPos = ndcPos / gl_FragCoord.w;
	
	return vec3(clipToCameraMatrix * clipPos);
}

vec4 ApplyLightIntensity(in vec3 cameraSpacePosition, out vec3 lightDirection)
{
	vec3 lightDifference = cameraSpaceLightPos - cameraSpacePosition;
	float lightDistanceSqr = dot(lightDifference, lightDifference);
	lightDirection = lightDifference * inversesqrt(lightDistanceSqr);
	
	float distFactor = useRSquare ? lightDistanceSqr : sqrt(lightDistanceSqr);
	
	return lightIntensity / (1 + lightAttenuation * distFactor);
}

void main()
{
	vec3 cameraSpacePosition = CalcCameraSpacePosition();
	
	vec3 lightDir = vec3(0);
	vec4 attenIntensity = ApplyLightIntensity(cameraSpacePosition, lightDir);
	
	float cosAngIncidence = clamp(dot(normalize(vertexNormal), lightDir), 0, 1);
	
	outputColor = (diffuseColor * attenIntensity * cosAngIncidence) + (diffuseColor * ambientIntensity);
}
