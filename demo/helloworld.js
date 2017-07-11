const akka = require(`../lib/akkajs`)
const readline = require(`readline`)

const system = akka.ActorSystem.create(`helloworld`)

class Greeter extends akka.Actor {
  constructor() {
     super()
     // like what is suggested https://facebook.github.io/react/docs/react-without-es6.html#autobinding
     this.receive = Greeter.prototype.receive.bind(this)
   }
   receive(msg) {
    console.log(`Hello ${msg}`)
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
