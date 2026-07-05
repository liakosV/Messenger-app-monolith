package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectAccessDeniedException;
import com.project.messenger.core.exception.AppObjectNotFoundException;
import com.project.messenger.dto.message.MessageInsertDto;
import com.project.messenger.dto.message.MessageReadDto;
import com.project.messenger.dto.message.MessageUpdateDto;
import com.project.messenger.mapper.MessageMapper;
import com.project.messenger.model.Conversation;
import com.project.messenger.model.Message;
import com.project.messenger.model.User;
import com.project.messenger.repository.ConversationRepository;
import com.project.messenger.repository.MessageRepository;
import com.project.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     * Sends a message inside a conversation after confirming the sender belongs to that conversation.
     *
     * @param conversationUuid the public UUID of the conversation receiving the message
     * @param senderUuid the public UUID of the user sending the message
     * @param insertDto the message content to send
     * @return the saved message as a read DTO
     * @throws AppObjectNotFoundException if the conversation or sender does not exist
     * @throws AppObjectAccessDeniedException if the sender is not a participant in the conversation
     */
    @Transactional
    public MessageReadDto sendMessage(UUID conversationUuid, UUID senderUuid, MessageInsertDto insertDto) {
        Conversation conversation = conversationRepository.findByUuid(conversationUuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Conversation", "Conversation not found"));

        User sender = userRepository.findByUuid(senderUuid)
                .orElseThrow(() -> new AppObjectNotFoundException("User", "User not found"));

        if (!conversation.getParticipants().contains(sender)) {
            throw new AppObjectAccessDeniedException("Conversation", "User is not a participant");
        }

        Message message = messageMapper.mapToMessageEntity(insertDto);
        message.setConversation(conversation);
        message.setSender(sender);

        Message savedMessage = messageRepository.save(message);

        return messageMapper.mapToMessageReadDto(savedMessage);
    }

    /**
     * Edits a message only when the current user is the original sender.
     *
     * @param updateDto the new message content
     * @param messageUuid the public UUID of the message to edit
     * @param currentUserUuid the public UUID of the user requesting the edit
     * @return the updated message as a read DTO
     * @throws AppObjectNotFoundException if no message exists with the given UUID
     * @throws AppObjectAccessDeniedException if the current user is not the original sender
     */
    @Transactional
    public MessageReadDto editMessage(MessageUpdateDto updateDto, UUID messageUuid, UUID currentUserUuid) {
        Message message = messageRepository.findByUuid(messageUuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Message", "Message not found"));

        if (!message.getSender().getUuid().equals(currentUserUuid)) {
            throw new AppObjectAccessDeniedException("Message", "You can only edit your own messages");
        }

        messageMapper.updateMessageFromDto(message, updateDto);

        return messageMapper.mapToMessageReadDto(message);
    }

    /**
     * Deletes a message only when the current user is the original sender.
     *
     * @param messageUuid the public UUID of the message to delete
     * @param currentUserUuid the public UUID of the user requesting the delete
     * @throws AppObjectNotFoundException if no message exists with the given UUID
     * @throws AppObjectAccessDeniedException if the current user is not the original sender
     */
    @Transactional
    public void deleteMessage(UUID messageUuid, UUID currentUserUuid) {
        Message message = messageRepository.findByUuid(messageUuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Message", "Message not found"));

        if (!message.getSender().getUuid().equals(currentUserUuid)) {
            throw new AppObjectAccessDeniedException("Message", "You can only delete your own messages");
        }

        messageRepository.delete(message);
    }

    /**
     * Finds all messages for a conversation in creation order.
     *
     * @param conversationUuid the public UUID of the conversation
     * @return the conversation messages as read DTOs
     */
    @Transactional(readOnly = true)
    public List<MessageReadDto> getAllMessagesByConversationUuid(UUID conversationUuid) {
        return messageRepository.findByConversationUuidOrderByCreatedAt(conversationUuid)
                .stream()
                .map(messageMapper::mapToMessageReadDto)
                .toList();
    }

    /**
     * Finds a message by UUID.
     *
     * @param uuid the public UUID of the message
     * @return the matching message as a read DTO
     * @throws AppObjectNotFoundException if no message exists with the given UUID
     */
    @Transactional(readOnly = true)
    public MessageReadDto getMessage(UUID uuid) {
        return messageRepository.findByUuid(uuid)
                .map(messageMapper::mapToMessageReadDto)
                .orElseThrow(() -> new AppObjectNotFoundException("Message", "Message not found"));
    }


}
