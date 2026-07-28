package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectAccessDeniedException;
import com.project.messenger.core.exception.AppObjectInvalidArgumentException;
import com.project.messenger.core.exception.AppObjectNotFoundException;
import com.project.messenger.dto.conversation.ConversationInsertDto;
import com.project.messenger.dto.conversation.ConversationParticipantDto;
import com.project.messenger.dto.conversation.ConversationReadDto;
import com.project.messenger.dto.conversation.ConversationSummaryDto;
import com.project.messenger.mapper.ConversationMapper;
import com.project.messenger.model.Conversation;
import com.project.messenger.model.User;
import com.project.messenger.repository.ConversationRepository;
import com.project.messenger.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversationServiceTest {

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConversationService conversationService;

    @Test
    void createConversationWhenUsersExists() {
        UUID currentUserUuid = UUID.randomUUID();
        UUID participantUuid = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setUuid(currentUserUuid);

        User participant = new User();
        participant.setUuid(participantUuid);

        Conversation conversation = new Conversation();

        ConversationInsertDto insertDto = new ConversationInsertDto(new HashSet<>(Set.of(participantUuid)));
        ConversationReadDto expectedDto = mock(ConversationReadDto.class);
        Set<User> users = Set.of(currentUser, participant);
        Set<UUID> participantUuids = Set.of(currentUserUuid, participantUuid);

        when(userRepository.findAllByUuidIn(participantUuids))
                .thenReturn(List.of(currentUser, participant));

        when(conversationRepository.findConversationByExactParticipants(participantUuids, participantUuids.size()))
                .thenReturn(Optional.empty());

        when(conversationMapper.mapToConversationEntity(users))
                .thenReturn(conversation);

        when(conversationRepository.save(conversation))
                .thenReturn(conversation);

        when(conversationMapper.mapToConversationReadDto(conversation))
                .thenReturn(expectedDto);

        ConversationReadDto actualDto = conversationService.createConversation(insertDto, currentUserUuid);

        assertSame(expectedDto, actualDto);
        verify(userRepository).findAllByUuidIn(participantUuids);
        verify(conversationRepository).findConversationByExactParticipants(participantUuids, 2L);
        verify(conversationMapper).mapToConversationEntity(users);
        verify(conversationRepository).save(conversation);
        verify(conversationMapper).mapToConversationReadDto(conversation);
    }

    @Test
    void createConversationWhenOnlyCurrentUserIsProvided() {
        UUID currentUserUuid = UUID.randomUUID();

        ConversationInsertDto insertDto = new ConversationInsertDto(new HashSet<>());

        AppObjectInvalidArgumentException exception = assertThrows(
                AppObjectInvalidArgumentException.class,
                () -> conversationService.createConversation(insertDto, currentUserUuid)
        );

        assertEquals("Conversation needs at least two participants", exception.getMessage());

        verifyNoInteractions(userRepository, conversationRepository, conversationMapper);
    }

    @Test
    void createConversationWhenOneUserIsNotFound() {
        UUID currentUserUuid = UUID.randomUUID();
        UUID participantUuid = UUID.randomUUID();

        User participant = new User();
        participant.setUuid(participantUuid);

        User currentUser = new User();
        currentUser.setUuid(currentUserUuid);

        ConversationInsertDto insertDto = new ConversationInsertDto(new HashSet<>(Set.of(participantUuid)));

        when(userRepository.findAllByUuidIn(Set.of(participantUuid, currentUserUuid)))
                .thenReturn(List.of(currentUser));

        AppObjectNotFoundException exception = assertThrows(
                AppObjectNotFoundException.class,
                () -> conversationService.createConversation(insertDto, currentUserUuid)
        );

        assertEquals("One or more participants not found", exception.getMessage());

        verify(userRepository).findAllByUuidIn(Set.of(participantUuid, currentUserUuid));

        verifyNoInteractions(conversationRepository, conversationMapper);
    }

    @Test
    void createConversationWhenConversationAlreadyExists() {
        UUID currentUserUuid = UUID.randomUUID();
        UUID firstParticipantUuid = UUID.randomUUID();
        UUID secondParticipantUuid = UUID.randomUUID();

        User currentUser = new User();
        currentUser.setUuid(currentUserUuid);

        User firstParticipant = new User();
        firstParticipant.setUuid(firstParticipantUuid);

        User secondParticipant = new User();
        secondParticipant.setUuid(secondParticipantUuid);

        Set<UUID> participantUuids = Set.of(firstParticipantUuid, secondParticipantUuid, currentUserUuid);

        Set<User> participants = Set.of(currentUser, firstParticipant, secondParticipant);

        Conversation existingConversation = new Conversation();
        existingConversation.setParticipants(participants);

        ConversationInsertDto insertDto = new ConversationInsertDto(new HashSet<>(Set.of(firstParticipantUuid, secondParticipantUuid)));
        ConversationReadDto expectedDto = mock(ConversationReadDto.class);

        when(userRepository.findAllByUuidIn(participantUuids))
                .thenReturn(List.of(firstParticipant, secondParticipant, currentUser));

        when(conversationRepository.findConversationByExactParticipants(participantUuids, participantUuids.size()))
                .thenReturn(Optional.of(existingConversation));

        when(conversationMapper.mapToConversationReadDto(existingConversation))
                .thenReturn(expectedDto);

        ConversationReadDto actualDto = conversationService.createConversation(insertDto, currentUserUuid);

        assertSame(expectedDto, actualDto);

        verify(userRepository).findAllByUuidIn(participantUuids);
        verify(conversationRepository).findConversationByExactParticipants(participantUuids, 3L);
        verify(conversationMapper).mapToConversationReadDto(existingConversation);
        verify(conversationRepository, never()).save(any(Conversation.class));
        verify(conversationMapper, never()).mapToConversationEntity(anySet());
    }

    @Test
    void deleteConversationWhenLoggedInUserIsParticipant() {
        UUID conversationUuid = UUID.randomUUID();
        UUID loggedInUserUuid = UUID.randomUUID();

        User loggedInUser = new User();
        loggedInUser.setUuid(loggedInUserUuid);

        Conversation conversation = new Conversation();
        conversation.setParticipants(Set.of(loggedInUser));

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.of(conversation));

        conversationService.deleteConversation(conversationUuid, loggedInUserUuid);

        verify(conversationRepository).findByUuid(conversationUuid);
        verify(conversationRepository).delete(conversation);
    }

    @Test
    void deleteConversationWhenConversationNotFound() {
        UUID conversationUuid = UUID.randomUUID();
        UUID loggedInUserUuid = UUID.randomUUID();

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.empty());

        AppObjectNotFoundException exception = assertThrows(
                AppObjectNotFoundException.class,
                () -> conversationService.deleteConversation(conversationUuid, loggedInUserUuid)
        );

        assertEquals("Conversation not found", exception.getMessage());

        verify(conversationRepository).findByUuid(conversationUuid);
        verify(conversationRepository, never()).delete(any(Conversation.class));
    }

    @Test
    void deleteConversationWhenLoggedInUserIsNotParticipant() {
        UUID conversationUuid = UUID.randomUUID();
        UUID loggedInUserUuid = UUID.randomUUID();
        UUID otherUserUuid = UUID.randomUUID();

        User otherUser = new User();
        otherUser.setUuid(otherUserUuid);

        Conversation conversation = new Conversation();
        conversation.setParticipants(Set.of(otherUser));

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.of(conversation));

        AppObjectAccessDeniedException exception = assertThrows(
                AppObjectAccessDeniedException.class,
                () -> conversationService.deleteConversation(conversationUuid, loggedInUserUuid)
        );

        assertEquals("You can only delete your own conversations", exception.getMessage());

        verify(conversationRepository).findByUuid(conversationUuid);
        verify(conversationRepository, never()).delete(any(Conversation.class));
    }

    @Test
    void getAllConversationsForLoggedInUser() {
        UUID loggedInUserUuid = UUID.randomUUID();

        User loggedInUser = new User();
        loggedInUser.setUuid(loggedInUserUuid);

        Conversation firstConversation = new Conversation();
        firstConversation.setParticipants(Set.of(loggedInUser));

        Conversation secondConversation = new Conversation();
        secondConversation.setParticipants(Set.of(loggedInUser));

        ConversationSummaryDto firstDto = mock(ConversationSummaryDto.class);
        ConversationSummaryDto secondDto = mock(ConversationSummaryDto.class);

        when(userRepository.existsByUuid(loggedInUserUuid))
                .thenReturn(true);

        when(conversationRepository.findAllByParticipantUuid(loggedInUserUuid))
                .thenReturn(List.of(firstConversation, secondConversation));

        when(conversationMapper.mapToConversationSummaryDto(firstConversation))
                .thenReturn(firstDto);

        when(conversationMapper.mapToConversationSummaryDto(secondConversation))
                .thenReturn(secondDto);

        List<ConversationSummaryDto> actualDtos = conversationService.getAllConversationsForUser(loggedInUserUuid);

        assertEquals(List.of(firstDto, secondDto), actualDtos);

        verify(userRepository).existsByUuid(loggedInUserUuid);
        verify(conversationRepository).findAllByParticipantUuid(loggedInUserUuid);
        verify(conversationMapper).mapToConversationSummaryDto(firstConversation);
        verify(conversationMapper).mapToConversationSummaryDto(secondConversation);
    }

    @Test
    void getAllConversationsWhenUserIsNotFound() {
        UUID loggedInUserUuid = UUID.randomUUID();

        when(userRepository.existsByUuid(loggedInUserUuid))
                .thenReturn(false);

        AppObjectNotFoundException exception = assertThrows(
                AppObjectNotFoundException.class,
                () -> conversationService.getAllConversationsForUser(loggedInUserUuid)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).existsByUuid(loggedInUserUuid);
        verifyNoInteractions(conversationRepository, conversationMapper);
    }

    @Test
    void getConversationWhenConversationAndLoggedInUserExists() {
        UUID conversationUuid = UUID.randomUUID();
        UUID loggedInUserUuid = UUID.randomUUID();

        User loggedInUser = new User();
        loggedInUser.setUuid(loggedInUserUuid);

        Conversation conversation = new Conversation();

        ConversationParticipantDto conversationParticipantDto = new ConversationParticipantDto();
        conversationParticipantDto.setUuid(loggedInUserUuid);

        ConversationReadDto expectedDto = new ConversationReadDto();
        expectedDto.setParticipants(Set.of(conversationParticipantDto));

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.of(conversation));

        when(conversationMapper.mapToConversationReadDto(conversation))
                .thenReturn(expectedDto);

        ConversationReadDto actualDto = conversationService.getConversation(conversationUuid, loggedInUserUuid);

        assertSame(expectedDto, actualDto);

        verify(conversationRepository).findByUuid(conversationUuid);
        verify(conversationMapper).mapToConversationReadDto(conversation);
    }

    @Test
    void getConversationWhenConversationNotFound() {
        UUID conversationUuid = UUID.randomUUID();
        UUID loggedInUserUuid = UUID.randomUUID();

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.empty());

        AppObjectNotFoundException exception = assertThrows(
                AppObjectNotFoundException.class,
                () -> conversationService.getConversation(conversationUuid, loggedInUserUuid)
        );

        assertEquals("Conversation not found", exception.getMessage());

        verify(conversationRepository).findByUuid(conversationUuid);
        verifyNoInteractions(conversationMapper);
    }

    @Test
    void getConversationWhenLoggedInUserIsNotParticipant() {
        UUID conversationUuid = UUID.randomUUID();
        UUID loggedInUserUuid = UUID.randomUUID();
        UUID otherUserUuid = UUID.randomUUID();

        Conversation conversation = new Conversation();

        ConversationParticipantDto participantDto = new ConversationParticipantDto();
        participantDto.setUuid(otherUserUuid);

        ConversationReadDto readDto = new ConversationReadDto();
        readDto.setParticipants(Set.of(participantDto));

        when(conversationRepository.findByUuid(conversationUuid))
                .thenReturn(Optional.of(conversation));

        when(conversationMapper.mapToConversationReadDto(conversation))
                .thenReturn(readDto);

        AppObjectAccessDeniedException exception = assertThrows(
                AppObjectAccessDeniedException.class,
                () -> conversationService.getConversation(conversationUuid, loggedInUserUuid)
        );

        assertEquals("You can only see your own conversation", exception.getMessage());

        verify(conversationRepository).findByUuid(conversationUuid);
        verify(conversationMapper).mapToConversationReadDto(conversation);
    }

}
