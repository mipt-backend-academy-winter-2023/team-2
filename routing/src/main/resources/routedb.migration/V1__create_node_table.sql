CREATE TABLE "node"
(
    "id" SERIAL PRIMARY KEY,
    "is_intersection" BOOLEAN NOT NULL,
    "house_name"  VARCHAR,
    "lat" FLOAT NOT NULL,
    "lon" FLOAT NOT NULL,
);

INSERT INTO "node" (is_intersection, house_name, lat, lon)
VALUES ('false', 'house1', '2', '1'),
       ('false', 'house2', '-3', '4');

INSERT INTO "node" (is_intersection, lat, lon)
VALUES ('true', '0', '0'),
        ('false', '3', '-1');