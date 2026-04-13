package com.liagent.aiagent.memory;

import com.liagent.aiagent.entity.ChatMessage;
import com.liagent.aiagent.mapper.ChatMessageMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ChatMemoryByMysql implements ChatMemory {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        for (Message message : messages) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setConversationId(conversationId);
            chatMessage.setContent(message.getText());
            
            // 根据消息类型设置messageType
            if (message instanceof UserMessage) {
                chatMessage.setMessageType("user");
            } else if (message instanceof AssistantMessage) {
                chatMessage.setMessageType("assistant");
            } else if (message instanceof SystemMessage) {
                chatMessage.setMessageType("system");
            } else {
                chatMessage.setMessageType("unknown");
            }
            
            chatMessageMapper.insert(chatMessage);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        if (lastN <= 0) {
            return new ArrayList<>();
        }
        
        List<ChatMessage> chatMessages = chatMessageMapper.getRecentMessages(conversationId, lastN);
        if (chatMessages == null || chatMessages.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 由于查询是按时间倒序的，需要反转列表以保持时间正序
        Collections.reverse(chatMessages);
        
        List<Message> messages = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            Message message = convertToMessage(chatMessage);
            if (message != null) {
                messages.add(message);
            }
        }
        
        return messages;
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        chatMessageMapper.deleteByConversationId(conversationId);
    }
    
    /**
     * 将ChatMessage转换为Spring AI的Message对象
     */
    private Message convertToMessage(ChatMessage chatMessage) {
        String content = chatMessage.getContent();
        String messageType = chatMessage.getMessageType();
        
        return switch (messageType) {
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default -> null;
        };
    }
}