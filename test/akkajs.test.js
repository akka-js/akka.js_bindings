import test from 'ava'
import { ActorSystem, Actor } from '../lib/akkajs'

test.cb('an Actor should start and receive messages', t => {
  t.plan(1)

  const textMsg = "this is a message"

  const system = ActorSystem.create(`helloworld`)

  class Basic extends Actor {
    constructor() {
      super()
      this.receive = this.receive.bind(this)
    }
    receive(msg) {
      t.is(msg, textMsg)
      t.end()
    }
  }

  const basic = system.spawn(new Basic())

  setTimeout(() => basic.tell(textMsg), 0)
})

test.cb('an Actor should have expected properties', t => {
  t.plan(4)

  const system = ActorSystem.create(`helloworld`)

  class BasicFather extends Actor {
    constructor () {
      super()
      this.name = "father"
    }
    preStart () {
      this.spawn(new BasicTest())
    }
  }

  class BasicTest extends Actor {
    constructor () {
      super()
      this.name = "test"
    }
    preStart () {
      this.spawn(new BasicChild())

      t.is(this.path(), "akka://helloworld/user/father/test")
      t.is(this.self().path(), this.path())
      t.is(this.parent().path(), "akka://helloworld/user/father")
      t.is(this.children()[0].path(), "akka://helloworld/user/father/test/child")
      t.end()
    }
  }

  class BasicChild extends Actor {
    constructor () {
      super()
      this.name = "child"
    }
  }

  system.spawn(new BasicFather())
})

test.cb('an Actor should change behavior', t => {
  t.plan(2)

  const textMsg = "this is a message"

  const system = ActorSystem.create(`helloworld`)

  class Basic extends Actor {
    constructor () {
      super()
      this.receive = this.receive.bind(this)
      this.operative = this.operative.bind(this)
    }
    receive (msg) {
      t.is(msg, textMsg)
      this.become(this.operative)
    }
    operative (msg) {
      t.is(msg, textMsg)
      t.end()
    }
  }

  const basic = system.spawn(new Basic())

  setTimeout(() => {
    basic.tell(textMsg)
    basic.tell(textMsg)
  }, 0)
})

test.cb('an Actor should answer the sender', t => {
  t.plan(2)

  const textMsg = "this is a message"

  const system = ActorSystem.create(`helloworld`)

  class Ping extends Actor {
    constructor () {
      super()
      this.name = "ping"
      this.receive = this.receive.bind(this)
    }
    receive (msg) {
      t.is(msg, textMsg)
      t.end()
    }
  }

  class Pong extends Actor {
    constructor () {
      super()
      this.name = "pong"
      this.receive = this.receive.bind(this)
    }
    receive (msg) {
      t.is(msg, textMsg)
      this.sender().tell(msg)
    }
  }

  const ping = system.spawn(new Ping())
  const pong = system.spawn(new Pong())

  setTimeout(() => pong.tell(textMsg, ping), 0)
})
