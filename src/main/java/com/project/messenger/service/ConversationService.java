package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectInvalidArgumentException;
import com.project.messenger.core.exception.AppObjectNotFoundException;
import com.project.messenger.dto.conversation.ConversationInsertDto;
import com.project.messenger.dto.conversation.ConversationReadDto;
import com.project.messenger.dto.conversation.ConversationSummaryDto;
import com.project.messenger.mapper.ConversationMapper;
import com.project.messenger.model.Conversation;
import com.project.messenger.model.User;
import com.project.messenger.repository.ConversationRepository;
import com.project.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationMapper conversationMapper;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     * Creates a conversation between existing users, or returns an existing private conversation
     * when the same two users already have one.
     *
     * @param insertDto the participant UUIDs used to create the conversation
     * @return the created or existing conversation as a read DTO
     * @throws AppObjectInvalidArgumentException if fewer than two participants are provided
     * @throws AppObjectNotFoundException if one or more participant UUIDs do not belong to users
     */
    @Transactional
    public ConversationReadDto createConversation(ConversationInsertDto insertDto) {
        Set<UUID> participantUuids = insertDto.getParticipantUuids();

        if (participantUuids.size() < 2) {
            throw new AppObjectInvalidArgumentException("Conversation", "Conversation needs at least two participants");
        }

        List<User> users = userRepository.findAllByUuidIn(participantUuids);

        if (users.size() != participantUuids.size()) {
            throw new AppObjectNotFoundException("User", "One or more participants not found");
        }

        if (participantUuids.size() == 2) {
            List<UUID> uuids = participantUuids.stream().toList();

            Optional<Conversation> existingConversation = conversationRepository.findPrivateConversationBetweenUsers(uuids.get(0), uuids.get(1));

            if (existingConversation.isPresent()) {
                return conversationMapper.mapToConversationReadDto(existingConversation.get());
            }
        }

        Conversation conversation = conversationMapper.mapToConversationEntity(new HashSet<>(users));

        Conversation savedConversation = conversationRepository.save(conversation);

        return conversationMapper.mapToConversationReadDto(savedConversation);
    }

    /**
     * Deletes a conversation by UUID.
     *
     * @param uuid the public UUID of the conversation to delete
     * @throws AppObjectNotFoundException if no conversation exists with the given UUID
     */
    @Transactional
    public void deleteConversation(UUID uuid) {
        Conversation conversation = conversationRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Conversation", "Conversation not found"));

        conversationRepository.delete(conversation);
    }

    /**
     * Finds all conversations where the given user is a participant.
     *
     * @param userUuid the public UUID of the user whose conversations should be listed
     * @return the user's conversations as summary DTOs
     * @throws AppObjectNotFoundException if no user exists with the given UUID
     */
    @Transactional(readOnly = true)
    public List<ConversationSummaryDto> getAllConversationsForUser(UUID userUuid) {
        if (!userRepository.existsByUuid(userUuid)) {
            throw new AppObjectNotFoundException("User", "User not found");
        }

        return conversationRepository.findAllByParticipantUuid(userUuid)
                .stream()
                .map(conversationMapper::mapToConversationSummaryDto)
                .toList();
    }

    /**
     * Finds a conversation by UUID.
     *
     * @param uuid the public UUID of the conversation
     * @return the matching conversation as a read DTO
     * @throws AppObjectNotFoundException if no conversation exists with the given UUID
     */
    @Transactional(readOnly = true)
    public ConversationReadDto getConversation(UUID uuid) {
        return conversationRepository.findByUuid(uuid)
                .map(conversationMapper::mapToConversationReadDto)
                .orElseThrow(() -> new AppObjectNotFoundException("Conversation", "Conversation not found"));
    }
}
