CREATE EXTENSION postgis;

CREATE TABLE "house"
(
    "id"    SERIAL,
    "name"  VARCHAR NOT NULL,
    "point" geography(POINT, 4326)
);

INSERT INTO "house" (name, point)
VALUES ('house1', 'SRID=4326;POINT(-110 30)');
