var akka = require("../bin/akkajs.js")

var config = new akka.Configuration(
    `akka {
      loglevel = "DEBUG"
      stdout-loglevel = "DEBUG"
    }`).get()

var system = akka.ActorSystem.create("pingpong", config)

var count = 0
var newSon = function(name) {
  return new akka.Actor(
    function(msg) {
      if (msg === "spawn" && count < 10000) {
        var right = this.spawn(newSon("R"))
        var left = this.spawn(newSon("L"))

        count += 2
        right.tell("spawn")
        left.tell("spawn")
      }
    },
    function() {console.log("starting "+this.path())},
    function() {console.log("stopping "+this.path())},
    name
  )
}

var root = system.spawn(newSon("root"))

root.tell("spawn")

setTimeout(function() {
  system.terminate()
}, 3000)
