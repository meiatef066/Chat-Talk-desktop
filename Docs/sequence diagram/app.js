const token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYWlhdGVmQGdtYWlsLmNvbSIsImlhdCI6MTc1MTIzNzc1NiwiZXhwIjoxNzUxMjQxMzU2fQ.H9zUhLF4ztzF3NW9KEL6FPh6m-xBJfrAvzY7luFOpQM";

const socket = new SockJS("http://localhost:8080/ws?token=" + token);
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: "Bearer " + token },
  function (frame) {
    console.log("✅ Connected: " + frame);

    // استقبلي طلبات الصداقة
    stompClient.subscribe("/user/queue/friend-request", function (message) {
      console.log("📨 Friend request received:", JSON.parse(message.body));
    });

    // استقبلي ردود الصداقة (accept/reject)
    stompClient.subscribe("/user/queue/friend-request-response", function (message) {
      console.log("🔁 Friend request response:", JSON.parse(message.body));
    });
  },
  function (error) {
    console.error("❌ WebSocket Error:", error);
  }
);

