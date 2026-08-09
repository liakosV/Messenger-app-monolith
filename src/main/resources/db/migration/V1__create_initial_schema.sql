CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid CHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    phone_number VARCHAR(255) NOT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_uuid UNIQUE (uuid),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
);

CREATE TABLE conversations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    uuid CHAR(36) NOT NULL,

    CONSTRAINT pk_conversations PRIMARY KEY (id),
    CONSTRAINT uk_conversations_uuid UNIQUE (uuid)
);

CREATE TABLE messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
        created_at DATETIME(6) NOT NULL,
        updated_at DATETIME(6) NOT NULL,
        uuid CHAR(36) NOT NULL,
        content VARCHAR(2000) NOT NULL,
        conversation_id BIGINT NOT NULL,
        sender_id BIGINT NOT NULL,

        CONSTRAINT pk_messages PRIMARY KEY (id),
        CONSTRAINT uk_messages_uuid UNIQUE (uuid),
        CONSTRAINT fk_messages_conversation_id FOREIGN KEY (conversation_id) REFERENCES conversations (id),
        CONSTRAINT fk_messages_sender_id FOREIGN KEY (sender_id) REFERENCES users (id)
);

CREATE TABLE conversation_participants (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT pk_conversation_participants PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_conversation_participants_conversation_id FOREIGN KEY (conversation_id) REFERENCES conversations (id),
    CONSTRAINT fk_conversation_participants_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);