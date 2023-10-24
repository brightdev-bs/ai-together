package com.example.aitogetherbackend.global.utils;

import java.util.UUID;

public class FileUtils {

    public static String generateUniqueFileName() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
