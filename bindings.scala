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
class Configuration(str: String) {

  var conf = ConfigFactory.default()
  def add(str: String): Unit = {
    val tmp = conf
    conf = ConfigFactory.parse(str).withFallback(tmp)
  }
  def get() = conf

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
class Actor(
  behavior: js.ThisFunction1[Actor, Any, Unit],
  preStart: js.ThisFunction0[Actor, Unit] = (_ : Actor) => Unit,
  postStop: js.ThisFunction0[Actor, Unit] = (_ : Actor) => Unit,
  var name: String = ""
) extends CanSpawn {
  jsActor =>

  var innerSelf: akkaactor.Actor = _
  def spawnFrom = innerSelf.context

  def path(): String = innerSelf.self.path.toString
  def parent(): ActorRef = newAR(innerSelf.context.parent)
  def children(): js.Array[ActorRef] =
    js.Array(innerSelf.context.children.map(newAR).toSeq: _*)

  lazy val actor: akkaactor.Actor = new akkaactor.Actor {
    self =>
    jsActor.innerSelf = self
    name = self.self.path.name

    override def preStart() = jsActor.preStart(jsActor)
    override def postStop() = jsActor.postStop(jsActor)

    def receive = {case any => jsActor.behavior(jsActor, any)}
  }

  def become(behavior: js.ThisFunction1[Actor, Any, Unit]) =
    innerSelf.context.become{case any => behavior(jsActor, any)}

  def sender() = newAR(innerSelf.context.sender())

  private def newAR(ar: akkaactor.ActorRef) = new ActorRef {
    val actorRef = ar
    override def tell(msg: Any) = actorRef.tell(msg, innerSelf.context.self)
  }

}
