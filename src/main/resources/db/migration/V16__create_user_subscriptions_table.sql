CREATE TABLE user_subscriptions (
                                    id BIGSERIAL PRIMARY KEY,
                                    stripe_customer_id VARCHAR(255) UNIQUE,
                                    stripe_subscription_id VARCHAR(255) UNIQUE,
                                    stripe_price_id VARCHAR(255),
                                    status VARCHAR(50) NOT NULL,
                                    subscription_end TIMESTAMP,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP NOT NULL,
                                    user_id BIGINT NOT NULL UNIQUE,
                                    CONSTRAINT fk_subscription_plan_user
                                        FOREIGN KEY (user_id) REFERENCES users(id)
);