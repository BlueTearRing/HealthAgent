package com.liagent.aiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liagent.aiagent.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天记录Mapper接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 根据会话ID获取最近N条消息记录
     * @param conversationId 会话ID
     * @param lastN 获取最近N条记录
     * @return 消息记录列表
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND is_delete = 0 " +
            "ORDER BY create_time DESC LIMIT #{lastN}")
    List<ChatMessage> getRecentMessages(@Param("conversationId") String conversationId, @Param("lastN") int lastN);

    /**
     * 根据会话ID删除所有消息记录（逻辑删除）
     * @param conversationId 会话ID
     * @return 影响行数
     */
    @Update("UPDATE chat_message SET is_delete = 1 WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(@Param("conversationId") String conversationId);
}
