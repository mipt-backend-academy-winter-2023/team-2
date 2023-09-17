CREATE EXTENSION IF NOT EXISTS postgis;
CREATE TABLE edges
(
    id     INT PRIMARY KEY,
    name   VARCHAR,
    fromId INT NOT NULL,
    toId   INT NOT NULL
);

INSERT INTO edges (id, name, fromId, toId)
VALUES (0, 'Campus', 0, 1),
       (1, 'M-1', 1, 2);