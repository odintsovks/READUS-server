-- V3: Content (Text) Service — branches, discussion branch/soft-delete, reaction targets, analytics events

-- 1. Branches
CREATE TABLE branches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO branches (name, slug, description) VALUES
    ('General', 'general', 'General discussion and off-topic conversation'),
    ('Help & Support', 'help-support', 'Ask questions and get help from the community'),
    ('Announcements', 'announcements', 'Official news and updates from the team');

CREATE INDEX idx_branches_slug ON branches (slug);

-- 2. Discussions: branch assignment + soft delete
ALTER TABLE discussions ADD COLUMN branch_id UUID REFERENCES branches(id) ON DELETE SET NULL;
CREATE INDEX idx_discussions_branch_id ON discussions (branch_id);
ALTER TABLE discussions ADD COLUMN deleted_at TIMESTAMP;
CREATE INDEX idx_discussions_active ON discussions (created_at DESC, id DESC) WHERE deleted_at IS NULL;

-- 3. Reactions: support discussion targets and multiple reaction types per user
ALTER TABLE reactions DROP CONSTRAINT reactions_user_id_message_id_key;
ALTER TABLE reactions ALTER COLUMN message_id DROP NOT NULL;
ALTER TABLE reactions ADD COLUMN target_type VARCHAR(20) NOT NULL DEFAULT 'MESSAGE';
ALTER TABLE reactions ADD COLUMN discussion_id UUID REFERENCES discussions(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX ux_reaction_message ON reactions (user_id, type, message_id) WHERE message_id IS NOT NULL;
CREATE UNIQUE INDEX ux_reaction_discussion ON reactions (user_id, type, discussion_id) WHERE discussion_id IS NOT NULL;
CREATE INDEX idx_reactions_discussion_id ON reactions (discussion_id);

-- 4. Analytics events (feed ranking input)
CREATE TABLE analytics_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(30) NOT NULL,
    entity_id UUID,
    page_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_analytics_events_user_id ON analytics_events (user_id);
