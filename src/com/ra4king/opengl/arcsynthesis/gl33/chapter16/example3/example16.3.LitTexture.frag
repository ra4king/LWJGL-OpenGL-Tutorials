#version 330

in vec2 colorCoord;
in vec3 cameraSpacePosition;
in vec3 cameraSpaceNormal;

out vec4 outputColor;

layout(std140) uniform;

struct PerLight
{
	vec4 cameraSpaceLightPos;
	vec4 lightIntensity;
};

uniform Light
{
	vec4 ambientIntensity;
	float lightAttenuation;
	float maxIntensity;
	PerLight lights[4];
} Lgt;

uniform int numberOfLights;

float calcAttenuation(in vec3 cameraSpacePosition, in vec3 cameraSpaceLightPos, out vec3 lightDirection)
{
	vec3 lightDifference = cameraSpaceLightPos - cameraSpacePosition;
	float lightDistanceSqr = dot(lightDifference, lightDifference);
	lightDirection = lightDifference * inversesqrt(lightDistanceSqr);
	
	return 1.0 / (1.0 + Lgt.lightAttenuation * lightDistanceSqr);
}

vec4 computeLighting(in vec4 diffuseColor, in PerLight lightData)
{
	vec3 lightDir;
	vec4 lightIntensity;
	if(lightData.cameraSpaceLightPos.w == 0.0)
	{
		lightDir = vec3(lightData.cameraSpaceLightPos);
		lightIntensity = lightData.lightIntensity;
	}
	else
	{
		float atten = calcAttenuation(cameraSpacePosition, lightData.cameraSpaceLightPos.xyz, lightDir);
		lightIntensity = atten * lightData.lightIntensity;
	}
	
	vec3 surfaceNormal = normalize(cameraSpaceNormal);
	float cosAngIncidence = dot(surfaceNormal, lightDir);
	cosAngIncidence = cosAngIncidence < 0.0001 ? 0.0 : cosAngIncidence;
	
	return diffuseColor * lightIntensity * cosAngIncidence;
}

uniform sampler2D diffuseColorTex;

void main()
{
	vec4 diffuseColor = texture(diffuseColorTex, colorCoord);
	
	vec4 accumLighting = diffuseColor * Lgt.ambientIntensity;
	for(int light = 0; light < numberOfLights; light++)
	{
		accumLighting += computeLighting(diffuseColor, Lgt.lights[light]);
	}
	
	outputColor = accumLighting / Lgt.maxIntensity;
}
