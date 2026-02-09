package com.magicalcomputerguy.chamber;

import com.magicalcomputerguy.chamber.gfx.SpriteRenderer;
import com.magicalcomputerguy.chamber.gfx.Texture;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.*;

public class Main {
    private long window;
    private int width = 800;
    private int height = 600;

    private SpriteRenderer spriteRenderer;
    private Texture playerTex;

    public void run() {
        initWindow();
        initGL();
        initGame();
        loop();
        cleanup();
    }

    private void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to init GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // For Mac OS
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Chamber 1", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create game window");
        }

        glfwSetFramebufferSizeCallback(window, (w, newW, newH) -> {
            width = newW;
            height = newH;
            glViewport(0, 0, width, height);
        });

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // V-Sync

        int[] fbW = new int[1];
        int[] fbH = new int[1];
        glfwGetFramebufferSize(window, fbW, fbH);
        width = fbW[0];
        height = fbH[0];

        glfwShowWindow(window);
    }

    private void initGL() {
        GL.createCapabilities();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(0.08f, 0.08f, 0.1f, 1.0f);
        glViewport(0, 0, width, height);
    }

    private void initGame() {
        spriteRenderer = new SpriteRenderer();
        playerTex = Texture.load("/assets/player.png");
    }

    private void loop() {
        double last = glfwGetTime();
        double acc = 0.0;
        double dt = 1.0 / 60.0;

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            double frame = now - last;
            last = now;
            acc += frame;

            while (acc >= dt) {
                acc -= dt;
            }

            render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT);

        // oyuncuyu ekranın ortasına çiz (pixel coords)
        float x = (width / 2f) - 24;
        float y = (height / 2f) - 24;
        spriteRenderer.begin(width, height);
        spriteRenderer.draw(playerTex, x, y, 24, 24);
        spriteRenderer.end();
    }

    private void cleanup() {
        glfwDestroyWindow(window);
        glfwTerminate();
        var cb = glfwSetErrorCallback(null);
        if (cb != null) {
            cb.free();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
