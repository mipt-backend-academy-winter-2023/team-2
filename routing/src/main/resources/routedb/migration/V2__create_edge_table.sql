CREATE TABLE "edge"
(
    "id"     SERIAL,
    "label"  VARCHAR,
    "fromid" VARCHAR,
    "toid"   INTEGER
);

INSERT INTO "edge" (label, fromid, toid)
VALUES ('street1', 1, 2),
       ('street2', 2, 1);
