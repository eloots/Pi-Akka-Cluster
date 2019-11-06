#Explore sharding

Exercise introduces two screens:
- First shows cluster node statuses
- Second shows persistent entities that each node has in memory

To switch between screens you need to push the button

Nodes use JDBC/MySQL journal at 192.168.1.99 (login/password = akkapi/akkapi)

#Initialize shard data

```
curl -d '{"amount":10}' -H "Content-Type: application/json" -X POST http://node-0:8080/user/Alex
curl -d '{"amount":20}' -H "Content-Type: application/json" -X POST http://node-0:8080/user/Max
curl -d '{"amount":30}' -H "Content-Type: application/json" -X POST http://node-1:8080/user/Peter
curl -d '{"amount":40}' -H "Content-Type: application/json" -X POST http://node-1:8080/user/Vlad
```

#Get persistent data

`curl http://node-0:8080/user/Peter`

Read request can trigger rehidration of persistent actor

#Read state of cluster

`curl http://node-0:8558/cluster/members/ | jq`

Mark node as down
`curl -d 'operation=DOWN' -X PUT http://node-0:8558/cluster/members/akka-oled@192.168.1.102:2550`