#!/bin/bash

echo "========================================="
echo "Network Connectivity Diagnostic Tool"
echo "========================================="
echo ""

# Get local IP
LOCAL_IP=$(ip route get 1.1.1.1 2>/dev/null | grep -oP 'src \K\S+')
echo "Your IP: $LOCAL_IP"
echo ""

# Check if registry is running
echo "1. Checking if registry is running..."
if ps aux | grep -i "RicartAgrawalaApp registry" | grep -v grep > /dev/null; then
    echo "   ✓ Registry process is running"
else
    echo "   ✗ Registry is NOT running"
    echo "   → Start it with: ./start_registry_server.sh"
    echo ""
fi

# Check if port 1099 is listening
echo "2. Checking if port 1099 is listening..."
if ss -tlnp 2>/dev/null | grep :1099 > /dev/null || netstat -tlnp 2>/dev/null | grep :1099 > /dev/null; then
    echo "   ✓ Port 1099 is listening"
    ss -tlnp 2>/dev/null | grep :1099 || netstat -tlnp 2>/dev/null | grep :1099
else
    echo "   ✗ Port 1099 is NOT listening"
    echo "   → Registry server must be started first"
fi

echo ""
echo "3. Checking firewall rules..."
if sudo iptables -L INPUT -n | grep -E "1099|tcp dpt:2000:2009" > /dev/null 2>&1; then
    echo "   ✓ Firewall rules are configured"
else
    echo "   ⚠ Firewall rules may not be configured"
    echo "   → Run: sudo ./setup_firewall.sh"
fi

echo ""
echo "4. Network interfaces:"
ip -4 addr show | grep inet | awk '{print "   " $2 " (" $NF ")"}'

echo ""
echo "========================================="
echo "From OTHER machine, test connection with:"
echo "========================================="
echo ""
echo "# Test if registry port is reachable:"
echo "nc -zv $LOCAL_IP 1099"
echo ""
echo "# Or use telnet:"
echo "telnet $LOCAL_IP 1099"
echo ""
echo "# Test if you can ping:"
echo "ping -c 3 $LOCAL_IP"
echo ""
echo "========================================="
echo "To start nodes from other machine:"
echo "========================================="
echo ""
echo "./start_node.sh $LOCAL_IP 0   # For Node 0"
echo "./start_node.sh $LOCAL_IP 1   # For Node 1"
echo ""
