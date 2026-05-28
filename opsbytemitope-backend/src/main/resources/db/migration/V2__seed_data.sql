-- =============================================================
-- V2 – Seed / Sample Data
-- Passwords are BCrypt hashes of "Password123!"
-- DO NOT use this migration in production environments.
-- =============================================================

-- ── Users ────────────────────────────────────────────────────
INSERT INTO users (email, password, first_name, last_name, role, enabled)
VALUES
    ('admin@opsbytemitope.com',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCBgXm3//9Y6EyJT5Hl6Jii',
     'Admin', 'User', 'ADMIN', TRUE),

    ('alice@example.com',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCBgXm3//9Y6EyJT5Hl6Jii',
     'Alice', 'Johnson', 'USER', TRUE),

    ('bob@example.com',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCBgXm3//9Y6EyJT5Hl6Jii',
     'Bob', 'Smith', 'USER', TRUE),

    ('carol@example.com',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCBgXm3//9Y6EyJT5Hl6Jii',
     'Carol', 'Williams', 'USER', TRUE);

-- ── Projects (owned by Alice) ────────────────────────────────
INSERT INTO projects (name, description, status, due_date, owner_id)
VALUES
    ('Website Redesign',
     'Complete overhaul of the company marketing website with new branding and improved UX.',
     'ACTIVE',
     NOW() + INTERVAL '60 days',
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('Mobile App MVP',
     'Build and ship the first version of the iOS/Android mobile application.',
     'ACTIVE',
     NOW() + INTERVAL '90 days',
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('Data Pipeline Refactor',
     'Migrate legacy ETL scripts to a modern Spark-based pipeline with proper monitoring.',
     'ON_HOLD',
     NOW() + INTERVAL '120 days',
     (SELECT id FROM users WHERE email = 'alice@example.com'));

-- ── Projects (owned by Bob) ──────────────────────────────────
INSERT INTO projects (name, description, status, due_date, owner_id)
VALUES
    ('API Gateway Integration',
     'Integrate all internal services behind a unified API gateway with rate limiting and auth.',
     'ACTIVE',
     NOW() + INTERVAL '45 days',
     (SELECT id FROM users WHERE email = 'bob@example.com'));

-- ── Tasks – Website Redesign ─────────────────────────────────
INSERT INTO tasks (title, description, status, priority, due_date, project_id, assignee_id, created_by)
VALUES
    ('Audit current website performance',
     'Run Lighthouse and PageSpeed audits. Document all scores and identify bottlenecks.',
     'DONE', 'HIGH',
     NOW() - INTERVAL '5 days',
     (SELECT id FROM projects WHERE name = 'Website Redesign'),
     (SELECT id FROM users WHERE email = 'alice@example.com'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('Create new design system',
     'Define colour palette, typography, spacing tokens, and base component library in Figma.',
     'IN_PROGRESS', 'HIGH',
     NOW() + INTERVAL '14 days',
     (SELECT id FROM projects WHERE name = 'Website Redesign'),
     (SELECT id FROM users WHERE email = 'carol@example.com'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('Implement responsive navbar',
     'Build the new top navigation with mobile hamburger menu. Match design system tokens.',
     'TODO', 'MEDIUM',
     NOW() + INTERVAL '21 days',
     (SELECT id FROM projects WHERE name = 'Website Redesign'),
     (SELECT id FROM users WHERE email = 'bob@example.com'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('SEO meta-tag audit and update',
     'Review all page meta tags, open-graph properties, and structured data markup.',
     'TODO', 'LOW',
     NOW() + INTERVAL '30 days',
     (SELECT id FROM projects WHERE name = 'Website Redesign'),
     NULL,
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('Accessibility review (WCAG 2.1 AA)',
     'Run axe-core scans and manual keyboard-navigation testing across all page templates.',
     'TODO', 'HIGH',
     NOW() + INTERVAL '35 days',
     (SELECT id FROM projects WHERE name = 'Website Redesign'),
     (SELECT id FROM users WHERE email = 'carol@example.com'),
     (SELECT id FROM users WHERE email = 'alice@example.com'));

-- ── Tasks – Mobile App MVP ───────────────────────────────────
INSERT INTO tasks (title, description, status, priority, due_date, project_id, assignee_id, created_by)
VALUES
    ('Set up React Native project scaffold',
     'Initialise Expo managed workflow, configure ESLint, Prettier, and commit hooks.',
     'DONE', 'CRITICAL',
     NOW() - INTERVAL '10 days',
     (SELECT id FROM projects WHERE name = 'Mobile App MVP'),
     (SELECT id FROM users WHERE email = 'bob@example.com'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('Implement authentication flow',
     'Login, register, forgot-password screens connected to the OpsByTemitope REST API with token storage.',
     'IN_PROGRESS', 'CRITICAL',
     NOW() + INTERVAL '7 days',
     (SELECT id FROM projects WHERE name = 'Mobile App MVP'),
     (SELECT id FROM users WHERE email = 'bob@example.com'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('Task list screen',
     'Display paginated task list with filter chips (status, priority) and pull-to-refresh.',
     'TODO', 'HIGH',
     NOW() + INTERVAL '20 days',
     (SELECT id FROM projects WHERE name = 'Mobile App MVP'),
     (SELECT id FROM users WHERE email = 'carol@example.com'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('Push notification integration',
     'Integrate Expo push notifications for task assignment and status-change events.',
     'TODO', 'MEDIUM',
     NOW() + INTERVAL '45 days',
     (SELECT id FROM projects WHERE name = 'Mobile App MVP'),
     NULL,
     (SELECT id FROM users WHERE email = 'alice@example.com'));

-- ── Tasks – API Gateway Integration ─────────────────────────
INSERT INTO tasks (title, description, status, priority, due_date, project_id, assignee_id, created_by)
VALUES
    ('Evaluate Kong vs AWS API Gateway',
     'Produce an ADR with latency benchmarks, cost analysis, and team skill assessment.',
     'IN_REVIEW', 'HIGH',
     NOW() + INTERVAL '5 days',
     (SELECT id FROM projects WHERE name = 'API Gateway Integration'),
     (SELECT id FROM users WHERE email = 'bob@example.com'),
     (SELECT id FROM users WHERE email = 'bob@example.com')),

    ('Define rate-limiting policy',
     'Agree per-consumer and global rate limits. Document tiering strategy for internal vs external clients.',
     'TODO', 'MEDIUM',
     NOW() + INTERVAL '15 days',
     (SELECT id FROM projects WHERE name = 'API Gateway Integration'),
     (SELECT id FROM users WHERE email = 'alice@example.com'),
     (SELECT id FROM users WHERE email = 'bob@example.com'));

-- ── Activity Events ──────────────────────────────────────────
INSERT INTO activity_events (event_type, description, project_id, actor_id)
VALUES
    ('PROJECT_CREATED',
     'Project ''Website Redesign'' was created',
     (SELECT id FROM projects WHERE name = 'Website Redesign'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('PROJECT_CREATED',
     'Project ''Mobile App MVP'' was created',
     (SELECT id FROM projects WHERE name = 'Mobile App MVP'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('PROJECT_CREATED',
     'Project ''API Gateway Integration'' was created',
     (SELECT id FROM projects WHERE name = 'API Gateway Integration'),
     (SELECT id FROM users WHERE email = 'bob@example.com'));

INSERT INTO activity_events (event_type, description, task_id, project_id, actor_id)
VALUES
    ('TASK_STATUS_CHANGED',
     'Task ''Audit current website performance'' status changed from TODO to DONE',
     (SELECT id FROM tasks WHERE title = 'Audit current website performance'),
     (SELECT id FROM projects WHERE name = 'Website Redesign'),
     (SELECT id FROM users WHERE email = 'alice@example.com')),

    ('TASK_STATUS_CHANGED',
     'Task ''Set up React Native project scaffold'' status changed from IN_PROGRESS to DONE',
     (SELECT id FROM tasks WHERE title = 'Set up React Native project scaffold'),
     (SELECT id FROM projects WHERE name = 'Mobile App MVP'),
     (SELECT id FROM users WHERE email = 'bob@example.com'));
