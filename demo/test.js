const akka = require(`../lib/akkajs`)
const readline = require(`readline`)

const system = akka.ActorSystem.create(`helloworld`)

class Greeter extends akka.Actor {
  constructor() {
    super()
    this.name = "pluto"
    this.preStart = () => {
      console.log("prestarting...")
    }
    this.receive = (msg) => {
      console.log(`Hello ${msg}`)
    }
  }
}

const greeter = system.spawn(new Greeter())

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
})

rl.question('What is your name? ', (answer) => {
  greeter.tell(answer)
  rl.close();
})
