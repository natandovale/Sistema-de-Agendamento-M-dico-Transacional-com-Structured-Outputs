package com.med.scheduler.infrastructure.ai.port;

import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;

/**
 * Porta de saída para serviços de extração estruturada de intenção via LLM.
 */
public interface LlmExtractionPort {

    IntentExtractionResult extractIntentAndParameters(String rawMessage);
}
