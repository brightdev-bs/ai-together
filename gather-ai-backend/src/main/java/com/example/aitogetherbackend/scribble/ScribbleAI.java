package com.example.aitogetherbackend.scribble;

public class ScribbleAI implements Runnable {

    private volatile boolean isRunning = false;

    @Override
    public void run() {
        isRunning = true;




        isRunning = false;
    }
}
