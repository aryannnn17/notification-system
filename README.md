# Scalable Notification System

A **microservices-based notification system** designed to handle email, SMS, and push notifications efficiently using **Apache Kafka** for event streaming, **Redis** for caching, and **MySQL** for data persistence.

## 🚀 Features

- **Multi-Channel Support**: Email, SMS, and Push Notifications
- **Priority-Based Processing**: Three priority levels (1=High, 2=Medium, 3=Low)
- **Microservices Architecture**: Independent services for scalability
- **API Gateway**: RESTful API for sending notifications
- **Message Queuing**: Apache Kafka for reliable message delivery
- **Database Storage**: MySQL for notification history and tracking
- **Caching**: Redis for template priorities and performance

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │───▶│ Notification    │───▶│     Kafka       │
│                 │    │    Service      │    │   (Topics)      │
└─────────────────┘    │   (Port 8080)   │    └─────────────────┘
                       └─────────────────┘              │
                                                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Email/SMS     │◀───│ Priority        │◀───│   Consumers     │
│   Providers     │    │   Processors    │    │   (3 Services)  │
└─────────────────┘    │   (Ports 8081-  │    └─────────────────┘
                       │    8083)        │
                       └─────────────────┘
```

## 📋 Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **Docker & Docker Compose**
- **MySQL 8.0+**
- **Redis 6.0+**

## 🛠️ Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/aryannnn17/scalable-notification-system.git
cd scalable-notification-system
```

### 2. Start Infrastructure Services
```bash
# Start Kafka, MySQL, and Redis using Docker Compose
docker compose up -d

# Verify services are running
docker ps
```

### 3. Start All Microservices
```bash
# Start Notification Service (Port 8080)
cd notificationservice && ./mvnw spring-boot:run &

# Start Priority Processors (Ports 8081-8083)
cd NotificationProcessorPriority1 && ./mvnw spring-boot:run &
cd NotificationProcessorPriority2 && ./mvnw spring-boot:run &
cd NotificationProcessorPriority3 && ./mvnw spring-boot:run &

# Start Consumer Services (Ports 8090-8092)
cd EmailConsumer && ./mvnw spring-boot:run &
cd SMSConsumer && ./mvnw spring-boot:run &
cd PushNConsumer && ./mvnw spring-boot:run &
```

### 4. Test the System
```bash
# Health check
curl http://localhost:8080/api/health

# Send a test notification
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
      "message": "Test notification message",
      "emailSubject": "Test Subject"
    }
  }'
```

## ⚙️ Configuration

### Environment Setup
The system is pre-configured to work with local instances:
- **Kafka**: `localhost:9092`
- **MySQL**: `localhost:3306/notification_system`
- **Redis**: `localhost:6379`

### API Keys (Optional)
For actual email/SMS delivery, update these files:

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

**Note**: The system works without API keys but actual delivery will fail until configured.

## 📊 API Endpoints

### Notification Service (Port 8080)
- `GET /api/health` - Health check
- `POST /api/send-notification` - Send notification

**Sample Request**:
```json
{
  "notificationPriority": 1,
  "channels": ["email", "sms"],
  "recipient": {
    "userId": "123",
    "userEmail": "user@example.com"
  },
  "content": {
    "usingTemplates": false,
    "message": "Your message here",
    "emailSubject": "Subject"
  }
}
```

## 🔧 Development

### Project Structure
```
scalable-notification-system/
├── notificationservice/           # API Gateway
├── NotificationProcessorPriority1/ # High Priority Processor
├── NotificationProcessorPriority2/ # Medium Priority Processor  
├── NotificationProcessorPriority3/ # Low Priority Processor
├── EmailConsumer/                # Email Service
├── SMSConsumer/                   # SMS Service
├── PushNConsumer/                 # Push Notification Service
├── docker-compose.yml            # Infrastructure
└── README.md                      # This file
```

### Building Services
```bash
# Build all services
mvn clean install

# Build specific service
cd notificationservice && mvn clean install
```

### Database Setup
The system automatically creates the `notification_system` database and required tables on first run.

## 🐳 Docker Services

The `docker-compose.yml` provides:
- **Kafka** (Port 9092) - Message broker
- **MySQL** (Port 3306) - Database
- **Redis** (Port 6379) - Caching

## 📝 Monitoring

- Check service logs for processing status
- Monitor Kafka topics: `priority-1`, `priority-2`, `priority-3`
- Database table: `notifications` stores all sent notifications

## 🚨 Troubleshooting

### Common Issues

1. **Port Conflicts**: Ensure ports 8080-8092 are available
2. **Kafka Connection**: Verify Kafka container is running
3. **Database Connection**: Check MySQL is accessible
4. **Redis Connection**: Ensure Redis container is running

### Reset System
```bash
# Stop all services
docker compose down
pkill -f "spring-boot:run"

# Restart infrastructure
docker compose up -d

# Restart applications
# (Follow step 3 in Quick Start)
```

## 📚 Learn More

- [System Design PPT](./presentation/scalable-notification-system-presentation.pptx)
- [Architecture Video](https://www.youtube.com/watch?v=ec-NCUGOI58&t)
- [Demo Video](https://www.youtube.com/watch?v=2gOpx4rR5gw)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.


