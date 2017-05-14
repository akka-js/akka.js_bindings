const akka = require(`akkajs`)
const readline = require(`readline`)

var system = akka.ActorSystem.create(`helloworld`)

const greeter = system.spawn(
  new akka.Actor(
    function(name) {
      console.log(`Hello ${name}`)
    }
  )
)

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
})

rl.question('What is your name? ', (answer) => {
  greeter.tell(answer)
  rl.close();
})
