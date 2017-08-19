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
     this.receive = this.receive.bind(this)
     this.preStart = this.preStart.bind(this)
     this.postStop = this.postStop.bind(this)
   }
   receive(msg) {
     if (msg === "spawn" && count < 10000) {
       var right = this.spawn(new NodeActor("R"))
       var left = this.spawn(new NodeActor("L"))

       count += 2
       right.tell("spawn")
       left.tell("spawn")
     }
   }
   preStart() {
     console.log("starting "+this.path())
   }
   postStop() {
     console.log("stopping "+this.path())
   }
}

var root = system.spawn(new NodeActor("root"))

root.tell("spawn")

setTimeout(function() {
  system.terminate()
}, 3000)
