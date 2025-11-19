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

#### Machine 1 (Registry + Node 0):
```bash
# Terminal 1 - Start RMI Registry
./start_registry.sh

# Terminal 2 - Start Node 0
./start_node.sh <MACHINE1_IP> 0
# Enter: 3
```

#### Machine 2 (Node 1):
```bash
./start_node.sh <MACHINE1_IP> 1
# Enter: 3
```

#### Machine 3 (Node 2):
```bash
./start_node.sh <MACHINE1_IP> 2
# Enter: 3
```

### Simulating Multi-Machine on Same Machine

```bash
# Terminal 1 (Registry + Node 0):
rmiregistry &
java -Djava.rmi.server.hostname=192.168.31.13 -Dregistry.host=192.168.31.13 RicartAgrawalaApp multi
# Enter: 0

# Terminal 2 (Node 1):
java -Djava.rmi.server.hostname=192.168.31.13 -Dregistry.host=192.168.31.13 RicartAgrawalaApp multi
# Enter: 1

# Terminal 3 (Node 2):
java -Djava.rmi.server.hostname=192.168.31.13 -Dregistry.host=192.168.31.13 RicartAgrawalaApp multi
# Enter: 2
```

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
