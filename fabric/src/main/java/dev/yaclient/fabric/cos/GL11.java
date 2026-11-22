package dev.yaclient.fabric.cos;

/** Shim: the cosmetic code was written for OpenGL 1.x; none of this is needed any more. */
final class GL11 {
    static final int GL_TEXTURE_2D = 0, GL_TEXTURE_MIN_FILTER = 1, GL_TEXTURE_MAG_FILTER = 2, GL_TEXTURE_WRAP_S = 3, GL_TEXTURE_WRAP_T = 4,
            GL_LINEAR = 5, GL_NEAREST = 6, GL_SRC_ALPHA = 7, GL_ONE_MINUS_SRC_ALPHA = 8, GL_ONE = 9, GL_ZERO = 10, GL_QUADS = 11, GL_SMOOTH = 12,
            GL_LEQUAL = 13, GL_DEPTH_TEST = 14, GL_DEPTH_BUFFER_BIT = 15, GL_TRIANGLE_FAN = 16;

    private GL11() {}

    static void glTexParameteri(int a, int b, int c) {}
}
