#!/bin/bash

echo "====================================="
echo "Validating Ricart-Agrawala Setup"
echo "====================================="
echo ""

# Check Java
echo -n "1. Checking Java... "
if command -v java >/dev/null 2>&1; then
    echo "✓"
else
    echo "✗ Java not found!"
    exit 1
fi

# Check compilation
echo -n "2. Checking compilation... "
if [ -f "NodeRegistry.class" ] && [ -f "NodeRegistryImpl.class" ] && [ -f "RicartAgrawalaApp.class" ]; then
    echo "✓"
else
    echo "⚠ Not compiled. Compiling..."
    javac *.java
    if [ $? -eq 0 ]; then
        echo "   ✓ Compilation successful"
    else
        echo "   ✗ Compilation failed!"
        exit 1
    fi
fi

# Check scripts
echo -n "3. Checking scripts... "
if [ -f "start_registry_server.sh" ] && [ -x "start_registry_server.sh" ]; then
    echo "✓"
else
    echo "⚠ start_registry_server.sh missing or not executable"
    chmod +x start_registry_server.sh 2>/dev/null
fi

# Check ports
echo "4. Checking ports..."
for PORT in 1099 2000 2001 2002; do
    if lsof -i :$PORT > /dev/null 2>&1; then
        echo "   ⚠ Port $PORT is in use"
    else
        echo "   ✓ Port $PORT available"
    fi
done

# Get IP
echo ""
echo "Your IP address:"
if command -v python3 >/dev/null 2>&1; then
    IP=$(python3 -c 'import socket; s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM); s.connect(("8.8.8.8", 80)); print(s.getsockname()[0]); s.close()' 2>/dev/null)
elif ip route get 1.1.1.1 >/dev/null 2>&1; then
    IP=$(ip route get 1.1.1.1 2>/dev/null | grep -oP 'src \K\S+')
else
    IP="Unable to detect"
fi
echo "  $IP"

echo ""
echo "====================================="
echo "✓ Setup validated successfully!"
echo "====================================="
echo ""
echo "To start:"
echo "  Registry Server: ./start_registry_server.sh"
echo "  Node:            ./start_node.sh <REGISTRY_IP> <NODE_ID>"
echo ""
