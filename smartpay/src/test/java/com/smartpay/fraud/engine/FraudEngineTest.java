package com.smartpay.fraud.engine;

import com.smartpay.common.exception.FraudDetectedException;
import com.smartpay.fraud.rules.AmountThreshold;
import com.smartpay.fraud.service.FraudAiService;
import com.smartpay.payment.dto.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudEngineTest {

    // ─── Test 11: Amount above threshold ───────────────────────────────────────
    @Test
    void amountThresholdRule_shouldReturnHighScore_whenAmountExceedsThreshold() {
        // ARRANGE
        AmountThreshold rule = new AmountThreshold();
        PaymentRequest request = new PaymentRequest(
                new BigDecimal("60000"), UUID.randomUUID(), UUID.randomUUID());

        // ACT
        int score = rule.evaluate(request, "priya@test.com");

        // ASSERT — amount > 50000 should return score of 70
        assertEquals(70, score);
    }

    // ─── Test 12: Amount below threshold ───────────────────────────────────────
    @Test
    void amountThresholdRule_shouldReturnZero_whenAmountIsSafe() {
        // ARRANGE
        AmountThreshold rule = new AmountThreshold();
        PaymentRequest request = new PaymentRequest(
                new BigDecimal("500"), UUID.randomUUID(), UUID.randomUUID());

        // ACT
        int score = rule.evaluate(request, "priya@test.com");

        // ASSERT — safe amount should return score of 0
        assertEquals(0, score);
    }

    // ─── Test 13: FraudEngine throws when score >= 70 ──────────────────────────
    @Test
    void fraudEngine_shouldThrowFraudDetectedException_whenScoreExceedsThreshold() {
        // ARRANGE
        FraudRule highScoreRule = mock(FraudRule.class);
        when(highScoreRule.evaluate(any(), anyString())).thenReturn(70);

        FraudAiService fraudAiService = mock(FraudAiService.class);
        when(fraudAiService.explain(any(), anyInt()))
                .thenReturn("High amount detected — transaction blocked.");

        FraudEngine fraudEngine = new FraudEngine(List.of(highScoreRule), fraudAiService);

        PaymentRequest request = new PaymentRequest(
                new BigDecimal("60000"), UUID.randomUUID(), UUID.randomUUID());

        // ACT + ASSERT
        assertThrows(FraudDetectedException.class, () ->
                fraudEngine.evaluate(request, "priya@test.com"));
    }
}