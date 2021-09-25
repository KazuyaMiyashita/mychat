import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Counter {

  sealed trait Command
  case class CountUp(replyTo: ActorRef[Count]) extends Command

  case class Count(value: Int) {
    def +(v: Int): Count = Count(value + v)
  }

  def apply(): Behavior[Command] = counter(Count(0))

  def counter(cnt: Count): Behavior[Command] = Behaviors.receiveMessage {
    case CountUp(replyTo) =>
      replyTo ! cnt
      counter(cnt + 1)
  }

}
