#!/bin/bash

echo "=== Ricart-Agrawala Distributed Mutual Exclusion Algorithm ==="
echo ""

if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    exit 1
fi

pkill -f rmiregistry 2>/dev/null

echo "Compiling..."
javac *.java

if [ $? -eq 0 ]; then
    rmiregistry &
    REGISTRY_PID=$!
    sleep 2
    
    echo "Running application..."
    echo ""
    
    java RicartAgrawalaApp
    
    echo ""
    echo "Stopping RMI registry..."
    kill $REGISTRY_PID 2>/dev/null
    wait $REGISTRY_PID 2>/dev/null
else
    echo "Compilation failed!"
    exit 1
fi

echo "Done."
