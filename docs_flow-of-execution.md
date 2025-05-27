# Chat Application: End-to-End Flow of Execution

This document describes the **step-by-step flow** from user registration to real-time chat and notifications in a JavaFX + Spring Boot ("Telegram-like") chat application.

---

## 1. **User Registration**

### 1.1. **Frontend (JavaFX)**
- User fills out registration form (username/email, password, etc).
- On "Register" click, client sends an HTTP POST request to:  
  `POST /api/auth/register`
  - **Payload:** `{ "username": "...", "email": "...", "password": "..." }`

### 1.2. **Backend (Spring Boot)**
- Receives registration request, validates data.
- Hashes the password and saves user to the database.
- Responds with success/failure message (no JWT yet).

---

## 2. **User Login**

### 2.1. **Frontend**
- User enters credentials and clicks "Login".
- Client sends `POST /api/auth/login` with credentials.
- On success, backend responds with a **JWT token**.

### 2.2. **Backend**
- Authenticates user (checks hashed password).
- Generates and returns JWT.

### 2.3. **Frontend**
- Stores received JWT securely (in memory or encrypted on disk).
- All subsequent API and WebSocket requests will include this JWT for authentication.

---

## 3. **Fetching User List / Contact List**

### 3.1. **Frontend**
- Sends `GET /api/users` with JWT in `Authorization: Bearer <token>`.
- Receives and displays a list of users/contacts (excluding self).

### 3.2. **Backend**
- Validates JWT.
- Returns list of users.

---

## 4. **Fetching Chat History**

### 4.1. **Frontend**
- When a user selects a contact, sends:  
  `GET /api/messages?withUser=<contactId>`  
  with JWT for authentication.
- Receives list of previous messages (paginated).

### 4.2. **Backend**
- Validates JWT.
- Fetches and returns message history between the authenticated user and the selected contact.

---

## 5. **Establishing Real-Time Chat (WebSocket)**

### 5.1. **Frontend**
- After login, opens WebSocket connection to `/ws` endpoint:  
  e.g., `ws://<server>/ws?token=<JWT>`
- On connection, subscribes to personal topic/channel (e.g., `/topic/messages.{userId}`) for incoming messages.

### 5.2. **Backend**
- Verifies JWT in the WebSocket handshake.
- If valid, establishes session and subscribes user to their message topic.

---

## 6. **Sending a Chat Message**

### 6.1. **Frontend**
- When user sends a message:
  - Publishes to WebSocket destination (e.g., `/app/chat.sendMessage`), including recipient, message, and JWT.

### 6.2. **Backend**
- Receives the message, validates JWT.
- Stores message in the database.
- Broadcasts the message to:
  - The recipient's topic (`/topic/messages.{recipientId}`)
  - Optionally, the sender's topic (for echo/confirmation).

### 6.3. **Frontend (Recipient)**
- Receives the message instantly via WebSocket subscription.
- Displays new message in UI.

---

## 7. **Message Notifications**

### 7.1. **Frontend**
- Listens for new messages via WebSocket.
- When a message is received for a chat not currently open:
  - Triggers a desktop notification (JavaFX Notification/Tray, or in-app badge).
  - Optionally, plays sound.

### 7.2. **Backend**
- (Optional) Can send special notification messages for events: user online/offline, group invites, etc.

---

## 8. **Other Features**

### **a. Typing Indicators**
- Frontend sends "typing" status via WebSocket to a dedicated channel.
- Backend forwards typing events to other user(s).

### **b. Online/Offline Presence**
- Backend tracks connected WebSocket sessions.
- On connect/disconnect, updates presence and notifies contacts.

### **c. Group Chats**
- Messages sent to group topic (`/topic/group.{groupId}`).
- Backend routes and stores group messages accordingly.

---

## 9. **Logout Flow**

### 9.1. **Frontend**
- User clicks "Logout".
- JWT is deleted from client storage.
- WebSocket connection is closed.
- UI returns to login screen.

### 9.2. **Backend**
- (Optional) Maintains blacklist of invalidated JWTs until expiry, or simply relies on client to disconnect.

---

## 10. **Security Notes**

- **JWT must be sent on all REST and WebSocket connections**.
- Passwords must always be hashed before storing.
- All endpoints must validate JWT.
- Rate limit login/register endpoints to prevent brute force attacks.

---

## **Summary Timeline (Sequence)**

1. User registers (REST)
2. User logs in (REST, gets JWT)
3. User fetches contacts & history (REST, JWT)
4. User opens WebSocket with JWT
5. User sends/receives messages (WebSocket)
6. User receives notifications (WebSocket)
7. User logs out (JWT discarded, socket closed)

---

## **References**

- [Spring Boot WebSocket Chat Example](https://github.com/callicoder/spring-boot-websocket-chat-demo)
- [JavaFX Desktop Notifications Example](https://stackoverflow.com/questions/11269632/javafx-notification-popup)
- [JWT in WebSocket Handshake](https://www.baeldung.com/spring-security-websockets)

---
