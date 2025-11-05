package com.xiaohunao.oxygen_not_included.client.noise;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NoiseDataGenerator {
    public static void main(String[] args) {
        final int SIZE = 64;
        final float FREQUENCY = 1.0f;

        ByteBuffer buffer = ByteBuffer.allocate(SIZE * SIZE * SIZE);
        buffer.order(ByteOrder.nativeOrder());

        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {

                    float fx = (float)x / (SIZE - 1) * FREQUENCY;
                    float fy = (float)y / (SIZE - 1) * FREQUENCY;
                    float fz = (float)z / (SIZE - 1) * FREQUENCY;

                    float noiseVal = GLSLNoise.fbm(new GLSLNoise.Vec.Vec3(fx, fy, fz));

                    byte byteVal = (byte) (noiseVal * 255.0f);
                    buffer.put(byteVal);
                }
            }
        }

        buffer.flip();

        try (FileOutputStream fos = new FileOutputStream("noise.raw")) {
            fos.getChannel().write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
