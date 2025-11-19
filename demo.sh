#!/bin/bash

echo "=== Ricart-Agrawala Demo (30 seconds) ==="
echo ""

pkill -f rmiregistry 2>/dev/null

echo "Compiling..."
javac *.java

if [ $? -eq 0 ]; then
    rmiregistry &
    REGISTRY_PID=$!
    sleep 2
    
    echo "Starting simulation with 3 nodes for 30 seconds..."
    echo ""
    
    (
        sleep 1
        echo "3"
        sleep 30
        echo "q"
    ) | java RicartAgrawalaApp
    
    sleep 2
    
    echo ""
    echo "Demo completed!"
    echo "Stopping RMI registry..."
    kill $REGISTRY_PID 2>/dev/null
    wait $REGISTRY_PID 2>/dev/null
else
    echo "Compilation failed!"
    exit 1
fi

echo "Done."
