# Ricart-Agrawala Distributed Mutual Exclusion Algorithm

Implementation of the Ricart-Agrawala algorithm for distributed mutual exclusion using Java RMI.

## Overview

This implementation provides a distributed mutual exclusion algorithm where multiple nodes coordinate access to a shared critical section without a central coordinator. The algorithm uses:

- **Logical clocks** (Lamport timestamps) for event ordering
- **Request-defer-reply** mechanism for coordination
- **Priority-based access** using timestamps and node IDs

## Files

- `Node.java` - RMI interface for node communication
- `NodeImpl.java` - Algorithm implementation with state management
- `RicartAgrawalaApp.java` - Main application and node coordination
- `Config.java` - Configuration parameters
- `Logger.java` - Logging utility
- `run.sh` - Local execution script
- `demo.sh` - 30-second local demo
- `start_registry.sh` - Start RMI registry for network
- `start_node.sh` - Start node on network

## Usage

### Single Machine (Local)

```bash
# Interactive mode
./run.sh

# Quick 30-second demo
./demo.sh
```

### Multiple Machines (Network)

In network mode, each process runs **one node**, and all nodes connect to a **single RMI registry** on Machine 1.

#### Machine 1 (Registry + Node 0):
```bash
# Terminal 1 - Start RMI Registry
./start_registry.sh

# Terminal 2 - Start Node 0 (node ID = 0)
./start_node.sh <MACHINE1_IP> 0
# When prompted: Enter node ID (0-9): 0
```

#### Machine 2 (Node 1):
```bash
./start_node.sh <MACHINE1_IP> 1
# When prompted: Enter node ID (0-9): 1
```

#### Machine 3 (Node 2):
```bash
./start_node.sh <MACHINE1_IP> 2
# When prompted: Enter node ID (0-9): 2
```

**Notes:**
- Replace `<MACHINE1_IP>` with the actual IP address of Machine 1 (the one running `start_registry.sh`), e.g. `192.168.137.37`.
- Each machine must use a **unique node ID** (0–9). The ID you type at the prompt must match the second argument to `start_node.sh`.
- The application now auto-detects each machine's outward-facing IP. If detection fails, override it explicitly with `-Dlocal.host=<THIS_MACHINE_IP>` (this also sets `java.rmi.server.hostname`).

### Simulating Multi-Machine on Same Machine

**Important:** Do NOT run `rmiregistry &` manually. The first node will automatically create the registry.

**Option 1: Using localhost (simplest for same-machine testing)**
```bash
# Terminal 1 (Node 0 - creates registry automatically):
java -Djava.rmi.server.hostname=localhost -Dregistry.host=localhost RicartAgrawalaApp multi
# Enter: 0

# Terminal 2 (Node 1 - connects to existing registry):
java -Djava.rmi.server.hostname=localhost -Dregistry.host=localhost RicartAgrawalaApp multi
# Enter: 1

# Terminal 3 (Node 2 - connects to existing registry):
java -Djava.rmi.server.hostname=localhost -Dregistry.host=localhost RicartAgrawalaApp multi
# Enter: 2
```

**Option 2: Using network IP (for actual network testing)**
```bash
# First, find your machine's IP address:
ipconfig getifaddr en0  # macOS
# or: hostname -I        # Linux

# Terminal 1 (Node 0 - creates registry automatically):
java -Djava.rmi.server.hostname=YOUR_IP -Dregistry.host=YOUR_IP RicartAgrawalaApp multi
# Enter: 0

# Terminal 2 (Node 1 - connects to existing registry):
java -Djava.rmi.server.hostname=YOUR_IP -Dregistry.host=YOUR_IP RicartAgrawalaApp multi
# Enter: 1

# Terminal 3 (Node 2 - connects to existing registry):
java -Djava.rmi.server.hostname=YOUR_IP -Dregistry.host=YOUR_IP RicartAgrawalaApp multi
# Enter: 2
```

**Note:** Replace `YOUR_IP` with your actual IP address (e.g., `192.168.137.37`). Using the wrong IP will cause connection failures.

### Manual Execution

```bash
# Compile
javac *.java

# Start RMI registry
rmiregistry &

# Run application (single machine mode)
java RicartAgrawalaApp

# Run application (multi-machine mode)
java RicartAgrawalaApp multi
```

## How It Works

1. Each node maintains a logical clock to order events
2. When requesting the critical section, a node:
   - Increments its logical clock
   - Broadcasts request to all other nodes with timestamp
   - Waits for replies from all other nodes
3. Nodes grant permission if they:
   - Are not requesting/in the critical section, OR
   - Have lower priority (higher timestamp or higher node ID)
4. Upon exiting the critical section, a node releases and sends deferred replies

## Configuration

Edit `Config.java` to adjust:

- `MIN_REQUEST_DELAY` / `MAX_REQUEST_DELAY` - Time between requests
- `MIN_CS_WORK_TIME` / `MAX_CS_WORK_TIME` - Time in critical section
- `MIN_NODES` / `MAX_NODES` - Number of nodes (2-10)

## Example Output

### Local (Single Machine):
```
[Node0][INFO] Requesting critical section [timestamp:1]
[Node1][INFO] Received request from Node 0 [timestamp:2]
[Node1][INFO] Grants permission to Node 0
[Node0][INFO] *** ENTERED CRITICAL SECTION *** [timestamp:1]
[Node0][INFO] *** EXITED CRITICAL SECTION *** [timestamp:1]
```

### Network (Multiple Machines):
```
# Machine 1:
[Node0][INFO] Requesting critical section [timestamp:1]
[Node0][INFO] *** ENTERED CRITICAL SECTION *** [timestamp:1]

# Machine 2:
[Node1][INFO] Received request from Node 0 [timestamp:2]
[Node1][INFO] Grants permission to Node 0
[Node1][INFO] Requesting critical section [timestamp:3]

# Machine 3:
[Node2][INFO] Received request from Node 0 [timestamp:3]
[Node2][INFO] Grants permission to Node 0
```

## Algorithm Properties

- **Mutual Exclusion**: Only one node can be in the critical section at a time
- **No Deadlocks**: Requests are granted based on priority
- **Fairness**: Nodes are served in priority order (timestamp, then node ID)
