CREATE SEQUENCE message_id_seq start 1;

CREATE TABLE messages (
    id int8 not null default nextval('message_id_seq'),
    message_type int2 not null,
    sequence_number bigint not null unique,
    message_id varchar(255) not null,
    request_id varchar(255) not null,
    version varchar(10) not null,
    created_at timestamp not null,
    event_type varchar(255) not null,
    primary key (id)
);

ALTER SEQUENCE message_id_seq OWNED BY messages.id;

CREATE INDEX messages_sequence_number_idx
    ON messages USING btree (sequence_number);