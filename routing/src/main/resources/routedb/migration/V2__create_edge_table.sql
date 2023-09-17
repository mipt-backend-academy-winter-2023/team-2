CREATE TABLE "edge"
(
    "id"     SERIAL,
    "label"  VARCHAR,
    "fromid" INTEGER,
    "toid"   INTEGER
);

INSERT INTO "edge" (label, fromid, toid)
VALUES ('street3', 2, 1),
       ('street2', 1, 2),
       ('street5', 2, 3),
       ('street9', 4, 3);
