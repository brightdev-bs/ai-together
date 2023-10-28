package com.example.aitogetherbackend.scribble;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ScribbleService {

    public static ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter ready(String key) {
        SseEmitter emitter = new SseEmitter();
        emitters.put(key, emitter);
        return emitter;
    }

    public void startProcess(String key, String filename, String command) {
        SseEmitter sseEmitter = emitters.get(key);
        try {
            StringBuilder script = generateScript(filename, command);
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", script.toString());
//            processBuilder.inheritIO();
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sseEmitter.send(SseEmitter.event()
                        .name("stage")
                        .data(line)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sseEmitter != null) {
                log.info("sseEmitter remove");
                sseEmitter.complete();
                emitters.remove(key);
            }
        }
    }

    private StringBuilder generateScript(String filename, String command) {
        StringBuilder script = new StringBuilder();
        script.append("conda activate control ")
                .append("&& ")
                .append("python ai-together/interaction-ai/scribble2image.py ")
                .append(filename).append(" ")
                .append(command);
        return script;
    }
}
