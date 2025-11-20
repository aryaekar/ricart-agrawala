# Multi-Machine Setup Guide - FIXED VERSION

## Problem That Was Fixed
The original code had **"Registry.rebind disallowed; origin is non-local host"** error because Java RMI registries don't allow remote machines to call `rebind()` for security reasons.

## Solution
Created a **custom NodeRegistry service** that runs on the registry server and accepts remote node registration via proper RMI method calls.

---

## Setup Instructions for Multiple Machines

### Machine 1: Registry Server

```bash
cd ricart-agrawala
javac *.java
./start_registry_server.sh
```

You will see:
```
=========================================
Ricart-Agrawala Registry Server
=========================================
Registry IP: 192.168.137.37   <-- Note this IP!
Registry Port: 1099

Other machines can connect using:
  ./start_node.sh 192.168.137.37 <node_id>

Press Ctrl+C to stop the server
=========================================
```

**IMPORTANT**: Note the Registry IP displayed. Use this IP on all other machines.

---

### Machine 2: Node 0

```bash
cd ricart-agrawala
javac *.java
./start_node.sh 192.168.137.37 0
```

When prompted:
```
Enter node ID (0-9): 0
```

You should see:
```
Connected to existing RMI registry at 192.168.137.37:1099
Connected to existing NodeRegistry service
Node 0 registered successfully
Waiting 3 seconds for other nodes to connect...
```

---

### Machine 3: Node 1

```bash
cd ricart-agrawala
javac *.java
./start_node.sh 192.168.137.37 1
```

When prompted:
```
Enter node ID (0-9): 1
```

---

### Machine 4: Node 2

```bash
cd ricart-agrawala
javac *.java
./start_node.sh 192.168.137.37 2
```

When prompted:
```
Enter node ID (0-9): 2
```

---

## What Changed in the Code

### Old Code (BROKEN):
```java
registry.rebind("Node" + nodeId, node);  // ❌ Fails from remote machine
Node otherNode = registry.lookup("Node" + targetNodeId);
```

### New Code (WORKING):
```java
nodeRegistry.registerNode(nodeId, stub);  // ✅ Works from any machine
Node otherNode = nodeRegistry.getNode(targetNodeId);
```

---

## Architecture

```
Machine 1 (Registry Server)
├── RMI Registry (port 1099)
└── NodeRegistry Service (RMI object)
    └── Stores: Map<Integer, Node>

Machine 2 (Node 0)
├── NodeImpl (local object)
├── Export as RMI stub (port 2000)
└── Call: nodeRegistry.registerNode(0, stub)

Machine 3 (Node 1)
├── NodeImpl (local object)
├── Export as RMI stub (port 2001)
└── Call: nodeRegistry.registerNode(1, stub)
```

---

## Ports Used

- **1099**: RMI Registry
- **2000**: Node 0
- **2001**: Node 1
- **2002**: Node 2
- etc...

---

## Firewall Configuration (If Needed)

### Linux (iptables):
```bash
sudo iptables -A INPUT -p tcp --dport 1099 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 2000:2009 -j ACCEPT
```

### Linux (firewalld):
```bash
sudo firewall-cmd --permanent --add-port=1099/tcp
sudo firewall-cmd --permanent --add-port=2000-2009/tcp
sudo firewall-cmd --reload
```

---

## Troubleshooting

### "NodeRegistry service not found"
- Make sure you started the registry server first using `./start_registry_server.sh`
- The registry server must be running before starting any nodes

### "Connection refused"
- Check if firewall allows ports 1099 and 2000-2009
- Verify you're using the correct registry IP
- Ping the registry server: `ping 192.168.137.37`

### Nodes can't discover each other
- Wait 2-3 seconds - nodes refresh connections every 2 seconds
- Check that each node has a unique ID (0-9)
- Verify all nodes are using the same registry IP

---

## Testing on Single Machine (For Development)

You can test with multiple terminals on one machine:

**Terminal 1:**
```bash
./start_registry_server.sh
```

**Terminal 2:**
```bash
./start_node.sh localhost 0
# Enter: 0
```

**Terminal 3:**
```bash
./start_node.sh localhost 1
# Enter: 1
```

**Terminal 4:**
```bash
./start_node.sh localhost 2
# Enter: 2
```

---

## Files Modified/Created

### New Files:
- ✅ `NodeRegistry.java` - RMI interface for node registry
- ✅ `NodeRegistryImpl.java` - Implementation with ConcurrentHashMap
- ✅ `start_registry_server.sh` - Script to start registry server

### Modified Files:
- ✅ `RicartAgrawalaApp.java` - Uses NodeRegistry instead of direct registry operations
- ✅ `start_node.sh` - Already had good IP detection

---

## Quick Reference Commands

### Start Registry Server:
```bash
./start_registry_server.sh
```

### Start Node on Remote Machine:
```bash
./start_node.sh <REGISTRY_IP> <NODE_ID>
```

### Stop Everything:
```bash
# Press 'q' and Enter on each node terminal
# Press Ctrl+C on registry server terminal
```

### Clean Up:
```bash
pkill -f RicartAgrawalaApp
rm *.class
```

---

## Success Indicators

✅ Registry server shows: "NodeRegistry service created and registered"
✅ Nodes show: "Connected to existing NodeRegistry service"
✅ Nodes show: "Node X registered successfully"
✅ Nodes show: "Node X now has Y connections"
✅ Algorithm runs: "*** ENTERED CRITICAL SECTION ***"

---

## Common Errors FIXED

❌ **"Registry.rebind disallowed; origin is non-local host"**
✅ FIXED: Using custom NodeRegistry service

❌ **"Connection refused to host:"**
✅ FIXED: Proper java.rmi.server.hostname configuration

❌ **Nodes can't find each other**
✅ FIXED: NodeRegistry provides centralized node discovery
