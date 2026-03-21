ALTER TABLE user_subscriptions
    ADD COLUMN cancellation_scheduled boolean NOT NULL DEFAULT false;