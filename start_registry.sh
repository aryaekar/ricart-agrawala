#!/bin/bash

echo "Starting RMI Registry on $(hostname -I | awk '{print $1}')"
echo "Registry IP: $(hostname -I | awk '{print $1}')"
echo "Registry Port: 1099"
echo ""

pkill -f rmiregistry 2>/dev/null
rmiregistry &
REGISTRY_PID=$!

echo "Registry started with PID: $REGISTRY_PID"
echo "Other machines can connect using:"
echo "  Registry IP: $(hostname -I | awk '{print $1}')"
echo "  Registry Port: 1099"
echo ""
echo "Press Ctrl+C to stop registry"
echo ""

trap "kill $REGISTRY_PID 2>/dev/null; echo 'Registry stopped'; exit" INT
wait $REGISTRY_PID
