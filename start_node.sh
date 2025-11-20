#!/bin/bash

# Function to get local IP
get_ip() {
    if command -v python3 >/dev/null 2>&1; then
        python3 -c 'import socket; s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM); s.connect(("8.8.8.8", 80)); print(s.getsockname()[0]); s.close()' 2>/dev/null
        return
    fi
    
    if hostname -I >/dev/null 2>&1; then
        hostname -I | awk '{print $1}'
        return
    fi
    
    if command -v ipconfig >/dev/null 2>&1; then
        ipconfig getifaddr en0
        return
    fi
    
    echo "127.0.0.1"
}

if [ $# -lt 1 ]; then
    echo "Usage: $0 <registry_ip> [node_id]"
    echo "Example: $0 192.168.1.100"
    echo "Example: $0 192.168.1.100 1"
    exit 1
fi

REGISTRY_IP=$1
NODE_ID=${2:-0}
HOST_IP=$(get_ip)

echo "Starting Node on $HOST_IP"
echo "Connecting to registry at: $REGISTRY_IP:1099"
echo "Node ID: $NODE_ID"
echo ""

# Compile if needed
if [ ! -f "RicartAgrawalaApp.class" ]; then
    echo "Compiling..."
    javac *.java
fi

echo "Starting Ricart-Agrawala Node in multi-machine mode..."
echo ""

java -Djava.rmi.server.hostname=$HOST_IP \
     -Dregistry.host=$REGISTRY_IP \
     -Dregistry.port=1099 \
     RicartAgrawalaApp multi
