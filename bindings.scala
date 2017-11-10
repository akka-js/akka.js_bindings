package org.akkajs.bindings

import akka.{actor => akkaactor}
import eu.unicredit.shocon
import com.typesafe.config.Config
import scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, ScalaJSDefined}

import akka.actor._

@JSExportTopLevel("ConfigFactory")
object ConfigFactory extends js.Object {

  def default(): Config = akkajs.Config.default
  def parse(str: String): Config = Config(shocon.Config(str))

}

@JSExportTopLevel("Configuration")
class Configuration(str: String = "") extends js.Object {

  var conf = ConfigFactory.default()
  def add(str: String): Unit = {
    val tmp = conf
    conf = ConfigFactory.parse(str).withFallback(tmp)
  }
  def get() = conf

  if (str.trim() != "")
    add(str)
}

@JSExportTopLevel("ActorSystem")
object ActorSystem extends js.Object {

  def create(name: String, config: Config): ActorSystemImpl =
    new ActorSystemImpl(
      akkaactor.ActorSystem(name, config)
    )
  def create(name: String): ActorSystemImpl =
    create(name, akkajs.Config.default)
  def create(): ActorSystemImpl =
    create("default", akkajs.Config.default)

}

class ActorRef(val actorRef: akkaactor.ActorRef, val sourceRef: akkaactor.ActorRef = null) extends js.Object {

  def path(): String = actorRef.path.toString

  def tell(msg: Any) = actorRef.tell(msg, sourceRef)
  def tell(msg: Any, sender: ActorRef) = actorRef.tell(msg, sender.actorRef)

  def kill() = tell(akkaactor.PoisonPill)
}

class ActorSelection(val actorSel: akkaactor.ActorSelection) extends js.Object {

  def path(): String = actorSel.anchorPath.toString

  def tell(msg: Any) = actorSel.tell(msg, null)
  def tell(msg: Any, sender: akkaactor.ActorRef) = actorSel.tell(msg, sender)

  def kill() = tell(akkaactor.PoisonPill)
}

class ActorSystemImpl(val system: akkaactor.ActorSystem) extends js.Object {

  def select(path: String) = new ActorSelection(
    system.actorSelection(path)
  )

  def spawn(actor: Actor) = new ActorRef({
      if (actor.name != null) system.actorOf(Props(actor.actor), actor.name)
      else system.actorOf(Props(actor.actor))
  })

  def terminate() = system.terminate()
}

@JSExportTopLevel("Actor")
class Actor() extends js.Object {
  jsActor =>

  // Functions intended to be overloaded
  var name: String = null

  def receive(any: Any): Unit = ()

  def preStart(): Unit = ()

  def postStop(): Unit = ()

  // API
  def path(): String = ar.path.toString
  def parent(): ActorRef = newAR(innerContext.parent)
  def children(): js.Array[ActorRef] =
    js.Array(innerContext.children.map(newAR).toSeq: _*)

  def become(behavior: js.Function1[Any, Unit]) =
    innerContext.become{case any => behavior(any)}

  def sender() = newAR(innerContext.sender())

  def self() = newAR(ar)

  def system() = new ActorSystemImpl(innerContext.system)

  def spawn(actor: Actor) = new ActorRef({
      if (actor.name != null) innerContext.actorOf(Props(actor.actor), actor.name)
      else innerContext.actorOf(Props(actor.actor))
  })

  //Inner implementation
  var ar: akkaactor.ActorRef = _
  var innerContext: akkaactor.ActorContext = _

  lazy val actor: akkaactor.Actor = new akkaactor.Actor {
    name = jsActor.name

    override def preStart() = {
      jsActor.ar = self
      jsActor.innerContext = context
      jsActor.preStart()
    }
    override def postStop() = jsActor.postStop()

    def receive = { case any => jsActor.receive(any) }
  }

  private def newAR(dest: akkaactor.ActorRef) = new ActorRef(dest, ar)

}
