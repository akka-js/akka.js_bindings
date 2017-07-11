
# Akka.Js Bindings

[![NPM](https://nodei.co/npm/akkajs.png?compact=true)](https://nodei.co/npm/akkajs/)

*this project is EXPERIMENTAL*

This project aim to bring Akka powerful framework available for JS applications.

This implementation is to be considered "on-going" but feel free to file an issue if you require a specific Akka functionality that isn't yet exposed in the current API.

Feedbacks are highly appreciated!

## Use It

Install the module:

```
npm install akkajs
```

A simple Ping Pong between two actors looks like:

```javascript
const akkajs = require(`../lib/akkajs`)

const system = akkajs.ActorSystem.create()

class Pinger extends akkajs.Actor {
  constructor() {
    super()
    this.name = "pinger"
    this.receive = Pinger.receive.bind(this)
  }

  static receive(msg) {
    console.log("RECEIVED PING")
    this.sender().tell("pong")
  }
}

let pinger = system.spawn(new Pinger())

class Ponger extends akkajs.Actor {
  constructor() {
    super()
    this.name = "ponger"
    this.receive = Ponger.receive.bind(this)
  }

  static receive(msg) {
    console.log("RECEIVED PONG")
    this.sender().tell("ping")
  }
}

let ponger = system.spawn(new Ponger())

pinger.tell("ping", ponger)
```

An Actor based greeter on Node looks like follows:

```javascript
const akkajs = require(`akkajs`)
const readline = require(`readline`)

const system = akkajs.ActorSystem.create(`helloworld`)

class Greeter extends akkajs.Actor {
  constructor() {
     super()
     // like what is suggested https://facebook.github.io/react/docs/react-without-es6.html#autobinding
     this.receive = Greeter.prototype.receive.bind(this)
   }
   receive(msg) {
    console.log(`Hello ${msg}`)
   }
}

const greeter = system.spawn(new Greeter())

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
})

rl.question('What is your name? ', (answer) => {
  greeter.tell(answer)
  rl.close();
})
const greeter = system.spawn(
  new akkajs.Actor(
    function(name) {
      console.log(`Hello ${name}`)
    }
  )
)

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
})

rl.question('What is your name? ', (answer) => {
  greeter.tell(answer)
  rl.close();
})
```

You can use Akka.Js from Node or Browser.

## More examples

Into the github repository there are several examples under the `demo` folder.

### Compile and run examples

You need to install Sbt to compile from sources.
To run the examples simply clone this project and:
```bash
sbt deploy
cd demo
node <example-name>
```
