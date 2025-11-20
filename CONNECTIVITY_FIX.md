# 🔥 MULTI-MACHINE CONNECTIVITY FIX

## Problem Identified
Your machines are on the same WiFi but the other machine cannot connect to your registry.

## Root Causes Fixed:
1. ✅ **Firewall blocking ports** - Opened ports 1099 and 2000-2009
2. ✅ **RMI security policy missing** - Added security.policy file
3. ✅ **No connectivity testing** - Added diagnostic tools

---

## 🚀 QUICK FIX - FOLLOW THESE STEPS

### Step 1: On Registry Machine (Your Machine - 192.168.137.149)

```bash
# 1. Configure firewall (run once)
sudo ./setup_firewall.sh

# 2. Start registry server
./start_registry_server.sh
```

**You should see:**
```
✓ Firewall configured
Registry IP: 192.168.137.149
Registry Port: 1099
...
Registry server is running...
```

**Keep this terminal open!**

---

### Step 2: Test From Other Machine

On the **other machine**, test connectivity FIRST:

```bash
# Test if you can reach port 1099
nc -zv 192.168.137.149 1099

# Or use telnet
telnet 192.168.137.149 1099

# Can you ping?
ping -c 3 192.168.137.149
```

**Expected result:**
- nc/telnet: "Connection succeeded" or port is open
- ping: Replies received

**If it fails:**
- Check if both machines are on same WiFi network
- Check WiFi settings for "AP Isolation" (disable it)
- Try from your machine: `ping <other_machine_ip>`

---

### Step 3: On Other Machine - Start Node

```bash
# Copy these files to the other machine:
# - All .java and .class files
# - start_node.sh
# - security.policy

# On other machine:
./start_node.sh 192.168.137.149 0

# When prompted:
Enter node ID (0-9): 0
```

**You should see:**
```
✓ Registry is reachable
Connected to existing RMI registry at 192.168.137.149:1099
Connected to existing NodeRegistry service
Node 0 registered successfully
```

---

## 📋 Files Created/Modified

### New Files:
1. ✅ `security.policy` - RMI security policy (REQUIRED!)
2. ✅ `setup_firewall.sh` - Configure firewall automatically
3. ✅ `test_connectivity.sh` - Test network connectivity

### Modified Files:
1. ✅ `start_registry_server.sh` - Added security policy and firewall check
2. ✅ `start_node.sh` - Added connectivity test and security policy
3. ✅ `RicartAgrawalaApp.java` - Better logging for network issues

---

## 🔍 Diagnostic Commands

### On Registry Machine:

```bash
# Check if registry is running
./test_connectivity.sh

# Check if port is listening
sudo ss -tlnp | grep 1099

# Check firewall rules
sudo iptables -L INPUT -n | grep -E "1099|2000"

# See your IP addresses
ip -4 addr
```

### On Other Machine:

```bash
# Test port connectivity
nc -zv 192.168.137.149 1099

# Test ping
ping -c 3 192.168.137.149

# Check if you can resolve hostname
nslookup 192.168.137.149
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "Connection refused"
**Cause:** Registry is not running or firewall blocking

**Fix:**
```bash
# On registry machine:
sudo ./setup_firewall.sh
./start_registry_server.sh

# Verify it's listening:
sudo ss -tlnp | grep 1099
```

### Issue 2: "Connection timed out"
**Cause:** Network firewall or router blocking

**Fix:**
```bash
# Check WiFi AP Isolation setting (disable it)
# Try direct connection test:
ping 192.168.137.149

# If ping fails, check:
# - Both on same WiFi?
# - Router firewall settings?
# - WiFi guest network isolation?
```

### Issue 3: "NodeRegistry service not found"
**Cause:** Registry started without NodeRegistry service

**Fix:**
```bash
# Stop old registry:
pkill -f RicartAgrawalaApp

# Start new one:
./start_registry_server.sh
```

### Issue 4: "java.rmi.AccessException"
**Cause:** Missing security policy

**Fix:**
```bash
# Ensure security.policy exists:
ls -l security.policy

# If missing, create it:
cat > security.policy << 'EOF'
grant {
    permission java.security.AllPermission;
};
EOF
```

---

## 📡 Network Requirements

### Firewall Ports (Both Machines):
- **1099/tcp** - RMI Registry
- **2000-2009/tcp** - RMI Object ports (nodes 0-9)

### Network Configuration:
- ✅ Both machines on **same subnet** (192.168.137.x/24)
- ✅ No **AP Isolation** enabled on WiFi router
- ✅ No **guest network** restrictions
- ✅ Machines can **ping each other**

---

## ✅ Verification Checklist

On **Registry Machine** (192.168.137.149):
- [ ] Firewall configured: `sudo ./setup_firewall.sh`
- [ ] Registry running: `./start_registry_server.sh`
- [ ] Port listening: `sudo ss -tlnp | grep 1099`
- [ ] Diagnostics pass: `./test_connectivity.sh`

On **Other Machine**:
- [ ] Can ping registry: `ping -c 3 192.168.137.149`
- [ ] Port 1099 reachable: `nc -zv 192.168.137.149 1099`
- [ ] Has security.policy file: `ls security.policy`
- [ ] Node connects: `./start_node.sh 192.168.137.149 0`

---

## 🎯 Expected Successful Output

### Registry Machine:
```
Registry IP: 192.168.137.149
✓ Firewall configured
NodeRegistry service created and registered
Registry server is running...
Node 0 registered successfully
Node 1 registered successfully
```

### Other Machine (Node):
```
✓ Registry is reachable
Connected to existing RMI registry at 192.168.137.149:1099
Connected to existing NodeRegistry service
Node 0 registered successfully
Node 0 now has 1 connections
[Node0] Requesting critical section [timestamp:1]
[Node0] *** ENTERED CRITICAL SECTION ***
```

---

## 🆘 Still Not Working?

Run these commands and share the output:

### On Registry Machine:
```bash
./test_connectivity.sh
sudo ss -tlnp | grep 1099
sudo iptables -L INPUT -n | grep 1099
```

### On Other Machine:
```bash
ping -c 3 192.168.137.149
nc -zv 192.168.137.149 1099
traceroute 192.168.137.149
```

---

## 📞 Quick Command Reference

### Start Everything:
```bash
# Registry machine:
sudo ./setup_firewall.sh && ./start_registry_server.sh

# Other machines:
./start_node.sh 192.168.137.149 0
./start_node.sh 192.168.137.149 1
```

### Stop Everything:
```bash
# Press Ctrl+C on each terminal
# Or force kill:
pkill -f RicartAgrawalaApp
```

### Test Connectivity:
```bash
./test_connectivity.sh
```

---

**Your IP: 192.168.137.149**
**Your WiFi: wlo1 interface**

✅ **Firewall is configured!**
🚀 **Ready to test!**
