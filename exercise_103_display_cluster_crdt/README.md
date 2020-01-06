#CRDT data structures

Exercise represents akka cluster with CRDT that serves as distributed cache. 
Information is being updated on one node and then
spreads through cluster nodes eventually. Cache has data about network servers statuses.
For example: NodeA is UP and Node B is Down.

Exercise has two screens:
- First shows cluster node statuses
- Second shows CRDT data that is spreading through cluster with gossip protocol

To switch between screens you need to push the button


#Initialize CRDT data
To initialize data you need to run following. This will add information about nodes to cache

```
curl -d '{"status":"Up"}' -H "Content-Type: application/json" -X POST http://node-0:8080/status/NodeA
curl -d '{"status":"Up"}' -H "Content-Type: application/json" -X POST http://node-0:8080/status/NodeB
curl -d '{"status":"Up"}' -H "Content-Type: application/json" -X POST http://node-1:8080/status/NodeC
curl -d '{"status":"Down"}' -H "Content-Type: application/json" -X POST http://node-1:8080/status/NodeD
```

Notice that data is spread across cluster with delay that gives gossip protocols. Still CRDT is approach 
with high availability with eventual consistency.

To read Node status you need to run
`curl http://node-0:8080/status/NodeA`


