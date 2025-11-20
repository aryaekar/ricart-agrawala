#!/bin/bash

echo "========================================="
echo "Setting up firewall for Ricart-Agrawala"
echo "========================================="
echo ""

# Check if iptables exists
if command -v iptables >/dev/null 2>&1; then
    echo "Configuring iptables..."
    sudo iptables -C INPUT -p tcp --dport 1099 -j ACCEPT 2>/dev/null || sudo iptables -A INPUT -p tcp --dport 1099 -j ACCEPT
    sudo iptables -C INPUT -p tcp --dport 2000:2009 -j ACCEPT 2>/dev/null || sudo iptables -A INPUT -p tcp --dport 2000:2009 -j ACCEPT
    echo "✓ iptables rules added"
fi

# Check if firewalld exists
if command -v firewall-cmd >/dev/null 2>&1; then
    echo "Configuring firewalld..."
    sudo firewall-cmd --permanent --add-port=1099/tcp 2>/dev/null
    sudo firewall-cmd --permanent --add-port=2000-2009/tcp 2>/dev/null
    sudo firewall-cmd --reload 2>/dev/null
    echo "✓ firewalld rules added"
fi

# Check if ufw exists
if command -v ufw >/dev/null 2>&1; then
    echo "Configuring ufw..."
    sudo ufw allow 1099/tcp 2>/dev/null
    sudo ufw allow 2000:2009/tcp 2>/dev/null
    echo "✓ ufw rules added"
fi

echo ""
echo "========================================="
echo "✓ Firewall configured!"
echo "========================================="
echo ""
echo "Ports opened:"
echo "  1099      - RMI Registry"
echo "  2000-2009 - Node RMI ports"
echo ""
