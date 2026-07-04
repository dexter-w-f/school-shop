package com.example.mapper;

import com.example.entity.ChatMessage;
import java.util.List;
import java.util.Map;

public interface ChatMessageMapper {
    int insert(ChatMessage chatMessage);
    List<ChatMessage> selectByUserId(Integer userId);
    List<Map<String, Object>> selectSessions();
}
