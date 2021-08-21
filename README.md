# Distributed_Chat
A Distributed Peer-to-Peer chat application, created for CS 6650 Project

## How to run:

The files have to be run in the following sequence -
1. StartAdmin -
2. StartBroker
3. StartClient

## Easiest way to run the project -
1. Run the Admin Cluster in the Run options
2. Run the Broker Cluster in the Run options
3. Run any two of Client 1, Client 2 and Client 3, or all of them. Check the Client instructions
 on how to chat with other user.

The specifications on how to run each of them is as follows -

## 1. Admin -
The admin takes the following arguments -
 a. Port - Port where the admin RMI registry will be running
 b. Priority - Priority of the admin for the Bully election

 For the Bully election algo, the information of all admin instances, their host,port and priority
 is stored in the admin-config.json file in the resources folder.

## 2. Broker -
The Broker takes the following arguments -
  a. Host of the admin server - Specify the host of the admin server to connect to.
  b. Port of admin server - Port where the admin rmi is running
  c. Broker port - Port where the Broker RMI registry will run

## 3. Client
The Client takes the following arguments -
  a. Username - Username of the user in the chat
  b. Server Host - Host address of the admin server
  c. Server Port - Port where the admin RMI registry is running
  d. Client RMI Port - Port where the client RMI is running

How to Chat -
1. Enter the username of the user to send message to in the console, i.e. the username passed to the program argument.Client
2. On the next line enter the message to send

By default the Client 1, 2 and 3 have their usernames set to user1, user2 and user3 respectively
in the program arguments.