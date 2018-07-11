
# Akka.Js

[![NPM](https://nodei.co/npm/akkajs.png?compact=true)](https://nodei.co/npm/akkajs/)
[![Build Status](https://travis-ci.org/akka-js/akka.js_bindings.svg?branch=master)](https://travis-ci.org/akka-js/akka.js_bindings)

This project aim to bring the Akka Actor framework available for JS applications.

## Quick start

Install the module:

```
npm install akkajs
```

A simple Ping Pong between two actors looks like:

```javascript
const akkajs = require("akkajs")

const system = akkajs.ActorSystem.create()

class Pinger extends akkajs.Actor {
  constructor() {
    super()
    this.name = "pinger"
    this.receive = this.receive.bind(this)
  }
  receive(msg) {
    console.log("RECEIVED PING")
    this.sender().tell("pong")
  }
}

const pinger = system.spawn(new Pinger())

class Ponger extends akkajs.Actor {
  constructor() {
    super()
    this.name = "ponger"
    this.receive = this.receive.bind(this)
  }
  receive(msg) {
    console.log("RECEIVED PONG")
    this.sender().tell("ping")
  }
}

const ponger = system.spawn(new Ponger())

pinger.tell("ping", ponger)
```

An Actor based greeter on Node looks like follows:

```javascript
const akkajs = require("akkajs")
const readline = require("readline")

const system = akkajs.ActorSystem.create("helloworld")

class Greeter extends akkajs.Actor {
  constructor() {
     super()
     // following guideline in:  https://facebook.github.io/react/docs/react-without-es6.html#autobinding
     this.receive = this.receive.bind(this)
   }
   receive(msg) {
    console.log("Hello " + msg)
   }
}

const greeter = system.spawn(new Greeter())

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
})

rl.question(
  "What is your name? ",
  (answer) => {
    greeter.tell(answer)
    rl.close()
  }
)
```

Akka.Js is pure Javascript and has not got known limitations in running in the major browsers or directly on any JS VM (e.g. Node, Phantom, ...)

Into the github repo there are few examples under the `demo` folder.

To run the examples simply clone this project go into the `demo` directory and run with:
```bash
https://github.com/akka-js/akka.js_bindings.git
cd akka.js_bindings/demo
node <example-name>
```

## Full API



### Configuration

`Configuration` class enable you to configure your `ActorSystem`.

Configuration is based on the [HOCON standard](https://github.com/typesafehub/config/blob/master/HOCON.md)
ported to JS through ScalaJs thanks to [SHOCON](https://github.com/unicredit/shocon)

The `Configuration` constructor accept an optional *String* as parameter that enable you to override parameters.

all defaults instantiation:
```javascript
const config = new akkajs.Configuration()
```

custom instantiation:
```javascript
const config = new akkajs.Configuration(`akka {
  loglevel = "DEBUG"
  stdout-loglevel = "DEBUG"
}`)
```

__NOTE:__ by not using the default configuration you may incur in longer `ActorSystem` startup time.

the full default configuration can be found [here](https://github.com/akka-js/akka.js/blob/v1.2.5.6/akka-js-actor/js/src/main/resources/application.conf)

___Methods___

|Method|Description|
|-------|----------|
|`config.add(moreconfig)`| To add additional customizations to your configuration (e.g. `config.add("akka.log-config-on-start = on")`) |
|`config.get()`| To obtain the configuration object to construct an `ActorSytem` |


### ActorSystem

An `ActorSystem` is a container and the environment for your Actors.

The `ActorSystem` creator method accept an optional *String* as name and an optional configuration.

all defaults instantiation:
```javascript
const system = akkajs.ActorSystem.create()
```

with custom name:
```javascript
const system = akkajs.ActorSystem.create("myActorSystem")
```

with custom name and configuration:
```javascript
const system = akkajs.ActorSystem.create("myActorSystem", config.get())
```

___Methods___

|Method|Description|
|-------|----------|
|`system.terminate()`| Shutdown the `ActorSystem` |
|`system.select(actorpath)`| To obtain a reference to an `Actor` by it's path |
|`system.spawn(actor)`| Will spawn a new instance of the provided `Actor` and return the reference to it |



### Actor

An `Actor` is is an execution unit that runs concurrently with other actors.

You have to define a class that `extends akkajs.Actor` and use the `spawn` method on an instance of it to run your `Actor` and obtain the reference to it.

```javascript
class Example extends akkajs.Actor {
  constructor() {
    super()
    this.receive = this.receive.bind(this)
  }
  receive(msg) {
    // do something when you receive a message
  }
}

const actor = system.spawn(new Example())
```

`Actors` talk each other only by message passing and you cannot call directly a method on them.
Given a reference to an `Actor` you can invoke the followings:

___Methods___

|Method|Description|
|-------|----------|
|`actor.path()`| Return a String representing a logical path associated to your actor, it can be used to identify an actor using the `select` method |
|`actor.tell(msg)`| Send to the `Actor` the specified `msg` |
|`actor.tell(msg, anotheractor)`| Send to the `Actor` the specified `msg` pretending the sender to be `anotheractor` |
|`actor.kill()`| Will kill the specified `actor` |

When defining an Actor class you can override a few definitions

___Methods___

|Method|Description|
|-------|----------|
|`this.name`| The name of the Actor will be used to construct the path |
|`this.receive(msg)`| This method gets triggered when dequeuing the mailbox of the actor |
|`this.preStart()`| Triggered once when the `Actor` is started (or re-started) |
|`this.postStop()`| Triggered once when the `Actor` is stopped |

an example class that override all of these looks like:

```javascript
class Example extends akkajs.Actor {
  constructor() {
    super()
    this.name = "newname"
    this.preStart = this.preStart.bind(this)
    this.receive = this.receive.bind(this)
    this.postStop = this.postStop.bind(this)
  }
  preStart() { console.log("starting " + this.name) }
  receive(msg) { console.log("received message: " + msg) }
  postStop() { console.log("stopping") }
}

// and you can trigger all of them by running:
const actor = system.spawn(new Example())
setTimeout(
  () => {
    actor.tell("hello world")
    actor.kill()
  }
)
```

From ___within___ the `Actor` itself you have access to a few more functions to call:

___Methods___

|Method|Description|
|-------|----------|
|`this.path()`| Return the path of the `Actor` |
|`this.parent()`| Return the array of the children of this `Actor` |
|`this.children()`| Return the array of the children of this `Actor` |
|`this.sender()`| Called within the `receive` method return the reference to the `Actor` that sent the message - inferring the sender has some limitations consider using explicit sender in the `tell` method to have it working reliably |
|`this.self()`| Return the reference to itself |
|`this.system()`| Return the `ActorSystem` this `Actor` belong to |
|`this.spawn(actor)`| Let spawn an `Actor` as a child of this one, return it's reference |
|`this.become(func)`| Change the current `receive` function to the argument `func` |



### Compile and contribute

You need to install Sbt to compile this project from sources.
Compile with
```bash
sbt compile
```

You can run Javascript test using:
```bash
npm test
```

and you can test examples with your modifications by running first:
```bash
sbt deploy
```

and running desired examples into the `demo` directory.
