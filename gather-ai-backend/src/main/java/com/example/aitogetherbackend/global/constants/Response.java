package com.example.aitogetherbackend.global.constants;

public enum Response {

    SUCCESS("성공"), FAIL("실패");

    String message;

    Response(String message) {
        this.message = message;
    }
}
