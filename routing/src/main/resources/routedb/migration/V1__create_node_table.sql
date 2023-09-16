CREATE EXTENSION postgis;

CREATE TABLE "node"
(
    "id"    SERIAL,
    "category" INT NOT NULL,
    "name"  VARCHAR,
    "location" geography(POINT, 4326)
);

INSERT INTO "node" (category, name, location)
VALUES (0, 'house1', 'SRID=4326;POINT(-110 30)'),
       (1, 'intersection1', 'SRID=4326;POINT(-80 70)');
