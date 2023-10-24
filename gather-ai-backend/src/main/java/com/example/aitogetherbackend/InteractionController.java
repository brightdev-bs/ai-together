package com.example.aitogetherbackend;

import com.example.aitogetherbackend.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/interaction")
    public void processImage(@RequestPart MultipartFile image) {

        String filename = saveImage(image);

        // 비동기 코드 작성 -> 인공지능 모델 구동
        URI getTestUri =
        WebClient.create()
                .get()
                .url()
    }

    private String saveImage(MultipartFile image) {
        String filename;
        try {
            Path path = Paths.get("images");
            if(!Files.exists(path)) Files.createDirectory(path);

            filename = FileUtils.generateUniqueFileName();
            Path filepath = path.resolve(filename);
            Files.write(filepath, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filename;
    }
}
