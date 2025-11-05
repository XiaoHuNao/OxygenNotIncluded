#version 400 core
layout (location = 0) in vec3 in_Position;
uniform mat4 u_ModelViewMatrix;
uniform mat4 u_ProjectionMatrix;
uniform vec3 u_VolumeMin;
uniform vec3 u_VolumeMax;
out vec3 v_WorldPos;
void main() {
    gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(in_Position, 1.0);
    vec3 volumeSize = u_VolumeMax - u_VolumeMin;
    v_WorldPos = u_VolumeMin + (in_Position * volumeSize);
}