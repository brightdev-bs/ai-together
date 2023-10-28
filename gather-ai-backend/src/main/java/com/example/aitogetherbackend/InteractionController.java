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

@Slf4j
@RestController
@RequiredArgsConstructor
public class InteractionController {

    private final EmailService emaiLService;

    @PostMapping("/interaction")
    public ApiResponse processImage(@RequestPart MultipartFile image,
                                    String email,
                                    @RequestParam(defaultValue = "scribble") String command)
    {
        String filename = saveImage(image);
        startAiModel(command, filename);
        emaiLService.sendEmail(email, filename);
        return ApiResponse.of(HttpStatus.OK.toString(), Response.SUCCESS.toString());
    }

    private String saveImage(MultipartFile image) {
        String filename;
        try {
            Path path = Paths.get("ai-together/interaction-ai/images/inputs/");
            if(!Files.exists(path)) Files.createDirectory(path);

            filename = FileUtils.generateUniqueFileName();
            Path filepath = path.resolve(filename);
            Files.write(filepath, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filename;
    }

    private void startAiModel(String command, String filename) {
        try {
            StringBuilder script = generateScript(filename, command);
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", script.toString());
            processBuilder.inheritIO();
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
