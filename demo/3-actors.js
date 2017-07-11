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
    this.receive = Shouter.receive.bind(this);
  }

  static receive(msg) {
    console.log(msg.toUpperCase())
  }
}

//you can override 4 functions in akka.Actor:

//receive (MANDATORY) executed everytime a message is received
//receive e.g. function(arg) { arg }

//preStart (optional) executed once before an Actor is started
//preStart e.g function() {}

//postStop (optional) excuted once before an Actor is stopped
//postStop e.g. function() {}

//name (optional) explicit name for your actor
//name = "bho"

//To spawn a top level actor you can call the spawn method on the ActorSystem
const shouter = system.spawn(new Shouter())

//Now you can send messages to your Actor using the tell method
shouter.tell("hey")
