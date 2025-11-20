#!/bin/bash

echo "========================================="
echo "Configuring Firewall for Ricart-Agrawala"
echo "========================================="
echo ""

# Detect firewall type
if command -v firewall-cmd &> /dev/null && sudo firewall-cmd --state &> /dev/null; then
    echo "Detected: firewalld"
    echo "Opening ports..."
    sudo firewall-cmd --permanent --add-port=1099/tcp
    sudo firewall-cmd --permanent --add-port=2000-2009/tcp
    sudo firewall-cmd --reload
    echo "✓ Ports opened: 1099, 2000-2009"
    
elif command -v ufw &> /dev/null; then
    echo "Detected: ufw"
    echo "Opening ports..."
    sudo ufw allow 1099/tcp
    sudo ufw allow 2000:2009/tcp
    echo "✓ Ports opened: 1099, 2000-2009"
    
else
    echo "Detected: iptables (or no firewall)"
    echo "Opening ports with iptables..."
    sudo iptables -I INPUT -p tcp --dport 1099 -j ACCEPT
    sudo iptables -I INPUT -p tcp --dport 2000:2009 -j ACCEPT
    echo "✓ Ports opened: 1099, 2000-2009"
    echo ""
    echo "⚠ Note: iptables rules are not persistent."
    echo "   To make permanent, install iptables-persistent:"
    echo "   sudo pacman -S iptables"
    echo "   sudo iptables-save > /etc/iptables/iptables.rules"
fi

echo ""
echo "========================================="
echo "Verifying ports..."
echo "========================================="
sudo ss -tlnp | grep -E ":(1099|200[0-9])" || echo "No ports listening yet (this is OK - start registry server first)"

echo ""
echo "========================================="
echo "Testing connectivity..."
echo "========================================="
echo "Your IP addresses:"
ip -4 addr show | grep inet | awk '{print "  " $2}'

echo ""
echo "To test from another machine:"
echo "  telnet $(ip route get 1.1.1.1 | grep -oP 'src \K\S+') 1099"
echo "  nc -zv $(ip route get 1.1.1.1 | grep -oP 'src \K\S+') 1099"
echo ""
echo "✓ Firewall configured!"
