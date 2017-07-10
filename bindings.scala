package org.akkajs.bindings

import akka.{actor => akkaactor}
import eu.unicredit.shocon
import com.typesafe.config.Config
import scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExportAll}

import akka.actor._

@JSExportTopLevel("ConfigFactory")
@JSExportAll
object ConfigFactory {

  def default(): Config = akkajs.Config.default
  def parse(str: String): Config = Config(shocon.Config(str))

}

@JSExportTopLevel("Configuration")
@JSExportAll
class Configuration(str: String = "") {

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
    new ActorSystemImpl{
      val system = akkaactor.ActorSystem(name, config)
    }
  def create(name: String): ActorSystemImpl =
    create(name, akkajs.Config.default)
  def create(): ActorSystemImpl =
    create("default", akkajs.Config.default)

}

@JSExportAll
trait CanSpawn {
  type Spawner = {
    def actorOf(props: akkaactor.Props, name: String): akkaactor.ActorRef
    def actorOf(props: akkaactor.Props): akkaactor.ActorRef
  }

  def spawnFrom: Spawner

  def spawn(actor: Actor) = new ActorRef {
    val actorRef =
      if (actor.name != "") spawnFrom.actorOf(Props(actor.actor), actor.name)
      else spawnFrom.actorOf(Props(actor.actor))
  }
}

@JSExportAll
trait ActorRef {
  val actorRef: akkaactor.ActorRef

  def path(): String = actorRef.path.toString

  def tell(msg: Any) = actorRef.tell(msg, null)
  def tell(msg: Any, sender: ActorRef) = actorRef.tell(msg, sender.actorRef)

  def kill() = tell(akkaactor.PoisonPill)
}

@JSExportAll
trait ActorSelection {
  val actorSel: akkaactor.ActorSelection

  def tell(msg: Any) = actorSel.tell(msg, null)
  def tell(msg: Any, sender: akkaactor.ActorRef) = actorSel.tell(msg, sender)

  def kill() = tell(akkaactor.PoisonPill)
}

@JSExportAll
trait ActorSystemImpl extends CanSpawn {
  val system: akkaactor.ActorSystem
  def spawnFrom = system

  def select(path: String) = new ActorSelection {
    val actorSel = system.actorSelection(path)
  }

  def terminate() = system.terminate()
}

@JSExportTopLevel("Actor")
@JSExportAll
class Actor() extends CanSpawn {
  jsActor =>

  // Functions intended to be overloaded
  var name: String = ""

  var receive: js.Function1[Any, Unit] = null

  var preStart: js.Function0[Unit] = () => Unit

  var postStop: js.Function0[Unit] = () => Unit

  // API
  def path(): String = ar.path.toString
  def parent(): ActorRef = newAR(innerContext.parent)
  def children(): js.Array[ActorRef] =
    js.Array(innerContext.children.map(newAR).toSeq: _*)

  def become(behavior: js.Function1[Any, Unit]) =
    innerContext.become{case any => behavior(any)}

  def sender() = newAR(innerContext.sender())

  //Inner implementation
  var ar: akkaactor.ActorRef = _
  var innerContext: akkaactor.ActorContext = _

  lazy val actor: akkaactor.Actor = new akkaactor.Actor {
    if (js.isUndefined(jsActor.receive))
      throw new Exception("Actor receive method not defiend")

    name = jsActor.name

    override def preStart() = {
      jsActor.ar = self
      jsActor.innerContext = context
      jsActor.preStart()
    }
    override def postStop() = jsActor.postStop()

    def receive = {case any => jsActor.receive(any)}
  }

  def spawnFrom = innerContext

  private def newAR(_ar: akkaactor.ActorRef) = new ActorRef {
    val actorRef = _ar
    override def tell(msg: Any) = actorRef.tell(msg, ar)
  }

}
