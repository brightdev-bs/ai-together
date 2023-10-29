package com.example.aitogetherbackend.global.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

@Getter
@Builder
public class ApiResponse {

    private final String statusCode;
    private final Object response;

    private ApiResponse(String statusCode, Object response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public static ApiResponse of(String statusCode, Object data) {
        return ApiResponse.builder()
                .statusCode(statusCode)
                .response(data)
                .build();
    }

    public static ApiResponse of(String statusCode, BindingResult bindingResult) {
        return ApiResponse.builder()
                .statusCode(statusCode)
                .response(createErrorMessage(bindingResult))
                .build();
    }

    private static String createErrorMessage(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            if(!isFirst) {
                sb.append(", ");
            } else {
                isFirst = false;
            }
            sb.append("[");
            sb.append(fieldError.getField());
            sb.append("] ");
            sb.append(fieldError.getDefaultMessage());
        }

        return sb.toString();
    }
}