package app.chat

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Rooms {

  sealed trait Command
  case class Create(name: RoomName, replyTo: ActorRef[RoomName])    extends Command
  case class GetList(replyTo: ActorRef[List[RoomName]])             extends Command
  case class RoomCommand(roomName: RoomName, command: Room.Command) extends Command

  case class RoomName(value: String)

  def apply(): Behavior[Command] = rooms(Map.empty)

  def rooms(roomMap: Map[RoomName, ActorRef[Room.Command]]): Behavior[Command] = Behaviors.receive {
    (context, command) =>
      command match {
        case Create(name, replyTo) =>
          val roomRef = context.spawnAnonymous(Room())
          replyTo ! name
          rooms(roomMap + (name -> roomRef))
        case GetList(replyTo) =>
          replyTo ! roomMap.keys.toList
          Behaviors.same
        case RoomCommand(roomName, command) =>
          roomMap.get(roomName).foreach(_ ! command)
          Behaviors.same
      }

  }

}
