package com.med.scheduler.infrastructure.ai;

import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public class OpenRouterLiveTest {

    @Disabled("Integration test calling external OpenRouter API")
    @Test
    void testLiveCall() {
        String apiKey = "";
        OpenAiApi api = new OpenAiApi("https://openrouter.ai/api", apiKey);
        OpenAiChatModel chatModel = new OpenAiChatModel(api, OpenAiChatOptions.builder()
                .model("meta-llama/llama-3.1-8b-instruct")
                .build());

        ChatClient.Builder builder = ChatClient.builder(chatModel);
        OpenRouterStructuredExtractionService service = new OpenRouterStructuredExtractionService(builder);

        IntentExtractionResult result = service
                .extractIntentAndParameters("Olá! Gostaria de agendar uma consulta com o Dr. Roberto amanhã às 14:00.");
        System.out.println("RESULTADO OBTIDO: " + result);
    }
}
