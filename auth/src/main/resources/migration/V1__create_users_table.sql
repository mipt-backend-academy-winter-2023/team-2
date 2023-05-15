CREATE TABLE "Users"
(
    "id" SERIAL,
    "username" VARCHAR(32) NOT NULL,
    "password" VARCHAR(32) NOT NULL
);

INSERT INTO "Users" (username, password)
VALUES ('user1', 'pass1'),
       ('user2', 'pass2');