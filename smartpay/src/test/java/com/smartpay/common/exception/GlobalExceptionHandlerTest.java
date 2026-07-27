package com.smartpay.common.exception;

import com.smartpay.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    // ─── Test 14: ResourceNotFoundException returns 404 ────────────────────────
    @Test
    void handleResourceNotFound_shouldReturn404_whenResourceNotFound() {
        // ARRANGE
        ResourceNotFoundException ex = new ResourceNotFoundException("Wallet not found");

        // ACT
        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleResourceNotFound(ex);

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Wallet not found", response.getBody().getMessage());
    }

    // ─── Test 15: IllegalArgumentException returns 400 ─────────────────────────
    @Test
    void handleIllegalArgument_shouldReturn400_whenBadInput() {
        // ARRANGE
        IllegalArgumentException ex = new IllegalArgumentException("Insufficient balance");

        // ACT
        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleIllegalArgument(ex);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Insufficient balance", response.getBody().getMessage());
    }
}