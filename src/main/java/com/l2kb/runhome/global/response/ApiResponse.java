package com.l2kb.runhome.global.response;

public record ApiResponse<T>(int status, String message, T payload) {

    public static <T> ApiResponse<T> ok(T payload) {
        return new ApiResponse<>(200, "success", payload);
    }

    public static <T> ApiResponse<T> created(T payload) {
        return new ApiResponse<>(201, "created", payload);
    }

    public static ApiResponse<Void> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
