package app

import akka.NotUsed
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Flow
import chat.{RoomController, RoomsController}

class RouterImpl(
    counterController: CounterController,
    roomsController: RoomsController,
    roomController: RoomController
) extends Router {

  val RoomsWithRoomName = "/rooms/(.*)".r

  override protected val route: PartialFunction[(HttpMethod, Uri.Path), Flow[HttpRequest, HttpResponse, NotUsed]] = {
    case GET -> Uri.Path("/") =>
      Flow.fromFunction((_: HttpRequest) => HttpResponse(200, entity = HttpEntity("API works!")))
    case GET -> Uri.Path("/count") =>
      counterController.countUp()
    case GET -> Uri.Path("/rooms") =>
      roomsController.list()
    case POST -> Uri.Path("/rooms") =>
      roomsController.create()
    case GET -> Uri.Path(RoomsWithRoomName(roomName)) =>
      roomController.list(roomName)
    case POST -> Uri.Path(RoomsWithRoomName(roomName)) =>
      roomController.addMessage(roomName)
    case (method, path) =>
      Flow.fromFunction((_: HttpRequest) => HttpResponse(404, entity = HttpEntity(s"method: $method, path: $path")))
  }

}
