package com.smartpay.fraud.engine;

import com.smartpay.common.exception.FraudDetectedException;
import com.smartpay.payment.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.smartpay.fraud.service.FraudAiService;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FraudEngine {
    private final List<FraudRule> rules;
    private final  FraudAiService fraudAiService;
    public void evaluate(PaymentRequest request, String senderEmail) {
        int totalScore = rules.stream()
                .mapToInt(rule -> rule.evaluate(request, senderEmail))
                .sum();

        if (totalScore >= 70) {
            // Get AI explanation (or fallback if Ollama unavailable)
            String explanation = fraudAiService.explain(request, totalScore);
            throw new FraudDetectedException(explanation);
        }
    }

    private String buildReason(int totalScore){
        if(totalScore>=80) return "Unusual transaction velocity detected." ;
        if(totalScore>=70) return "Transaction amount exceeds safe threshold.";
        return "Multiple risk factor detected.";
    }
}
