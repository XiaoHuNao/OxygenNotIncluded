package com.xiaohunao.oxygen_not_included.client.noise;

public class GLSLNoise {

    private static float mod289(float x) {
        return x - (float)Math.floor(x * (1.0f / 289.0f)) * 289.0f;
    }
    private static Vec.Vec3 mod289(Vec.Vec3 x) {
        return new Vec.Vec3(mod289(x.x), mod289(x.y), mod289(x.z));
    }

    private static Vec.Vec4 mod289(Vec.Vec4 x) {
        return new Vec.Vec4(mod289(x.x), mod289(x.y), mod289(x.z), mod289(x.w));
    }

    private static Vec.Vec4 permute(Vec.Vec4 x) {
        float x_ = mod289(((x.x * 34.0f) + 1.0f) * x.x);
        float y_ = mod289(((x.y * 34.0f) + 1.0f) * x.y);
        float z_ = mod289(((x.z * 34.0f) + 1.0f) * x.z);
        float w_ = mod289(((x.w * 34.0f) + 1.0f) * x.w);
        return new Vec.Vec4(x_, y_, z_, w_);
    }

    private static Vec.Vec4 taylorInvSqrt(Vec.Vec4 r) {
        return new Vec.Vec4(
                1.79284291400159f - 0.85373472095314f * r.x,
                1.79284291400159f - 0.85373472095314f * r.y,
                1.79284291400159f - 0.85373472095314f * r.z,
                1.79284291400159f - 0.85373472095314f * r.w
        );
    }

    public static float snoise(Vec.Vec3 v) {
        final Vec.Vec2 C = new Vec.Vec2(1.0f/6.0f, 1.0f/3.0f);
        final Vec.Vec4 D = new Vec.Vec4(0.0f, 0.5f, 1.0f, 2.0f);

        Vec.Vec3 i = new Vec.Vec3(
                (float)Math.floor(v.x + (v.x + v.y + v.z) * C.y),
                (float)Math.floor(v.y + (v.x + v.y + v.z) * C.y),
                (float)Math.floor(v.z + (v.x + v.y + v.z) * C.y)
        );
        float dot_i_Cxxx = (i.x + i.y + i.z) * C.x;
        Vec.Vec3 x0 = new Vec.Vec3(v.x - i.x + dot_i_Cxxx, v.y - i.y + dot_i_Cxxx, v.z - i.z + dot_i_Cxxx);

        Vec.Vec3 g = new Vec.Vec3(x0.y >= x0.x ? 1.0f : 0.0f, x0.z >= x0.y ? 1.0f : 0.0f, x0.x >= x0.z ? 1.0f : 0.0f);
        Vec.Vec3 l = new Vec.Vec3(1.0f - g.x, 1.0f - g.y, 1.0f - g.z);
        Vec.Vec3 i1 = new Vec.Vec3(Math.min(g.x, l.z), Math.min(g.y, l.x), Math.min(g.z, l.y));
        Vec.Vec3 i2 = new Vec.Vec3(Math.max(g.x, l.z), Math.max(g.y, l.x), Math.max(g.z, l.y));

        Vec.Vec3 x1 = new Vec.Vec3(x0.x - i1.x + C.x, x0.y - i1.y + C.x, x0.z - i1.z + C.x);
        Vec.Vec3 x2 = new Vec.Vec3(x0.x - i2.x + C.y, x0.y - i2.y + C.y, x0.z - i2.z + C.y);
        Vec.Vec3 x3 = new Vec.Vec3(x0.x - D.y, x0.y - D.y, x0.z - D.y);

        i = mod289(i);
        Vec.Vec4 p = permute(permute(permute(
                new Vec.Vec4(i.z, i.z + i1.z, i.z + i2.z, i.z + 1.0f))
                .add(new Vec.Vec4(i.y, i.y + i1.y, i.y + i2.y, i.y + 1.0f)))
                .add(new Vec.Vec4(i.x, i.x + i1.x, i.x + i2.x, i.x + 1.0f))
        );

        float n_ = 0.142857142857f;
        Vec.Vec3 ns = new Vec.Vec3(n_ * D.w - D.x, n_ * D.y - D.z, n_ * D.z - D.x);

        Vec.Vec4 j = p.sub(49.0f * (float)Math.floor(p.x * ns.z * ns.z), 49.0f * (float)Math.floor(p.y * ns.z * ns.z), 49.0f * (float)Math.floor(p.z * ns.z * ns.z), 49.0f * (float)Math.floor(p.w * ns.z * ns.z));
        Vec.Vec4 x_ = j.mul(ns.z).floor();
        Vec.Vec4 y_ = j.sub(x_.mul(7.0f)).floor();

        Vec.Vec4 x = x_.mul(ns.x).add(ns.y);
        Vec.Vec4 y = y_.mul(ns.x).add(ns.y);
        Vec.Vec4 h = new Vec.Vec4(1.0f).sub(x.abs()).sub(y.abs());

        Vec.Vec4 b0 = new Vec.Vec4(x.x, x.y, y.x, y.y);
        Vec.Vec4 b1 = new Vec.Vec4(x.z, x.w, y.z, y.w);

        Vec.Vec4 s0 = b0.floor().mul(2.0f).add(1.0f);
        Vec.Vec4 s1 = b1.floor().mul(2.0f).add(1.0f);
        Vec.Vec4 sh = h.step(0.0f).mul(-1.0f);

        Vec.Vec4 a0 = new Vec.Vec4(b0.x, b0.z, b0.y, b0.w).add(new Vec.Vec4(s0.x, s0.z, s0.y, s0.w).mul(new Vec.Vec4(sh.x, sh.x, sh.y, sh.y)));
        Vec.Vec4 a1 = new Vec.Vec4(b1.x, b1.z, b1.y, b1.w).add(new Vec.Vec4(s1.x, s1.z, s1.y, s1.w).mul(new Vec.Vec4(sh.z, sh.z, sh.w, sh.w)));

        Vec.Vec3 p0 = new Vec.Vec3(a0.x, a0.y, h.x);
        Vec.Vec3 p1 = new Vec.Vec3(a0.z, a0.w, h.y);
        Vec.Vec3 p2 = new Vec.Vec3(a1.x, a1.y, h.z);
        Vec.Vec3 p3 = new Vec.Vec3(a1.z, a1.w, h.w);

        Vec.Vec4 norm = taylorInvSqrt(new Vec.Vec4(p0.dot(p0), p1.dot(p1), p2.dot(p2), p3.dot(p3)));
        p0 = p0.mul(norm.x);
        p1 = p1.mul(norm.y);
        p2 = p2.mul(norm.z);
        p3 = p3.mul(norm.w);

        Vec.Vec4 m = new Vec.Vec4(
                x0.dot(x0),
                x1.dot(x1),
                x2.dot(x2),
                x3.dot(x3)
        ).mul(-1.0f).add(0.6f).max(0.0f);
        m = m.mul(m);
        m = m.mul(m);

        return 42.0f * new Vec.Vec4(
                p0.dot(x0),
                p1.dot(x1),
                p2.dot(x2),
                p3.dot(x3)
        ).dot(m);
    }

    public static float fbm(Vec.Vec3 pos) {
        int OCTAVES = 3;
        float total = 0.0f;
        float frequency = 1.0f;
        float amplitude = 0.5f;
        float maxValue = 0.0f;
        for (int i = 0; i < OCTAVES; i++) {
            total += snoise(pos.mul(frequency)) * amplitude;
            maxValue += amplitude;
            amplitude *= 0.5f;
            frequency *= 2.0f;
        }
        return (total / maxValue + 1.0f) / 2.0f;
    }


    static class Vec {
        public static class Vec2 {
            public float x, y;
            public Vec2(float x, float y) { this.x = x; this.y = y; }
        }

        public static class Vec3 {
            public float x, y, z;
            public Vec3(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
            public float dot(Vec3 v) { return this.x * v.x + this.y * v.y + this.z * v.z; }
            public Vec3 mul(float f) { return new Vec3(x * f, y * f, z * f); }
        }

        public static class Vec4 {
            public float x, y, z, w;
            public Vec4(float v) { this.x = v; this.y = v; this.z = v; this.w = v; }
            public Vec4(float x, float y, float z, float w) { this.x = x; this.y = y; this.z = z; this.w = w; }

            public Vec4 add(Vec4 v) { return new Vec4(x + v.x, y + v.y, z + v.z, w + v.w); }
            public Vec4 add(float f) { return new Vec4(x + f, y + f, z + f, w + f); }
            public Vec4 sub(Vec4 v) { return new Vec4(x - v.x, y - v.y, z - v.z, w - v.w); }
            public Vec4 sub(float f) { return new Vec4(x - f, y - f, z - f, w - f); }
            public Vec4 sub(float f1, float f2, float f3, float f4) { return new Vec4(x-f1, y-f2, z-f3, w-f4); }
            public Vec4 mul(Vec4 v) { return new Vec4(x * v.x, y * v.y, z * v.z, w * v.w); }
            public Vec4 mul(float f) { return new Vec4(x * f, y * f, z * f, w * f); }
            public Vec4 floor() { return new Vec4((float)Math.floor(x), (float)Math.floor(y), (float)Math.floor(z), (float)Math.floor(w)); }
            public Vec4 abs() { return new Vec4(Math.abs(x), Math.abs(y), Math.abs(z), Math.abs(w)); }
            public Vec4 max(float f) { return new Vec4(Math.max(x, f), Math.max(y, f), Math.max(z, f), Math.max(w, f)); }
            public float dot(Vec4 v) { return this.x * v.x + this.y * v.y + this.z * v.z + this.w * v.w; }
            public Vec4 step(float edge) {
                return new Vec4(x >= edge ? 1.0f : 0.0f, y >= edge ? 1.0f : 0.0f, z >= edge ? 1.0f : 0.0f, w >= edge ? 1.0f : 0.0f);
            }
        }
    }
}