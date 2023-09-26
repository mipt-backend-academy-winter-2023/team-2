CREATE TABLE "edge"
(
    "name" VARCHAR(80) PRIMARY KEY,
    "end1" REFERENCES node(id),
    "end2" REFERENCES node(id),
);

INSERT INTO "edge" (name, end1, end2)
VALUES ('street1', '1', '3'),
        ('street2', '2', '3'),
        ('street3', '4', '3');