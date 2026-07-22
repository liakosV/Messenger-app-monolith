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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void sendMessageWhenSenderIsNotParticipant() {
        UUID conversationUuid = UUID.randomUUID();
        UUID senderUuid = UUID.randomUUID();

        User sender = new User();
        sender.setUuid(senderUuid);

        User participant = new User();

        Conversation conversation = new Conversation();
        conversation.setUuid(conversationUuid);
        conversation.setParticipants(new HashSet<>());
        conversation.getParticipants().add(participant);

        MessageInsertDto insertDto = new MessageInsertDto("Hello World!");

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.of(conversation));

        when(userRepository.findByUuid(senderUuid))
                .thenReturn(Optional.of(sender));

        AppObjectAccessDeniedException exception = assertThrows(
                AppObjectAccessDeniedException.class,
                () -> messageService.sendMessage(conversationUuid, senderUuid, insertDto)
        );

        assertEquals("User is not a participant", exception.getMessage());

        verify(messageRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void sendMessageWhenSenderIsParticipant() {
        UUID conversationUuid = UUID.randomUUID();
        UUID senderUuid = UUID.randomUUID();

        User sender = new User();
        sender.setUuid(senderUuid);

        Conversation conversation = new Conversation();
        conversation.setUuid(conversationUuid);
        conversation.setParticipants(new HashSet<>());
        conversation.getParticipants().add(sender);

        MessageInsertDto insertDto = new MessageInsertDto("Hello World!");

        Message mappedMessage = new Message();
        MessageReadDto expectedDto = mock(MessageReadDto.class);

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.of(conversation));

        when(userRepository.findByUuid(senderUuid))
                .thenReturn(Optional.of(sender));

        when(messageMapper.mapToMessageEntity(insertDto))
                .thenReturn(mappedMessage);

        when(messageRepository.save(mappedMessage))
                .thenReturn(mappedMessage);

        when(messageMapper.mapToMessageReadDto(mappedMessage))
                .thenReturn(expectedDto);

        MessageReadDto actualDto = messageService.sendMessage(
                conversationUuid,
                senderUuid,
                insertDto
        );

        assertSame(sender, mappedMessage.getSender());
        assertSame(conversation, mappedMessage.getConversation());
        assertSame(expectedDto, actualDto);

        verify(messageRepository).save(mappedMessage);
    }

    @Test
    void sendMessageWhenConversationDoesNotExist() {
        UUID conversationUuid = UUID.randomUUID();
        UUID senderUuid = UUID.randomUUID();
        MessageInsertDto insertDto = new MessageInsertDto("Hello world!");

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.empty());

         assertThrows(
                AppObjectNotFoundException.class,
                () -> messageService.sendMessage(conversationUuid, senderUuid, insertDto)
        );

        verifyNoInteractions(userRepository);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessageWhenSenderDoesNotExist() {
        UUID conversationUuid = UUID.randomUUID();
        UUID senderUuid = UUID.randomUUID();
        MessageInsertDto insertDto = new MessageInsertDto("Hello World!");

        Conversation conversation = new Conversation();
        conversation.setUuid(conversationUuid);
        conversation.setParticipants(new HashSet<>());

        when(userRepository.findByUuid(senderUuid))
                .thenReturn(Optional.empty());

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.of(conversation));

        assertThrows(
                AppObjectNotFoundException.class,
                () -> messageService.sendMessage(conversationUuid, senderUuid, insertDto)
        );

        verify(messageRepository, never()).save(any());

    }

    @Test
    void editMessageWhenMessageExistsAndUserIsSender() {
        UUID messageUuid = UUID.randomUUID();
        UUID senderUuid = UUID.randomUUID();

        User sender = new User();
        sender.setUuid(senderUuid);

        Message message = new Message();
        message.setUuid(messageUuid);
        message.setSender(sender);
        message.setContent("Hello world");

        MessageUpdateDto updateDto = new MessageUpdateDto("Hello world!");

        MessageReadDto expectedDto = mock(MessageReadDto.class);

        when(messageRepository.findByUuid(messageUuid))
                .thenReturn(Optional.of(message));

        when(messageMapper.mapToMessageReadDto(message))
                .thenReturn(expectedDto);

        MessageReadDto actualDto = messageService.editMessage(updateDto, messageUuid, senderUuid);

        verify(messageMapper).updateMessageFromDto(message, updateDto);
        verify(messageMapper).mapToMessageReadDto(message);
        verify(messageRepository).findByUuid(messageUuid);

        assertSame(expectedDto, actualDto);
    }

    @Test
    void editMessageWhenMessageDoesNotExist() {
        UUID senderUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        MessageUpdateDto updateDto = new MessageUpdateDto("Hello World!");

        when(messageRepository.findByUuid(messageUuid))
                .thenReturn(Optional.empty());

        AppObjectNotFoundException exception = assertThrows(
                AppObjectNotFoundException.class,
                () -> messageService.editMessage(updateDto, messageUuid, senderUuid));

        assertEquals("Message not found", exception.getMessage());

        verify(messageRepository).findByUuid(messageUuid);
        verifyNoInteractions(messageMapper);
    }

    @Test
    void editMessageWhenUserIsNotSender() {
        UUID currentUserUuid = UUID.randomUUID();
        UUID actualSenderUuid = UUID.randomUUID();
        UUID messageUuid = UUID.randomUUID();

        User actualSender = new User();
        actualSender.setUuid(actualSenderUuid);

        Message message = new Message();
        message.setUuid(messageUuid);
        message.setContent("Hello World");
        message.setSender(actualSender);

        MessageUpdateDto updateDto = new MessageUpdateDto("Hello world!");

        when(messageRepository.findByUuid(messageUuid))
                .thenReturn(Optional.of(message));

        AppObjectAccessDeniedException exception = assertThrows(
                AppObjectAccessDeniedException.class,
                () -> messageService.editMessage(updateDto, messageUuid, currentUserUuid)
        );

        assertEquals("You can only edit your own messages", exception.getMessage());

        verify(messageRepository).findByUuid(messageUuid);
        verifyNoInteractions(messageMapper);
    }

    @Test
    void deleteMessageWhenUserIsSender() {
        UUID messageUuid = UUID.randomUUID();
        UUID senderUuid = UUID.randomUUID();

        User sender = new User();
        sender.setUuid(senderUuid);

        Message message = new Message();
        message.setUuid(messageUuid);
        message.setSender(sender);
        message.setContent("Hello world!");

        when(messageRepository.findByUuid(messageUuid))
                .thenReturn(Optional.of(message));

        messageService.deleteMessage(messageUuid, senderUuid);

        verify(messageRepository).findByUuid(messageUuid);
        verify(messageRepository).delete(message);

    }

    @Test
    void deleteMessageWhenMessageDoesNotExist() {
        UUID messageUuid = UUID.randomUUID();
        UUID senderUuid = UUID.randomUUID();

        when(messageRepository.findByUuid(messageUuid))
                .thenReturn(Optional.empty());

        AppObjectNotFoundException exception = assertThrows(
                AppObjectNotFoundException.class,
                () -> messageService.deleteMessage(messageUuid, senderUuid)
        );

        assertEquals("Message not found", exception.getMessage());

        verify(messageRepository).findByUuid(messageUuid);
        verify(messageRepository, never()).delete(any());
    }

    @Test
    void deleteMessageWhenUserIsNotSender() {
        UUID messageUuid = UUID.randomUUID();
        UUID actualSenderUuid = UUID.randomUUID();
        UUID currentUserUuid = UUID.randomUUID();

        User actualSender = new User();
        actualSender.setUuid(actualSenderUuid);

        Message message = new Message();
        message.setUuid(messageUuid);
        message.setSender(actualSender);

        when(messageRepository.findByUuid(messageUuid))
                .thenReturn(Optional.of(message));

        AppObjectAccessDeniedException exception = assertThrows(
                AppObjectAccessDeniedException.class,
                () -> messageService.deleteMessage(messageUuid, currentUserUuid)
        );

        assertEquals("You can only delete your own messages", exception.getMessage());

        verify(messageRepository).findByUuid(messageUuid);
        verify(messageRepository, never()).delete(message);
    }
}
