CREATE TABLE "User"(
    "Id" UUID NOT NULL,
    "first name" VARCHAR(255) NOT NULL,
    "last name" VARCHAR(255) NOT NULL,
    "email" VARCHAR(255) NOT NULL,
    "password" VARCHAR(255) NOT NULL,
    "phone number" VARCHAR(255) NOT NULL,
    "country" VARCHAR(255) NOT NULL,
    "is online" BOOLEAN NOT NULL,
    "gender" VARCHAR(255) CHECK
        ("gender" IN('')) NOT NULL,
        "profilePicture" VARCHAR(255) NOT NULL,
        "address" VARCHAR(255) NOT NULL,
        "bio" VARCHAR(255) NOT NULL,
        "role" VARCHAR(255)
    CHECK
        ("role" IN('')) NOT NULL,
        "created at" DATE NOT NULL
);
ALTER TABLE
    "User" ADD PRIMARY KEY("Id");
CREATE TABLE "contact"(
    "id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "contact_id" BIGINT NOT NULL,
    "contact status" VARCHAR(255) CHECK
        (
            "contact status" IN('PENDING', 'ACCEPTED', 'BLOCKED')
        ) NOT NULL,
        "created at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "contact" ADD PRIMARY KEY("id");
COMMENT
ON COLUMN
    "contact"."user_id" IS 'The owner of the contact list';
CREATE TABLE "Message"(
    "id" BIGINT NOT NULL,
    "chat" BIGINT NOT NULL,
    "sender" BIGINT NOT NULL,
    "content" TEXT NOT NULL,
    "message_type" VARCHAR(255) CHECK
        ("message_type" IN('')) NOT NULL,
        "is read" BOOLEAN NOT NULL,
        "sent at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "Message" ADD PRIMARY KEY("id");
CREATE TABLE "chat"(
    "id" BIGINT NOT NULL,
    "name" VARCHAR(30) NOT NULL,
    "is group" BOOLEAN NOT NULL,
    "create at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "chat" ADD PRIMARY KEY("id");
CREATE TABLE "chat participation"(
    "id" BIGINT NOT NULL,
    "user id" BIGINT NOT NULL,
    "chat id" BIGINT NOT NULL,
    "join at" TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
);
ALTER TABLE
    "chat participation" ADD PRIMARY KEY("id");
ALTER TABLE
    "chat participation" ADD CONSTRAINT "chat participation_chat id_foreign" FOREIGN KEY("chat id") REFERENCES "chat"("id");
ALTER TABLE
    "chat participation" ADD CONSTRAINT "chat participation_user id_foreign" FOREIGN KEY("user id") REFERENCES "User"("Id");
ALTER TABLE
    "Message" ADD CONSTRAINT "message_chat_foreign" FOREIGN KEY("chat") REFERENCES "chat"("id");
ALTER TABLE
    "contact" ADD CONSTRAINT "contact_user_id_foreign" FOREIGN KEY("user_id") REFERENCES "User"("Id");