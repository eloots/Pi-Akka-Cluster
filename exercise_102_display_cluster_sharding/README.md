# Explore sharding

This exercise using installation with OLED displays. Instructions can be found [here](../docs/display/display-assembly-instructions.md)

Exercise introduces two screens:
- First one shows cluster node statuses
- Second one shows persistent entities that each node has in memory

To switch between screens you need to push the button

Nodes use JDBC/MySQL journal at 192.168.1.99 (login/password = akkapi/akkapi). This should be set up on your laptop.
To create DB schema yo uned to run [SQL script](https://github.com/akka/akka-persistence-jdbc/blob/master/core/src/test/resources/schema/mysql/mysql-schema.sql) 

# Initialize shard data

```
curl -d '{"points":10}' -H "Content-Type: application/json" -X POST http://node-0:8080/user/Alex
curl -d '{"points":20}' -H "Content-Type: application/json" -X POST http://node-0:8080/user/Max
curl -d '{"points":30}' -H "Content-Type: application/json" -X POST http://node-1:8080/user/Peter
curl -d '{"points":40}' -H "Content-Type: application/json" -X POST http://node-1:8080/user/Richard
curl -d '{"points":40}' -H "Content-Type: application/json" -X POST http://node-1:8080/user/Jim
curl -d '{"points":40}' -H "Content-Type: application/json" -X POST http://node-1:8080/user/Michele

```

# Get persistent data

`curl http://node-0:8080/user/Peter`

Read request can trigger rehidration of persistent actor.
This can be seen on display as quick updates of point number once system applies all updates from database to actor

# Read state of cluster

`curl http://node-0:8558/cluster/members/ | jq`

# Mark node as down

`curl -d 'operation=DOWN' -X PUT http://node-0:8558/cluster/members/akka-oled@node-2:2550`