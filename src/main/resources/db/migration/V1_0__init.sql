CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,

    provider VARCHAR(50),
    provider_id VARCHAR(255),
    image_url VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    last_login_at TIMESTAMP,

    UNIQUE(email),
    UNIQUE(provider, provider_id)
);


CREATE TABLE IF NOT EXISTS workspaces (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);


CREATE TABLE IF NOT EXISTS boards (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    workspace_id BIGINT REFERENCES workspaces(id)
);


CREATE TABLE IF NOT EXISTS workspace_members (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT REFERENCES workspaces(id),
    user_id BIGINT REFERENCES users(id),
    role VARCHAR(50) NOT NULL, -- 'ADMIN', 'MEMBER', 'GUEST'
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(workspace_id, user_id)
);


CREATE TABLE IF NOT EXISTS board_members (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT REFERENCES boards(id),
    user_id BIGINT REFERENCES users(id),
    role VARCHAR(50) NOT NULL, -- 'ADMIN', 'MEMBER', 'OBSERVER'
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(board_id, user_id)
);

