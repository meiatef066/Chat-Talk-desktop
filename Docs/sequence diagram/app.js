const token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYWlhdGVmQGdtYWlsLmNvbSIsImlhdCI6MTc1MDUzNDg4NiwiZXhwIjoxNzUwNTM4NDg2fQ.GopuflzQPCDJ8MD6z4inu2-TMVOGJY6Yoi4sqVmJA6U";

const socket = new SockJS("http://localhost:8080/ws?token=" + token);
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: "Bearer " + token },
  function (frame) {
    console.log("✅ Connected: " + frame);

    // استقبلي طلبات الصداقة
    stompClient.subscribe("/user/topic/friend-request", function (message) {
      console.log("📨 Friend request received:", JSON.parse(message.body));
    });

    // استقبلي ردود الصداقة (accept/reject)
    stompClient.subscribe("/user/topic/friend-request-response", function (message) {
      console.log("🔁 Friend request response:", JSON.parse(message.body));
    });
  },
  function (error) {
    console.error("❌ WebSocket Error:", error);
  }
);

