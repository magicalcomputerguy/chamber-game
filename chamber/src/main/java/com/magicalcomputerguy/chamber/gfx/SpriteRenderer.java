package com.magicalcomputerguy.chamber.gfx;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public final class SpriteRenderer implements AutoCloseable {

    private final int vao;
    private final int vbo;
    private final ShaderProgram shader;

    private final int uProj;
    private final int uTex;

    // 2D quad: pos(x,y) + uv(u,v) -> 4 vertices (triangle strip)
    private final FloatBuffer vertexBuf = BufferUtils.createFloatBuffer(4 * 4);
    private final FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
    private final Matrix4f proj = new Matrix4f();

    public SpriteRenderer() {
        String vs = """
            #version 330 core
            layout (location = 0) in vec2 aPos;
            layout (location = 1) in vec2 aUv;

            uniform mat4 uProj;

            out vec2 vUv;

            void main() {
                vUv = aUv;
                gl_Position = uProj * vec4(aPos.xy, 0.0, 1.0);
            }
        """;

        String fs = """
            #version 330 core
            in vec2 vUv;
            uniform sampler2D uTex;
            out vec4 FragColor;

            void main() {
                FragColor = texture(uTex, vUv);
            }
        """;

        shader = new ShaderProgram(vs, fs);
        uProj = shader.uniformLocation("uProj");
        uTex  = shader.uniformLocation("uTex");

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferData(GL_ARRAY_BUFFER, 4L * 4L * Float.BYTES, GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    /** call once per frame before draw() */
    public void begin(int screenW, int screenH) {
        shader.bind();

        proj.identity().ortho2D(0, screenW, 0, screenH);
        proj.get(matBuf.clear());
        glUniformMatrix4fv(uProj, false, matBuf);

        glUniform1i(uTex, 0);

        glBindVertexArray(vao);
    }

    public void draw(Texture tex, float x, float y, float w, float h) {
        // Note: OpenGL ortho2D here uses bottom-left origin.
        // If you want top-left origin later, we’ll flip the projection.
        tex.bind(0);

        float x0 = x,     y0 = y;
        float x1 = x + w, y1 = y + h;

        // triangle strip order: (x0,y0) (x1,y0) (x0,y1) (x1,y1)
        vertexBuf.clear();
        putVertex(x0, y0, 0f, 0f);
        putVertex(x1, y0, 1f, 0f);
        putVertex(x0, y1, 0f, 1f);
        putVertex(x1, y1, 1f, 1f);
        vertexBuf.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuf);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    public void end() {
        glBindVertexArray(0);
        shader.unbind();
    }

    private void putVertex(float x, float y, float u, float v) {
        vertexBuf.put(x).put(y).put(u).put(v);
    }

    @Override
    public void close() throws Exception {
        shader.close();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}

