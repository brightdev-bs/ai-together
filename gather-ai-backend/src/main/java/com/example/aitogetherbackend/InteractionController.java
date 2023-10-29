package com.example.aitogetherbackend;

import com.example.aitogetherbackend.email.EmailService;
import com.example.aitogetherbackend.global.constants.Response;
import com.example.aitogetherbackend.global.response.ApiResponse;
import com.example.aitogetherbackend.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InteractionController {

    static class Request {
        String email;
        String script;
        String filename;

        public Request(String email, String script, String filename) {
            this.email = email;
            this.script = script;
            this.filename = filename;
        }
    }

    private final EmailService emaiLService;
    private ExecutorService aiThreadPool = Executors.newSingleThreadExecutor();

    private BlockingQueue<Request> queue = new LinkedBlockingQueue<>();
    private volatile boolean isRunning = false;

    @PostMapping("/interaction")
    public ApiResponse processImage(@RequestPart MultipartFile image,
                                    String email,
                                    @RequestParam(defaultValue = "scribble") String command)
    {
        String filename = saveImage(image);
        String script = generateScript(filename, command);
        queue.add(new Request(email, script, filename));

        if(!isRunning)
            startAiModel();

        return ApiResponse.of(HttpStatus.OK.toString(), Response.SUCCESS.toString());
    }

    private String saveImage(MultipartFile image) {
        String filename;
        try {
            Path path = Paths.get("ai-together/gather-ai-front/public/images/inputs/");
            if(!Files.exists(path)) Files.createDirectory(path);

            filename = FileUtils.generateUniqueFileName();
            Path filepath = path.resolve(filename);
            Files.write(filepath, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filename;
    }

    private void startAiModel() {
        isRunning = true;
        aiThreadPool.execute(() -> {
            try {
                while(!queue.isEmpty()) {
                    Request request = queue.poll();
                    ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", request.script);
                    processBuilder.inheritIO();
                    Process process = processBuilder.start();

                    process.waitFor();
                    emaiLService.sendEmail(request.email, request.filename);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        isRunning = false;
    }

    private String generateScript(String filename, String command) {
        StringBuilder script = new StringBuilder();
        script.append("conda activate control ")
                .append("&& ")
                .append("python ai-together/interaction-ai/scribble2image.py ")
                .append(filename).append(" ")
                .append(command);
        return script.toString();
    }
}
