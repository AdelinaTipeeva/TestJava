CREATE TABLE file
(
    id            SERIAL PRIMARY KEY,
    title         VARCHAR UNIQUE NOT NULL,
    creation_date TIMESTAMP      NOT NULL,
    description   VARCHAR,
    content       VARCHAR
);