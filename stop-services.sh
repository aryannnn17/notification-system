#!/bin/bash

echo "🛑 Stopping Scalable Notification System Services..."
echo "=================================================="

# Stop all Java processes (Spring Boot services)
echo "🔧 Stopping microservices..."
pkill -f "spring-boot:run" || true

# Kill any processes on required ports
echo "🧹 Cleaning up port processes..."
for port in 8080 8081 8082 8083 8090 8091; do
    lsof -ti:$port | xargs kill -9 2>/dev/null || true
done

# Stop Docker services
echo "📦 Stopping infrastructure services..."
docker compose down

echo ""
echo "✅ All services stopped successfully!"
echo "=================================================="
echo "📝 To restart: ./start-services.sh"
echo "🧹 To clean logs: rm -rf logs/"
