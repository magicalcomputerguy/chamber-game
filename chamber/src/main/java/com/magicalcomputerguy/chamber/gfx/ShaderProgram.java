package com.magicalcomputerguy.chamber.gfx;

import static org.lwjgl.opengl.GL20.*;

public final class ShaderProgram implements AutoCloseable {
    private final int programId;

    public ShaderProgram(String vsSrc, String fsSrc) {
        int vs = compile(GL_VERTEX_SHADER, vsSrc);
        int fs = compile(GL_FRAGMENT_SHADER, fsSrc);

        programId = glCreateProgram();
        glAttachShader(programId, vs);
        glAttachShader(programId, fs);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            String log = glGetProgramInfoLog(programId);
            throw new IllegalStateException("Shader link failed: " + log);
        }

        glDetachShader(programId, vs);
        glDetachShader(programId, fs);
        glDeleteShader(vs);
        glDeleteShader(fs);
    }

    private static int compile(int type, String src) {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == 0) {
            String log = glGetShaderInfoLog(id);
            throw new IllegalStateException("Shader compile failed: " + log);
        }

        return id;
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int uniformLocation(String name) {
        int loc = glGetUniformLocation(programId, name);
        if (loc < 0) {
            throw new IllegalArgumentException("Uniform not found: " + name);
        }

        return loc;
    }

    @Override
    public void close() throws Exception {
        glDeleteProgram(programId);
    }
}
