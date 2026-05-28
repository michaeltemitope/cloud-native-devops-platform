-- =============================================================
-- V1 – Initial Schema
-- OpsByTemitope: users, projects, tasks, activity_events
-- =============================================================

-- ── users ────────────────────────────────────────────────────
CREATE TABLE users (
    id          BIGSERIAL       PRIMARY KEY,
    email       VARCHAR(100)    NOT NULL UNIQUE,
    password    VARCHAR(60)     NOT NULL,
    first_name  VARCHAR(50)     NOT NULL,
    last_name   VARCHAR(50)     NOT NULL,
    role        VARCHAR(20)     NOT NULL DEFAULT 'USER'
                    CHECK (role IN ('USER', 'ADMIN')),
    enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ
);

CREATE INDEX idx_users_email ON users (email);

-- ── projects ─────────────────────────────────────────────────
CREATE TABLE projects (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(150)    NOT NULL,
    description TEXT,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED')),
    due_date    DATE,
    owner_id    BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ
);

CREATE INDEX idx_projects_owner_id       ON projects (owner_id);
CREATE INDEX idx_projects_status         ON projects (status);
CREATE INDEX idx_projects_owner_status   ON projects (owner_id, status);

-- ── tasks ────────────────────────────────────────────────────
CREATE TABLE tasks (
    id          BIGSERIAL       PRIMARY KEY,
    title       VARCHAR(200)    NOT NULL,
    description TEXT,
    status      VARCHAR(20)     NOT NULL DEFAULT 'TODO'
                    CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED')),
    priority    VARCHAR(10)     NOT NULL DEFAULT 'MEDIUM'
                    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    due_date    DATE,
    project_id  BIGINT          NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    assignee_id BIGINT          REFERENCES users (id) ON DELETE SET NULL,
    created_by  BIGINT          NOT NULL REFERENCES users (id),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ
);

CREATE INDEX idx_tasks_project_id    ON tasks (project_id);
CREATE INDEX idx_tasks_assignee_id   ON tasks (assignee_id);
CREATE INDEX idx_tasks_status        ON tasks (status);
CREATE INDEX idx_tasks_priority      ON tasks (priority);
CREATE INDEX idx_tasks_project_status ON tasks (project_id, status);

-- ── activity_events ──────────────────────────────────────────
CREATE TABLE activity_events (
    id          BIGSERIAL       PRIMARY KEY,
    event_type  VARCHAR(50)     NOT NULL
                    CHECK (event_type IN (
                        'TASK_CREATED', 'TASK_UPDATED', 'TASK_STATUS_CHANGED',
                        'TASK_ASSIGNED', 'TASK_DELETED',
                        'PROJECT_CREATED', 'PROJECT_UPDATED', 'PROJECT_STATUS_CHANGED',
                        'PROJECT_DELETED', 'USER_JOINED', 'COMMENT_ADDED'
                    )),
    description TEXT,
    task_id     BIGINT          REFERENCES tasks (id) ON DELETE SET NULL,
    project_id  BIGINT          REFERENCES projects (id) ON DELETE CASCADE,
    actor_id    BIGINT          NOT NULL REFERENCES users (id),
    metadata    TEXT,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_project_id  ON activity_events (project_id);
CREATE INDEX idx_activity_actor_id    ON activity_events (actor_id);
CREATE INDEX idx_activity_task_id     ON activity_events (task_id);
CREATE INDEX idx_activity_created_at  ON activity_events (created_at DESC);
CREATE INDEX idx_activity_event_type  ON activity_events (event_type);
