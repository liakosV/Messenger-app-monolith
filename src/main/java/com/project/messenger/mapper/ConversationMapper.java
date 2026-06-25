package com.project.messenger.mapper;

import com.project.messenger.dto.conversation.ConversationInsertDto;
import com.project.messenger.dto.conversation.ConversationReadDto;
import com.project.messenger.dto.conversation.ConversationParticipantDto;
import com.project.messenger.dto.conversation.ConversationSummaryDto;
import com.project.messenger.dto.message.MessageReadDto;
import com.project.messenger.model.Conversation;
import com.project.messenger.model.Message;
import com.project.messenger.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConversationMapper {

    private final MessageMapper messageMapper;

    public ConversationReadDto mapToConversationReadDto(Conversation conversation) {
        return new ConversationReadDto(
                conversation.getId(),
                conversation.getUuid(),
                conversation.getParticipants().stream()
                        .map(this::mapToConversationParticipantDto)
                        .collect(Collectors.toSet()),
                conversation.getMessages().stream()
                        .map(messageMapper::mapToMessageReadDto)
                        .toList(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    private ConversationParticipantDto mapToConversationParticipantDto(User user) {
        return new ConversationParticipantDto(
                user.getId(),
                user.getUuid(),
                user.getUsername()
        );
    }

    public Conversation mapToConversationEntity(Set<User> participants) {
        Conversation conversation = new Conversation();

        conversation.setParticipants(new HashSet<>(participants));

        return conversation;
    }

    public ConversationSummaryDto mapToConversationSummaryDto(Conversation conversation) {
        Message lastMessage = conversation.getMessages().stream()
                .max(Comparator.comparing(Message::getCreatedAt))
                .orElse(null);

        return new ConversationSummaryDto(
                conversation.getId(),
                conversation.getUuid(),
                conversation.getParticipants().stream()
                        .map(this::mapToConversationParticipantDto)
                        .collect(Collectors.toSet()),
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getSender().getUsername() : null,
                lastMessage != null ? lastMessage.getCreatedAt() : null,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
