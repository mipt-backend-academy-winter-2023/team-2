CREATE TABLE "edge"
(
    "id"     SERIAL,
    "label"  VARCHAR,
    "fromid" INTEGER,
    "toid"   INTEGER
);

CREATE TRIGGER update_distance ON edge FOR INSERT AS
BEGIN
    UPDATE edge SET distance = getdate() FROM my_table1 
END

INSERT INTO "edge" (label, fromid, toid)
VALUES ('street3', 2, 1),
       ('street2', 1, 2),
       ('street5', 2, 3),
       ('street9', 4, 3);
