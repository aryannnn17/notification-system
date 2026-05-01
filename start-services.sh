#!/bin/bash

echo "🚀 Starting Scalable Notification System Services..."
echo "=================================================="

# Check if Docker is running
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start infrastructure services
echo "📦 Starting infrastructure services..."
docker compose up -d

# Wait for services to be ready
echo "⏳ Waiting for infrastructure services to be ready..."
sleep 15

# Check if infrastructure is running
if ! docker ps | grep -q "kafka\|mysql\|redis"; then
    echo "❌ Infrastructure services failed to start."
    exit 1
fi

echo "✅ Infrastructure services are running!"

# Kill any existing Java processes on required ports
echo "🧹 Cleaning up existing processes..."
for port in 8080 8081 8082 8083 8090 8091; do
    lsof -ti:$port | xargs kill -9 2>/dev/null || true
done

# Create logs directory if it doesn't exist
mkdir -p logs

# Start all microservices in background
echo "🔧 Starting microservices..."

# Notification Service
cd notificationservice
./mvnw spring-boot:run > ../logs/notificationservice.log 2>&1 &
echo "✅ Notification Service started (Port 8080)"
cd ..

# Priority Processor 1
cd NotificationProcessorPriority1
./mvnw spring-boot:run > ../logs/processor1.log 2>&1 &
echo "✅ Priority Processor 1 started (Port 8081)"
cd ..

# Priority Processor 2
cd NotificationProcessorPriority2
./mvnw spring-boot:run > ../logs/processor2.log 2>&1 &
echo "✅ Priority Processor 2 started (Port 8082)"
cd ..

# Priority Processor 3
cd NotificationProcessorPriority3
./mvnw spring-boot:run > ../logs/processor3.log 2>&1 &
echo "✅ Priority Processor 3 started (Port 8083)"
cd ..

# Email Consumer
cd EmailConsumer
./mvnw spring-boot:run > ../logs/emailconsumer.log 2>&1 &
echo "✅ Email Consumer started (Port 8090)"
cd ..

# SMS Consumer
cd SMSConsumer
./mvnw spring-boot:run > ../logs/smsconsumer.log 2>&1 &
echo "✅ SMS Consumer started (Port 8091)"
cd ..

# Push Consumer removed - only email and SMS supported
# cd PushNConsumer
# ./mvnw spring-boot:run > ../logs/pushconsumer.log 2>&1 &
# echo "✅ Push Consumer started (Port 8092)"
# cd ..

# Create logs directory if it doesn't exist
mkdir -p logs

echo ""
echo "🎉 All services started successfully!"
echo "=================================================="
echo "📊 Service Status:"
echo "   Notification Service: http://localhost:8080/api/health"
echo "   Priority Processor 1: Port 8081"
echo "   Priority Processor 2: Port 8082"  
echo "   Priority Processor 3: Port 8083"
echo "   Email Consumer: Port 8090"
echo "   SMS Consumer: Port 8091"
# echo "   Push Consumer: Port 8092" - Removed
echo ""
echo "🧪 Test the system:"
echo "   curl http://localhost:8080/api/health"
echo ""
echo "📝 Logs are available in the 'logs' directory"
echo "🛑 To stop all services: ./stop-services.sh"
