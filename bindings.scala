package org.akkajs.bindings

import akka.{actor => akkaactor}
import eu.unicredit.shocon
import com.typesafe.config.Config
import scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}

import akka.actor._

@JSExportTopLevel("Configuration")
@JSExportAll
object JSConfiguration {

  var base = default()

  def default(): Config = akkajs.Config.default
  def parse(str: String): Config = Config(shocon.Config(str))
  def add(str: String): Unit = {
    val tmp = base
    base = parse(str).withFallback(tmp)
  }
  def get() = base
}

@JSExportTopLevel("ActorSystem")
@JSExportAll
object ActorSystem {

  def create(name: String, config: Config): ActorSystemImpl =
    new ActorSystemImpl{
      val system = akkaactor.ActorSystem(name, config)
    }
  def create(name: String): ActorSystemImpl =
    create(name, akkajs.Config.default)
  def create(): ActorSystemImpl =
    create("default", akkajs.Config.default)

}

@JSExportAll
trait ActorSystemImpl {
  val system: akkaactor.ActorSystem

  def log() = system.log.info("ciao")
  def logError() = system.log.error("ciao")

  def logConfig() = system.logConfiguration()

  def spawn(actor: Actor) = new ActorRef {
    val actorRef =
      if (actor.name != "") system.actorOf(Props(actor.actor), actor.name)
      else system.actorOf(Props(actor.actor))
  }

  def terminate() = system.terminate()
}

@JSExportAll
trait ActorRef {
  val actorRef: akkaactor.ActorRef

  def tell(msg: Any) = actorRef ! msg
  def tell(msg: Any, sender: ActorRef) = actorRef.tell(msg, sender.actorRef)

  def kill() = actorRef ! akkaactor.PoisonPill
}

@JSExportTopLevel("Actor")
@JSExportAll
class Actor(
  behavior: js.ThisFunction1[Actor, Any, Unit],
  val name: String = "",
  preStart: () => Unit = () => (),
  postStart: () => Unit = () => ()
) {
  jsActor =>

  var innerSelf: akkaactor.Actor = _

  lazy val actor: akkaactor.Actor = new akkaactor.Actor {
    self =>

    jsActor.innerSelf = self

    override def preStart() = jsActor.preStart()
    override def postStop() = jsActor.postStart()
    def receive = {
      case any =>
        val s = sender()
        jsActor.behavior(jsActor, any)
    }
  }

  def become(behavior: js.ThisFunction1[Actor, Any, Unit]) =
    innerSelf.context.become{case any => behavior(jsActor, any)}

  def sender() = new ActorRef{
    val actorRef = innerSelf.context.sender()
    override def tell(msg: Any) = actorRef.tell(msg, innerSelf.context.self)
  }

  def spawn(actor: Actor) = new ActorRef {
    val actorRef =
      if (actor.name != "") innerSelf.context.actorOf(Props(actor.actor), actor.name)
      else innerSelf.context.actorOf(Props(actor.actor))
  }

}
