CREATE TABLE "users"
(
    "id" SERIAL,
    "username" VARCHAR NOT NULL,
    "password" VARCHAR NOT NULL
);

INSERT INTO "users" (username, password) VALUES ("testusername", "testpassword");

