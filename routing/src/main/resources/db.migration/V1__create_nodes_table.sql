CREATE EXTENSION IF NOT EXISTS postgis;
CREATE TABLE nodes
(
    id        INT PRIMARY KEY,
    nodeType  INT   NOT NULL,
    name      VARCHAR,
    latitude  FLOAT NOT NULL,
    longitude FLOAT NOT NULL
);

INSERT INTO nodes (id, name, latitude, longitude, nodeType)
VALUES (0, 0, 'Zuzino Mipt', 55.65, 37.68),
       (1, 0, 'New Building Mipt', 55.93, 37.52),
       (2, 1, 'Nemiga Metro Station', 53.9, 27.55);