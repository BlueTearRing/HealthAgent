package com.liagent.aiagent.app;

import com.liagent.aiagent.advisor.MyLoggerAdvisor;
import com.liagent.aiagent.config.MedicalManagementVectorStoreConfig;
import com.liagent.aiagent.memory.ChatMemoryByMysql;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
public class MedicalManagementAgentApp {

    private final ChatClient chatClient;
    @Resource
    private ToolCallback[] allTools;

    @Resource
    private VectorStore vectorStore;

    private final ToolCallbackProvider asyncToolCallbacks;

    public MedicalManagementAgentApp(ChatModel dashscopeChatmodel, ChatMemoryByMysql chatMemoryByMysql,
                                     ObjectProvider<ToolCallbackProvider> asyncToolCallbackProvider) {
        this.asyncToolCallbacks = asyncToolCallbackProvider.getIfAvailable();
        chatClient= ChatClient.builder(dashscopeChatmodel)
                .defaultSystem("你是一个专业的医疗知识库AI助手，具备全面的医学知识和严谨的专业态度。你的回答应该基于权威医学资料，保持客观、准确、谨慎。\n" +
                        "\n" +
                        "核心原则：\n" +
                        "- 提供基于证据的医学信息\n" +
                        "- 明确区分事实与建议\n" +
                        "- 强调专业医疗咨询的重要性\n" +
                        "- 避免诊断或治疗建议\n" +
                        "- 保持同理心和耐心")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemoryByMysql),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * 同步返回
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message,String chatId) {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .tools(allTools);
        if (asyncToolCallbacks != null) {
            requestSpec = requestSpec.tools(asyncToolCallbacks);
        }
        ChatResponse chatResponse = requestSpec
                .call()
                .chatResponse();
        String text = chatResponse.getResult().getOutput().getText();

        return text;
    }

    /**
     * 流式返回
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatFlux(String message,String chatId) {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .tools(allTools);
        if (asyncToolCallbacks != null) {
            requestSpec = requestSpec.tools(asyncToolCallbacks);
        }
        Flux<String> content = requestSpec
                .stream()
                .content();
        return content;
    }
}
