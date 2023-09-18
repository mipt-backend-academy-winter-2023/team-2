CREATE TABLE "edge"
(
    "id"     SERIAL,
    "label"  VARCHAR,
    "fromid" INTEGER,
    "toid"   INTEGER,
    "distance" FLOAT
);

CREATE OR REPLACE FUNCTION func_update_distance() RETURNS TRIGGER AS
$$
BEGIN
    NEW.distance = (SELECT id FROM "node" WHERE id = 2);
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_distance BEFORE INSERT ON "edge" FOR EACH ROW EXECUTE FUNCTION func_update_distance();

INSERT INTO "edge" (label, fromid, toid)
VALUES ('street3', 2, 1),
       ('street2', 1, 2),
       ('street5', 2, 3),
       ('street9', 4, 3);
