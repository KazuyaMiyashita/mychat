package app

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, Terminated}

import chat.{Rooms, RoomsController, RoomController}

object Initializer {

  def apply(interface: String, port: Int): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      val counter           = context.spawn(Counter(), "counter")
      val rooms             = context.spawn(Rooms(), "rooms")
      val requestHelper     = new RequestHelper
      val counterController = new CounterController(counter, requestHelper)
      val roomsController   = new RoomsController(rooms, requestHelper)
      val roomController    = new RoomController(rooms, requestHelper)
      val router: Router    = new RouterImpl(counterController, roomsController, roomController)
      context.spawn(WebServer(router, interface, port), "webserver")

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }

}
