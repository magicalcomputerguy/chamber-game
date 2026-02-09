package com.magicalcomputerguy.chamber.gfx;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Texture implements AutoCloseable {
    public final int id;
    public final int width;
    public final int height;

    private Texture(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public static Texture load(String classpathPath) {
        ByteBuffer image;
        int w, h;

        STBImage.stbi_set_flip_vertically_on_load(true);

        try (MemoryStack stack = stackPush()) {
            IntBuffer pw = stack.mallocInt(1);
            IntBuffer ph = stack.mallocInt(1);
            IntBuffer pc = stack.mallocInt(1);

            ByteBuffer fileData = Resource.readToBuffer(classpathPath);
            image = STBImage.stbi_load_from_memory(fileData, pw, ph, pc, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load image " + classpathPath + ": " + STBImage.stbi_failure_reason());
            }

            w = pw.get(0);
            h = ph.get(0);
        }

        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

        STBImage.stbi_image_free(image);

        return new Texture(texId, w, h);
    }

    public void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    @Override
    public void close() throws Exception {
        glDeleteTextures(id);
    }

    static final class Resource {
        static ByteBuffer readToBuffer(String path) {
            try (var in = Texture.class.getResourceAsStream(path)) {
                if (in == null) {
                    throw new IOException("Resource not found: " + path);
                }

                byte[] bytes = in.readAllBytes();
                ByteBuffer buf = org.lwjgl.BufferUtils.createByteBuffer(bytes.length);
                buf.put(bytes).flip();

                return buf;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
