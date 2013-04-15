#!/bin/bash
numServers=3
startPort=2777
for (( id = 0; id < $numServers; id++ ))
do
  java -cp target/paxos-1.0-jar-with-dependencies.jar edu.utexas.kkartal.chat.server.DefaultNode $id $startPort $numServers > server$id.txt 2>&1 &
done
