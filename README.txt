mvn package
java -cp target/paxos-1.0-jar-with-dependencies.jar edu.utexas.kkartal.chat.client.DefaultClient [startPort] [numServers]
java -cp target/paxos-1.0-jar-with-dependencies.jar edu.utexas.kkartal.chat.server.DefaultNode [serverId] [startPort] [numServers]

First compile the project with maven, compiles on chastity.cs.utexas.edu with 'mvn package'. then use the following commands to run a client where [startPort] is the startPort of the servers and [numServers] is the number of servers you are running.
The second command is to run a server node where [serverId] is the server's id. [numServers] is the number of servers participating in the protocol (all servers that are alive or dead). and [startPort] is the listening port of the first server (the other servers will listen on ports startPort+id so make sure you pick an appropriate port.)

That should be all you need to get it running!

