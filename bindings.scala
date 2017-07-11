package org.akkajs.bindings

import akka.{actor => akkaactor}
import eu.unicredit.shocon
import com.typesafe.config.Config
import scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll, ScalaJSDefined}

import akka.actor._

@JSExportTopLevel("ConfigFactory")
@JSExportAll
object ConfigFactory {

  def default(): Config = akkajs.Config.default
  def parse(str: String): Config = Config(shocon.Config(str))

}

@JSExportTopLevel("Configuration")
@ScalaJSDefined
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
@JSExportAll
object ActorSystem {

  def create(name: String, config: Config): ActorSystemImpl =
    new ActorSystemImpl(
      akkaactor.ActorSystem(name, config)
    )
  def create(name: String): ActorSystemImpl =
    create(name, akkajs.Config.default)
  def create(): ActorSystemImpl =
    create("default", akkajs.Config.default)

}

@ScalaJSDefined
class ActorRef(val actorRef: akkaactor.ActorRef) extends js.Object {

  def path(): String = actorRef.path.toString

  def tell(msg: Any) = actorRef.tell(msg, null)
  def tell(msg: Any, sender: ActorRef) = actorRef.tell(msg, sender.actorRef)

  def kill() = tell(akkaactor.PoisonPill)
}

@ScalaJSDefined
class ActorSelection(actorSel: akkaactor.ActorSelection) extends js.Object {

  def tell(msg: Any) = actorSel.tell(msg, null)
  def tell(msg: Any, sender: akkaactor.ActorRef) = actorSel.tell(msg, sender)

  def kill() = tell(akkaactor.PoisonPill)
}

@ScalaJSDefined
class ActorSystemImpl(system: akkaactor.ActorSystem) extends js.Object {

  def select(path: String) = new ActorSelection(
    system.actorSelection(path)
  )

  def spawn(actor: Actor) = new ActorRef(
    if (actor.name != null) system.actorOf(Props(actor.actor), actor.name)
    else system.actorOf(Props(actor.actor))
  )

  def terminate() = system.terminate()
}

@JSExportTopLevel("Actor")
@ScalaJSDefined
class Actor extends js.Object {
  jsActor =>

  // Functions intended to be overloaded
  var name: String = null

  def receive(x: Any): Unit = ()

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

  //Inner implementation
  private var ar: akkaactor.ActorRef = _
  private var innerContext: akkaactor.ActorContext = _

  def spawn(actor: Actor) = new ActorRef(
    if (actor.name != null) innerContext.actorOf(Props(actor.actor), actor.name)
    else innerContext.actorOf(Props(actor.actor))
  )

  lazy val actor: akkaactor.Actor = new akkaactor.Actor {
    name = jsActor.name

    override def preStart() = {
      jsActor.ar = self
      jsActor.innerContext = context
      jsActor.preStart()
    }
    override def postStop() = jsActor.postStop()

    def receive = {case any => jsActor.receive(any)}
  }

  private def newAR(_ar: akkaactor.ActorRef) = new ActorRef(_ar) {
    override def tell(msg: Any) = actorRef.tell(msg, ar)
  }

}
