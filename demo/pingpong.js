const akka = require(`../lib/akkajs`)

let config = new akka.Configuration(
    `akka {
      loglevel = "DEBUG"
      stdout-loglevel = "DEBUG"
    }`).get()

const system = akka.ActorSystem.create("pingpong", config)

class Pinger extends akka.Actor {
  constructor() {
    super()
    this.name = "pinger"
    this.receive = this.receive.bind(this)
  }

  receive(msg) {
    console.log("I'm pinger and I received "+msg)
    this.sender().tell("PING")
  }
}

let pinger = system.spawn(new Pinger())

class Ponger extends akka.Actor {
  constructor() {
    super()
    this.name = "ponger"
    this.i = 0
    this.receive = this.receive.bind(this)
  }

  receive(msg) {
    console.log("I'm ponger and I received "+msg+" i is "+this.i)
    this.sender().tell("PONG")
    this.i++
    if (this.i > 100) {
      this.become( (msg) => {
        this.sender().tell("PONG, I say!")
        this.self().kill()
      } )
    }
  }
}

let ponger = system.spawn(new Ponger())

setTimeout(function() {
  console.log("Starting...")

  ponger.tell("START", pinger)
}, 1000)

setTimeout(function() {
  console.log("Exit now...")

  pinger.kill()
  ponger.kill()
  system.terminate()
}, 3000)
