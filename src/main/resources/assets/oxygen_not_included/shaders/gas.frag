#version 400 core

in vec3 v_WorldPos;
uniform vec3 u_CameraPos;
uniform vec3 u_VolumeMin;
uniform vec3 u_VolumeMax;
uniform float u_Time;
uniform float u_Density;
uniform vec4 u_GasColor;
out vec4 out_Color;
uniform sampler3D u_NoiseTex;
vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 permute(vec4 x) { return mod289(((x*34.0)+1.0)*x); }
vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }
float snoise(vec3 v) {
    const vec2 C = vec2(1.0/6.0, 1.0/3.0); const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);
    vec3 i = floor(v + dot(v, C.yyy)); vec3 x0 = v - i + dot(i, C.xxx);
    vec3 g = step(x0.yzx, x0.xyz); vec3 l = 1.0 - g; vec3 i1 = min(g.xyz, l.zxy); vec3 i2 = max(g.xyz, l.zxy);
    vec3 x1 = x0 - i1 + C.xxx; vec3 x2 = x0 - i2 + C.yyy; vec3 x3 = x0 - D.yyy;
    i = mod289(i);
    vec4 p = permute(permute(permute(i.z + vec4(0.0, i1.z, i2.z, 1.0)) + i.y + vec4(0.0, i1.y, i2.y, 1.0)) + i.x + vec4(0.0, i1.x, i2.x, 1.0));
    float n_ = 0.142857142857; vec3 ns = n_ * D.wyz - D.xzx;
    vec4 j = p - 49.0 * floor(p * ns.z * ns.z); vec4 x_ = floor(j * ns.z); vec4 y_ = floor(j - 7.0 * x_);
    vec4 x = x_ * ns.x + ns.yyyy; vec4 y = y_ * ns.x + ns.yyyy; vec4 h = 1.0 - abs(x) - abs(y);
    vec4 b0 = vec4(x.xy, y.xy); vec4 b1 = vec4(x.zw, y.zw);
    vec4 s0 = floor(b0)*2.0 + 1.0; vec4 s1 = floor(b1)*2.0 + 1.0; vec4 sh = -step(h, vec4(0.0));
    vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy; vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww;
    vec3 p0 = vec3(a0.xy,h.x); vec3 p1 = vec3(a0.zw,h.y); vec3 p2 = vec3(a1.xy,h.z); vec3 p3 = vec3(a1.zw,h.w);
    vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2,p2), dot(p3,p3)));
    p0 *= norm.x; p1 *= norm.y; p2 *= norm.z; p3 *= norm.w;
    vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0); m = m * m;
    return 42.0 * dot(m*m, vec4(dot(p0,x0), dot(p1,x1), dot(p2,x2), dot(p3,x3)));
}
float fbm(vec3 pos) {
    int OCTAVES = 3;
    float total = 0.0; float frequency = 1.0; float amplitude = 0.5; float maxValue = 0.0;
    for (int i = 0; i < OCTAVES; i++) {
        total += snoise(pos * frequency) * amplitude;
        maxValue += amplitude; amplitude *= 0.5; frequency *= 2.0;
    }
    return (total / maxValue + 1.0) / 2.0;
}
vec2 intersectAABB(vec3 rayOrigin, vec3 rayDir, vec3 boxMin, vec3 boxMax) {
    vec3 tMin = (boxMin - rayOrigin) / rayDir;
    vec3 tMax = (boxMax - rayOrigin) / rayDir;
    vec3 t1 = min(tMin, tMax);
    vec3 t2 = max(tMin, tMax);
    float tNear = max(max(t1.x, t1.y), t1.z);
    float tFar = min(min(t2.x, t2.y), t2.z);
    return vec2(tNear, tFar);
}
void main() {
    vec3 rayDir = normalize(v_WorldPos - u_CameraPos);
    vec2 hit = intersectAABB(u_CameraPos, rayDir, u_VolumeMin, u_VolumeMax);
    float tNear = hit.x;
    float tFar = hit.y;
    if (tNear >= tFar) {
        discard;
    }
    tNear = max(tNear, 0.0);
    vec3 rayStart = u_CameraPos + rayDir * tNear;
    vec3 rayEnd = u_CameraPos + rayDir * tFar;
    float rayLength = distance(rayStart, rayEnd);
    int maxSteps = 16;
    float stepSize = rayLength / float(maxSteps);
    vec4 accumulatedColor = vec4(0.0);
    vec3 currentPos = rayStart;
    vec3 boxCenter = (u_VolumeMin + u_VolumeMax) * 0.5;
    vec3 boxSize = u_VolumeMax - u_VolumeMin;
    vec3 invRadius = 2.0 / boxSize;
    for (int i = 0; i < maxSteps; i++) {
        vec3 relativePos = (currentPos - boxCenter) * invRadius;
        float distSq = dot(relativePos, relativePos);
        float baseDensity = 1.0 - smoothstep(0.8, 1.5, distSq);
        baseDensity = clamp(baseDensity, 0.0, 1.0);
        vec3 noiseCoord = currentPos * u_Density + vec3(0.0, 0.0, u_Time * 0.2);
        float noiseDensity = texture(u_NoiseTex, noiseCoord).r;
        float finalDensity = baseDensity * noiseDensity;
        if (finalDensity > 0.01) {
            float concentration = 0.1;
            float absorption = 1.5;
            vec4 stepColor = u_GasColor;
            stepColor.a *= finalDensity * concentration * stepSize * absorption;
            accumulatedColor.rgb += (1.0 - accumulatedColor.a) * stepColor.rgb * stepColor.a;
            accumulatedColor.a += (1.0 - accumulatedColor.a) * stepColor.a;
        }
        currentPos += rayDir * stepSize;
        if (accumulatedColor.a > 0.99) {
            break;
        }
    }
    out_Color.a = accumulatedColor.a;
    if (out_Color.a > 0.0001) {
        out_Color.rgb = accumulatedColor.rgb / out_Color.a;
    } else {
        out_Color.rgb = vec3(0.0);
    }
}