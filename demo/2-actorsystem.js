const akka = require(`akkajs`)

//An ActorSystem is a container and the environment for your Actors
//To create an ActorSystem you have several options:

var system

//All defaults
system = akka.ActorSystem.create()

//Custom name
system = akka.ActorSystem.create("myActorSystemName")

//Custom name and custom configuration
system = akka.ActorSystem.create(
  "myActorSystemName",
  new akka.Configuration(`akka {
    loglevel = "DEBUG"
    stdout-loglevel = "DEBUG"
  }`).get()
)
