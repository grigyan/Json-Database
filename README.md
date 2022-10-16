# JSON Database

## About ℹ️
Client-server application that allows the clients to store their data on the server in JSON format.

## Getting Started 🚀
To start the program you firstly need to run ```src/main/java/server/Main.java```.

After the server is up and running the client can make requests to the database which is stored on the server.

Database is stored in ```src/main/java/server/resources/db.json``` file.

Client can make three types of requests: ```get```, ```set``` and ```delete```.

## Making requests to the server 🧐

1. Pass the request parameters as a program variable to ```client/Main.java```. Provide request type, key, and value.
Example requests: 
   - ```-t set -k name -v "John Doe"```
   - ```-t get -k name```
   - ```-t delete -k name```
   - ```-t exit```

    ```-t``` is the type of the request. ```-k``` is the key of JSON object. ```-v``` is the value of the JSON object.

2. Pass the request as a JSON object inside ```src/main/java/client/resources/inputJson.txt``` (or any other .txt or .json
file inside ```resources``` directory) and as a program variable to ```client/Main.java``` pass the filename in the following
format: ```-in inputJson.txt```

Note that file name should be different if you want to use other file for making requests.

<h2 align="center">
Example
</h2>

The greater-than symbol followed by a space ```(> )``` represents the user input.
Note that it's not part of the input.
### Starting the server:
```
> java Main
Server started!
```
### Starting the clients:
```
> java Main -t set -k 1 -v "Hello world!" 
Client started!
Sent: {"type":"set","key":"1","value":"Hello world!"}
Received: {"response":"OK"}
```
```
> java Main -in setFile.json 
Client started!
Sent:
{
   "type":"set",
   "key":"person",
   "value":{
      "name":"Elon Musk",
      "car":{
         "model":"Tesla Roadster",
         "year":"2018"
      },
      "rocket":{
         "name":"Falcon 9",
         "launches":"87"
      }
   }
}
Received: {"response":"OK"}
```
```
> java Main -in getFile.json 
Client started!
Sent: {"type":"get","key":["person","name"]}
Received: {"response":"OK","value":"Elon Musk"}
```
```
> java Main -in updateFile.json 
Client started!
Sent: {"type":"set","key":["person","rocket","launches"],"value":"88"}
Received: {"response":"OK"}
```
```
> java Main -in secondGetFile.json 
Client started!
Sent: {"type":"get","key":["person"]}
Received:
{
   "response":"OK",
   "value":{
      "name":"Elon Musk",
      "car":{
         "model":"Tesla Roadster",
         "year":"2018"
      },
      "rocket":{
         "name":"Falcon 9",
         "launches":"88"
      }
   }
}
```
```
> java Main -in deleteFile.json 
Client started!
Sent: {"type":"delete","key":["person","car","year"]}
Received: {"response":"OK"}
```
```
> java Main -in secondGetFile.json 
Client started!
Sent: {"type":"get","key":["person"]}
Received:
{
   "response":"OK",
   "value":{
      "name":"Elon Musk",
      "car":{
         "model":"Tesla Roadster"
      },
      "rocket":{
         "name":"Falcon 9",
         "launches":"88"
      }
   }
}
```
```
> java Main -t exit 
Client started!
Sent: {"type":"exit"}
Received: {"response":"OK"}
```