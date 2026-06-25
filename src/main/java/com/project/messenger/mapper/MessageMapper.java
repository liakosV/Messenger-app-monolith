package com.project.messenger.mapper;

import com.project.messenger.dto.message.MessageInsertDto;
import com.project.messenger.dto.message.MessageReadDto;
import com.project.messenger.dto.message.MessageUpdateDto;
import com.project.messenger.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public MessageReadDto mapToMessageReadDto(Message message) {
        return new MessageReadDto(
                message.getId(),
                message.getUuid(),
                message.getContent(),
                message.getSender().getId(),
                message.getSender().getUuid().toString(),
                message.getSender().getUsername(),
                message.getConversation().getId(),
                message.getConversation().getUuid().toString(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    public Message mapToMessageEntity(MessageInsertDto insertDto) {
        Message message = new Message();

        message.setContent(insertDto.getContent());

        return message;
    }

    public void updateMessageFromDto(Message message, MessageUpdateDto dto) {
        if (dto.getContent() != null) {
            message.setContent(dto.getContent());
        }
    }

}
