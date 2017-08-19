const akka = require(`../lib/akkajs`)

//once you have an actor system you can finally spawn your Actors
const system = akka.ActorSystem.create(
  "3actors",
  new akka.Configuration(`akka {
    loglevel = "DEBUG"
    stdout-loglevel = "DEBUG"
  }`).get()
)

//First of all describe your Actor
class Shouter extends akka.Actor {
  constructor() {
    super()
    this.receive = this.receive.bind(this)
  }
  receive(msg) {
    console.log(msg.toUpperCase())
  }
}

//there are 3 main functions you wish to override in akka.Actor:

//receive (MANDATORY) executed everytime a message is received
//receive e.g. function(arg) { arg }

//preStart (optional) executed once before an Actor is started
//preStart e.g function() {}

//postStop (optional) excuted once before an Actor is stopped
//postStop e.g. function() {}

//Optionally you can assigna  custom name to your actor

//name (optional) explicit name for your actor
//name = "bho"

class Shouter2 extends akka.Actor {
  constructor() {
    super()
    this.name = "shouter2"
    this.receive = this.receive.bind(this)
    this.preStart = this.preStart.bind(this)
    this.postStop = this.postStop.bind(this)
  }
  preStart() {
    console.log(`${this.self().path()} is going to start`)
  }
  receive(msg) {
    console.log(msg.toUpperCase())
  }
  postStop() {
    console.log(`${this.self().path()} is going to stop`)
  }
}

//To spawn a top level actor you can call the spawn method on the ActorSystem
const shouter = system.spawn(new Shouter())

//Now you can send messages to your Actor using the tell method
shouter.tell("hey")

const shouter2 = system.spawn(new Shouter2())

shouter2.tell("hoy")

shouter2.kill()
