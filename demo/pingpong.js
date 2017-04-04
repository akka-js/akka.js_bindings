var akka = require("../bin/akkajs.js")

var config = new akka.Configuration(
    `akka {
      loglevel = "DEBUG"
      stdout-loglevel = "DEBUG"
    }`).get()

var system = akka.ActorSystem.create("pingpong", config)

var pinger = system.spawn(new akka.Actor(
  function(msg) {
    console.log("I'm pinger and I received "+msg)
    this.sender().tell("PING")
  }
))

var i = 0
var ponger = system.spawn(new akka.Actor(function(msg) {
  console.log("I'm ponger and I received "+msg)
  this.sender().tell("PONG")
  i++
  if (i > 100)
    this.become( function(msg) {
      this.sender().tell("PONG, I say!")
    } )
}))

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
