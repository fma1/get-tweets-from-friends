CREATE DATABASE twitter;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    screen_name VARCHAR(30) UNIQUE NOT NULL
);

CREATE TABLE tweets (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    id_str VARCHAR,
    in_reply_to_status_id BIGINT,
    in_reply_to_status_id_str VARCHAR,
    in_reply_to_user_id BIGINT,
    in_reply_to_user_id_str VARCHAR,
    in_reply_to_screen_name VARCHAR,
    is_quote_status BOOLEAN,
    quoted_status_id BIGINT,
    quoted_status_id_str VARCHAR,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    favorite_count BIGINT,
    retweet_count BIGINT,
    retweeted BOOLEAN,
    source VARCHAR,
    text VARCHAR
);