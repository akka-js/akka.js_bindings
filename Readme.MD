
# Akka.Js Bindings

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
var akkajs = require("akkajs")

var system = akkajs.ActorSystem.create()
var pinger = system.spawn(
    new akkajs.Actor(
        function (msg) {
            if (msg == "ping") {
                console.log("RECEIVED PING")
                this.sender().tell("pong")
            }
        }
    )
)

var ponger = system.spawn(
    new akkajs.Actor(
        function (msg) {
            if (msg == "pong") {
                console.log("RECEIVED PONG")
                this.sender().tell("ping")
            }
        }
    )
)

pinger.tell("ping", ponger)
```

An Actor based greeter on Node looks like follows:

```javascript
const akkajs = require(`akkajs`)
const readline = require(`readline`)

var system = akkajs.ActorSystem.create(`helloworld`)

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
