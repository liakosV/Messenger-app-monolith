package com.project.messenger.repository;

import com.project.messenger.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByUuid(UUID uuid);

    List<Message> findByConversationUuidOrderByCreatedAt(UUID conversationUuid);

    Page<Message> findByConversationUuidOrderByCreatedAt(
            UUID conversationUuid,
            Pageable pageable
    );

    boolean existsByUuid(UUID uuid);
}
