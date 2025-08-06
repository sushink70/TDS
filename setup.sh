
#!/bin/bash
# setup.sh - Kafka Setup Script

set -e

echo "🚀 Setting up Kafka for Threat Detector..."

# Configuration
KAFKA_VERSION="2.13-3.5.1"
KAFKA_DIR="kafka_$KAFKA_VERSION"
ZOOKEEPER_PORT=2181
KAFKA_PORT=9092

# Download and extract Kafka if not exists
if [ ! -d "$KAFKA_DIR" ]; then
    echo "📥 Downloading Kafka..."
    wget "https://downloads.apache.org/kafka/3.5.1/$KAFKA_DIR.tgz"
    tar -xzf "$KAFKA_DIR.tgz"
    rm "$KAFKA_DIR.tgz"
fi

cd $KAFKA_DIR

echo "⚡ Starting Zookeeper..."
bin/zookeeper-server-start.sh config/zookeeper.properties &
ZOOKEEPER_PID=$!
sleep 10

echo "⚡ Starting Kafka..."
bin/kafka-server-start.sh config/server.properties &
KAFKA_PID=$!
sleep 15

echo "📝 Creating topics..."

# Create security-logs topic
bin/kafka-topics.sh --create \
    --topic security-logs \
    --bootstrap-server localhost:$KAFKA_PORT \
    --partitions 3 \
    --replication-factor 1 \
    --config retention.ms=604800000

# Create alerts topic
bin/kafka-topics.sh --create \
    --topic security-alerts \
    --bootstrap-server localhost:$KAFKA_PORT \
    --partitions 1 \
    --replication-factor 1

# List topics to verify
echo "📋 Topics created:"
bin/kafka-topics.sh --list --bootstrap-server localhost:$KAFKA_PORT

echo "✅ Kafka setup complete!"
echo "Zookeeper running on port $ZOOKEEPER_PORT (PID: $ZOOKEEPER_PID)"
echo "Kafka running on port $KAFKA_PORT (PID: $KAFKA_PID)"

# Function to stop services
cleanup() {
    echo "🛑 Stopping services..."
    kill $KAFKA_PID 2>/dev/null || true
    kill $ZOOKEEPER_PID 2>/dev/null || true
    exit 0
}

# Trap CTRL+C
trap cleanup INT

echo "Press CTRL+C to stop services..."
wait
