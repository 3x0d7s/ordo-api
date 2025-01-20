CREATE TABLE IF NOT EXISTS lists (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    board_id BIGINT REFERENCES boards(id),
    color VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    position INTEGER NOT NULL,
    list_id BIGINT REFERENCES lists(id),
    created_by BIGINT REFERENCES users(id) NOT NULL,
    assigned_to BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    created_by BIGINT REFERENCES users(id) NOT NULL,
    card_id BIGINT REFERENCES tasks(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);