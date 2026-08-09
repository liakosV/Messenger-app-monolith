package com.project.messenger.websocket;

import com.project.messenger.model.User;
import com.project.messenger.repository.ConversationRepository;
import com.project.messenger.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern CONVERSATION_TOPIC = Pattern.compile("^/topic/conversations/([0-9a-fA-F-]{36})$");

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ConversationRepository conversationRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscription(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization =
                accessor.getFirstNativeHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            throw new MessagingException(
                    "A Bearer token is required"
            );
        }

        try {
            String token = authorization.substring(7);
            String username = jwtService.extractUsername(token);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (!userDetails.isEnabled() || !jwtService.isTokenValid(token, userDetails)) {
                throw new MessagingException(
                        "Invalid WebSocket access token"
                );
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            accessor.setUser(authentication);
        } catch (MessagingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new MessagingException(
                    "Invalid WebSocket access token",
                    exception
            );
        }
    }

    private void authorizeSubscription(
            StompHeaderAccessor accessor
    ) {
        if (!(accessor.getUser()
                instanceof UsernamePasswordAuthenticationToken authentication)) {
            throw new MessagingException(
                    "WebSocket connection is not authenticated"
            );
        }

        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new MessagingException(
                    "Authenticated WebSocket user is invalid"
            );
        }

        String destination = accessor.getDestination();

        if (destination == null) {
            throw new MessagingException(
                    "Subscription destination is required"
            );
        }

        Matcher matcher = CONVERSATION_TOPIC.matcher(destination);

        if (!matcher.matches()) {
            throw new MessagingException(
                    "Invalid subscription destination"
            );
        }

        UUID conversationUuid =
                UUID.fromString(matcher.group(1));

        boolean participant =
                conversationRepository
                        .existsByUuidAndParticipantsUuid(
                                conversationUuid,
                                user.getUuid()
                        );

        if (!participant) {
            throw new MessagingException(
                    "You are not a participant in this conversation"
            );
        }
    }
}
