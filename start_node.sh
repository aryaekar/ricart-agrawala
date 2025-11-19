#!/bin/bash

if [ $# -lt 1 ]; then
    echo "Usage: $0 <registry_ip> [node_id]"
    echo "Example: $0 192.168.1.100"
    echo "Example: $0 192.168.1.100 1"
    exit 1
fi

REGISTRY_IP=$1
NODE_ID=${2:-0}

echo "Starting Node on $(hostname -I | awk '{print $1}')"
echo "Connecting to registry at: $REGISTRY_IP:1099"
echo "Node ID: $NODE_ID"
echo ""

# Compile if needed
if [ ! -f "*.class" ]; then
    echo "Compiling..."
    javac *.java
fi

echo "Starting Ricart-Agrawala Node in multi-machine mode..."
echo ""

java -Djava.rmi.server.hostname=$(hostname -I | awk '{print $1}') \
     -Dregistry.host=$REGISTRY_IP \
     -Dregistry.port=1099 \
     RicartAgrawalaApp multi
