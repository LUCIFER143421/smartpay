package com.smartpay.fraud.service;

import com.smartpay.payment.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FraudAiService {

    private final ChatClient chatClient;

    public String explain(PaymentRequest request, int fraudScore) {
        try {
            // Build a prompt describing the suspicious transaction
            String prompt = String.format(
                    """
                    You are a fraud detection AI for a payment system called SmartPay.
                    Analyze this transaction and explain in 2-3 sentences why it appears suspicious:
                    
                    Transaction Amount: %s INR
                    Fraud Score: %d out of 100
                    (Score >= 70 is considered fraudulent. 70 = high amount, 80 = velocity attack)
                    
                    Be concise and professional. Focus on the risk factors.
                    """,
                    request.getAmount(),
                    fraudScore
            );

            // Send prompt to Ollama running locally, get plain English response
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception e) {
            // Ollama not available (e.g. production deployment)
            // Fall back to rule-based explanation — no crash, no broken payment flow
            return buildFallbackExplanation(fraudScore);
        }
    }

    private String buildFallbackExplanation(int fraudScore) {
        if (fraudScore >= 80) {
            return "Transaction blocked: unusual payment velocity detected. " +
                    "Multiple payments in a short time window suggest automated or compromised account activity.";
        }
        if (fraudScore >= 70) {
            return "Transaction blocked: amount significantly exceeds normal transaction threshold. " +
                    "Large transfers require additional verification.";
        }
        return "Transaction blocked: multiple risk factors detected in this transaction.";
    }
}