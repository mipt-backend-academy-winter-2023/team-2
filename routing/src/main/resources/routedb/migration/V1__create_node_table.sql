CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE "node"
(
    "id"    SERIAL,
    "category" VARCHAR NOT NULL,
    "name"  VARCHAR,
    "location" geography(POINT, 4326)
);

INSERT INTO "node" (category, name, location)
VALUES ('0', 'house1', 'SRID=4326;POINT(-110 30)'::geography),
       ('1', 'intersection1', 'SRID=4326;POINT(-80 70)'::geography),
       ('1', 'intersection2', 'SRID=4326;POINT(-40 100)'::geography),
       ('0', 'house2', 'SRID=4326;POINT(-70 60)'::geography);
