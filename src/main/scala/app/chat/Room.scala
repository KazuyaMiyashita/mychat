package app.chat

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Room {

  sealed trait Command
  case class AddMessage(message: Message)              extends Command
  case class GetList(replyTo: ActorRef[List[Message]]) extends Command

  case class Message(value: String)

  def apply(): Behavior[Command] = room(Nil)

  def room(messages: List[Message]): Behavior[Command] = Behaviors.receiveMessage {
    case AddMessage(message) =>
      room(message :: messages)
    case GetList(replyTo) =>
      replyTo ! messages
      Behaviors.same
  }

}
