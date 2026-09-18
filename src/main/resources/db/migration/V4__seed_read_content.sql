-- V4: Seed stable read content for load testing.
--
-- The Gatling load test splits traffic by branch: readers browse `general`,
-- writers create/delete in `announcements`. Seeding persistent discussions
-- with messages into `general` gives readers live, never-deleted content to
-- browse from t=0 (without it, the discussion list is empty until writers
-- happen to create data, and the newest-first list is dominated by ephemeral
-- writer discussions that get soft-deleted mid-read).

DO $$
DECLARE
    v_seed_user UUID;
    v_general   UUID;
    v_disc      UUID;
    v_msg       UUID;
    v_i         INT;
BEGIN
    INSERT INTO users (email, username, password_hash)
    VALUES ('seed@readus.local', 'seeded-moderator', '$2a$10$seeded-not-a-real-bcrypt-hash')
    RETURNING id INTO v_seed_user;

    SELECT id INTO v_general FROM branches WHERE slug = 'general';
    IF v_general IS NULL THEN
        RAISE EXCEPTION 'branch "general" not found - cannot seed read content';
    END IF;

    FOR v_i IN 1..50 LOOP
        INSERT INTO discussions (title, slug, content, content_html, user_id, branch_id, created_at)
        VALUES ('Seeded discussion #' || v_i,
                'seeded-discussion-' || v_i,
                'Stable seed content for load testing. #' || v_i,
                '<p>Stable seed content for load testing. #<b>' || v_i || '</b></p>',
                v_seed_user, v_general,
                NOW() - make_interval(mins => v_i * 7))
        RETURNING id INTO v_disc;

        INSERT INTO messages (content, content_html, moderation_status, user_id, discussion_id, created_at)
        VALUES ('First message of seeded discussion #' || v_i,
                '<p>First message of seeded discussion #<b>' || v_i || '</b></p>',
                0, v_seed_user, v_disc,
                NOW() - make_interval(mins => v_i * 7))
        RETURNING id INTO v_msg;

        UPDATE discussions SET last_message_id = v_msg WHERE id = v_disc;
    END LOOP;
END $$;
