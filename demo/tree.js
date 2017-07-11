const akka = require(`../lib/akkajs`)

const config = new akka.Configuration(
    `akka {
      loglevel = "DEBUG"
      stdout-loglevel = "DEBUG"
    }`).get()

const system = akka.ActorSystem.create("pingpong", config)

let count = 0

class NodeActor extends akka.Actor {
  constructor(name) {
     super()
     this.name = name
     this.receive = NodeActor.receive.bind(this)
     this.preStart = NodeActor.preStart.bind(this)
     this.postStop = NodeActor.postStop.bind(this)
   }
   static receive(msg) {
     if (msg === "spawn" && count < 10000) {
       var right = this.spawn(new NodeActor("R"))
       var left = this.spawn(new NodeActor("L"))

       count += 2
       right.tell("spawn")
       left.tell("spawn")
     }
   }

   static preStart() {
     console.log("starting "+this.path())
   }

   static postStop() {
     console.log("stopping "+this.path())
   }
}

var root = system.spawn(new NodeActor("root"))

root.tell("spawn")

setTimeout(function() {
  system.terminate()
}, 3000)
