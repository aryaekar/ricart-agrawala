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

HOST_IP=$(get_ip)

echo "Detected IP: $HOST_IP"
echo "Starting RMI Registry..."
echo ""

# Compile if needed
if [ ! -f "RegistryServer.class" ]; then
    echo "Compiling..."
    javac RegistryServer.java
fi

# Run the Java-based registry which respects the hostname property
java -Djava.rmi.server.hostname=$HOST_IP \
     -Dregistry.port=1099 \
     RegistryServer
