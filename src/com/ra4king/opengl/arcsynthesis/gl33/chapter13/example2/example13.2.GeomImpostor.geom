#version 330

layout(std140) uniform;
layout(points) in;
layout(triangle_strip, max_vertices=4) out;

uniform Projection
{
	mat4 cameraToClipMatrix;
};

in VertexData
{
	vec3 cameraSpherePos;
	float sphereRadius;
} vert[];

out FragData
{
	flat vec3 cameraSpherePos;
	flat float sphereRadius;
	smooth vec2 mapping;
};

const float g_boxCorrection = 1.5;

void calculatePosition(in vec2 pos, out vec4 position, out int id) {
	cameraSpherePos = vec3(vert[0].cameraSpherePos);
	sphereRadius = vert[0].sphereRadius;
	mapping = pos * g_boxCorrection;
	
	vec4 cameraCornerPos = vec4(vert[0].cameraSpherePos, 1.0);
	cameraCornerPos.xy += vec2(vert[0].sphereRadius, vert[0].sphereRadius) * mapping;
	position = cameraToClipMatrix * cameraCornerPos;
	id = gl_PrimitiveIDIn;
}

void main()
{
	calculatePosition(vec2(-1.0, -1.0), gl_Position, gl_PrimitiveID);
	EmitVertex();
	
	calculatePosition(vec2(-1.0, 1.0), gl_Position, gl_PrimitiveID);
	EmitVertex();
	
	calculatePosition(vec2(1.0, -1.0), gl_Position, gl_PrimitiveID);
	EmitVertex();
	
	calculatePosition(vec2(1.0, 1.0), gl_Position, gl_PrimitiveID);
	EmitVertex();
}
