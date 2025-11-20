#!/bin/bash

# Function to get local IP
get_ip() {
    if command -v python3 >/dev/null 2>&1; then
        python3 -c 'import socket; s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM); s.connect(("8.8.8.8", 80)); print(s.getsockname()[0]); s.close()' 2>/dev/null && return
    fi
    if command -v hostname >/dev/null 2>&1 && hostname -I >/dev/null 2>&1; then
        hostname -I | awk '{print $1}' && return
    fi
    if ip route get 1.1.1.1 >/dev/null 2>&1; then
        ip route get 1.1.1.1 2>/dev/null | grep -oP 'src \K\S+' && return
    fi
    echo "127.0.0.1"
}

LOCAL_IP=$(get_ip)

echo "========================================="
echo "Ricart-Agrawala Registry Server"
echo "========================================="
echo "Registry IP: $LOCAL_IP"
echo "Registry Port: 1099"
echo ""
echo "Compiling..."
javac *.java
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo ""
echo "Starting registry server with NodeRegistry service..."
echo ""
echo "Other machines can connect using:"
echo "  ./start_node.sh $LOCAL_IP <node_id>"
echo ""
echo "Press Ctrl+C to stop the server"
echo "========================================="
echo ""

java -Djava.rmi.server.hostname=$LOCAL_IP RicartAgrawalaApp registry
