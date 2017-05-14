
# Akka.Js Bindings

*this project is EXPERIMENTAL*

This project aim to bring Akka powerful framework available for JS applications.

This implementation is to be considered "on-going" but feel free to file an issue if you require a specific Akka functionality that isn't yet exposed in the current API.

Feedbacks are highly appreciated!

## Use It

Install the module:

```
npm install akkajs
```

An example Actor based greeter looks like follows:

```javascript
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
```

You can use Akka.Js from Node or Browser.

## More examples

Into the github repository there are several examples under the `demo` folder.

### Compile and run examples

You need to install Sbt to compile from sources.
To run the examples simply clone this project and:
```bash
sbt deploy
cd demo
node <example-name>
```
