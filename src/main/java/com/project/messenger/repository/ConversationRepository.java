package com.project.messenger.repository;

import com.project.messenger.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Query("""
            SELECT c
            FROM Conversation c
            JOIN c.participants p
            WHERE p.uuid = :userUuid
            ORDER BY c.updatedAt DESC
            """)
    List<Conversation> findAllByParticipantUuid(@Param("userUuid") UUID userUuid);

    @Query("""
            SELECT c
            FROM Conversation c
            JOIN c.participants p1
            JOIN c.participants p2
            WHERE p1.uuid = :userAUuid
              AND p2.uuid = :userBUuid
              AND SIZE(c.participants) = 2
            """)
    Optional<Conversation> findPrivateConversationBetweenUsers(
            @Param("userAUuid") UUID userAUuid,
            @Param("userBUuid") UUID userBUuid
    );
}
