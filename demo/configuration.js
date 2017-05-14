const akka = require(`../lib/akkajs`)

//Configuration is based on the HOCON standard
//https://github.com/typesafehub/config/blob/master/HOCON.md
//ported to JS through ScalaJs with:
//https://github.com/unicredit/shocon

var config

//create a configuration (using akka default one as a base):
config = new akka.Configuration()

//create a configuration with additional keys to the Akka default one:
config = new akka.Configuration(`akka {
  loglevel = "DEBUG"
  stdout-loglevel = "DEBUG"
}`)

//add more customizations
config.add(`akka.log-config-on-start = on`)

//get the configuration to be used
config.get()

//Now you can provide your configuration during creation of ActorSystem
//Full configuration options are listed here:
//http://doc.akka.io/docs/akka/2.5/general/configuration.html#Listing_of_the_Reference_Configuration
const system = akka.ActorSystem.create("actorSystemName", config.get())

//in case terminate your system
system.terminate()
