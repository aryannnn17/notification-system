# Local Setup Instructions

## 🚀 Quick Setup Guide

### 1. Clone and Setup
```bash
# Clone the repository
git clone https://github.com/aryannnn17/scalable-notification-system.git
cd scalable-notification-system

# The project is ready to run locally!
```

### 2. Start Infrastructure
```bash
# Start Kafka, MySQL, Redis
docker compose up -d

# Verify services
docker ps
```

### 3. Start All Services
```bash
# Start all 7 microservices
./start-services.sh
```

### 4. Test the System
```bash
# Health check
curl http://localhost:8080/api/health

# Send test notification
curl -X POST http://localhost:8080/api/send-notification \
  -H "Content-Type: application/json" \
  -d '{
    "notificationPriority": 1,
    "channels": ["email"],
    "recipient": {
      "userId": "123",
      "userEmail": "test@example.com"
    },
    "content": {
      "usingTemplates": false,
      "message": "Test notification",
      "emailSubject": "Test Subject"
    }
  }'
```

## 🔧 Manual Service Startup

If you prefer to start services individually:

```bash
# Terminal 1: Notification Service
cd notificationservice && ./mvnw spring-boot:run

# Terminal 2: Priority Processor 1  
cd NotificationProcessorPriority1 && ./mvnw spring-boot:run

# Terminal 3: Priority Processor 2
cd NotificationProcessorPriority2 && ./mvnw spring-boot:run

# Terminal 4: Priority Processor 3
cd NotificationProcessorPriority3 && ./mvnw spring-boot:run

# Terminal 5: Email Consumer
cd EmailConsumer && ./mvnw spring-boot:run

# Terminal 6: SMS Consumer
cd SMSConsumer && ./mvnw spring-boot:run

# Terminal 7: Push Consumer
cd PushNConsumer && ./mvnw spring-boot:run
```

## 📊 Service Ports

| Service | Port | Purpose |
|---------|------|---------|
| NotificationService | 8080 | API Gateway |
| NotificationProcessorPriority1 | 8081 | High Priority |
| NotificationProcessorPriority2 | 8082 | Medium Priority |
| NotificationProcessorPriority3 | 8083 | Low Priority |
| EmailConsumer | 8090 | Email Service |
| SMSConsumer | 8091 | SMS Service |
| PushNConsumer | 8092 | Push Service |

## 🔑 API Configuration (Optional)

For actual email/SMS delivery, update:

**Email Service** (`EmailConsumer/src/main/java/.../EmailService.java`):
```java
private String SENDGRID_API_KEY = "your_sendgrid_api_key";
Email from = new Email("your_verified_sender_email");
```

**SMS Service** (`SMSConsumer/src/main/java/.../SmsService.java`):
```java
public final String ACCOUNT_SID = "your_twilio_account_sid";
public final String AUTH_TOKEN = "your_twilio_auth_token";
new PhoneNumber("your_twilio_phone_number");
```

## 🛠️ Troubleshooting

### Port Conflicts
```bash
# Kill all Java processes
pkill -f "spring-boot:run"

# Or kill specific ports
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9
# ... for all ports 8080-8092
```

### Reset Everything
```bash
# Stop all services
docker compose down
pkill -f "spring-boot:run"

# Restart
docker compose up -d
# Then restart services
```

### Check Logs
Each service logs to console. Check for:
- Kafka connection errors
- Database connection issues  
- Redis connection problems

## 📝 Notes

- System works without real API keys (just won't deliver actual emails/SMS)
- Database auto-creates on first run
- Kafka topics auto-created
- All services are pre-configured for local development
