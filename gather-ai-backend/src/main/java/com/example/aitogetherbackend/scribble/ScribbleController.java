package com.example.aitogetherbackend.scribble;

import com.example.aitogetherbackend.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Slf4j
@RestController
public class ScribbleController {

    private final ScribbleService scribbleService;

    @GetMapping("/connect")
    public ResponseEntity<SseEmitter> readyProcess(String key)  {
        SseEmitter emitter = scribbleService.ready(key);
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            ScribbleService.emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            log.info("complete");
            emitter.complete();
        });
        return ResponseEntity.ok(emitter);
    }

    @PostMapping("/scribble/process")
    public void startProcess(String key, @RequestPart MultipartFile image, @RequestParam(defaultValue = "scribble") String command) {
        String filename = saveImage(image);
        scribbleService.startProcess(key, filename, command);
    }

    private String saveImage(MultipartFile image) {
        String filename;
        try {
            Path path = Paths.get("ai-together/interaction-ai/images/inputs/");
            if (!Files.exists(path)) Files.createDirectory(path);

            filename = FileUtils.generateUniqueFileName() + ".png";
            Path filepath = path.resolve(filename);
            Files.write(filepath, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filename;
    }
}

