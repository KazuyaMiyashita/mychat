package app.chat

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import app.RequestHelper
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json, parser}

import scala.concurrent.duration._

class RoomController(
    rooms: ActorRef[Rooms.Command],
    requestHelper: RequestHelper
) {

  import RoomController._

  def addMessage(roomName: String): Flow[HttpRequest, HttpResponse, NotUsed] =
    requestHelper.withCodec[AddMessageRequest, Unit] {

      Flow.fromFunction { addMessageRequest =>
        rooms ! Rooms.RoomCommand(Rooms.RoomName(roomName), Room.AddMessage(Room.Message(addMessageRequest.message)))
        HttpResponse(202)
      }
    }

  def list(roomName: String): Flow[HttpRequest, HttpResponse, NotUsed] =
    requestHelper.withCodec[Unit, MessagesResponse] {
      implicit val timeout: Timeout = 1.seconds

      val flow: Flow[Unit, List[Room.Message], NotUsed] = ActorFlow.ask(parallelism = 8)(ref = rooms)(
        makeMessage = (message, ref) => Rooms.RoomCommand(Rooms.RoomName(roomName), Room.GetList(ref))
      )

      flow.map { roomNames => MessagesResponse(roomNames.map(_.value)) }
    }

}

object RoomController {

  case class AddMessageRequest(message: String)

  private implicit val addMessageRequestDecoder: RequestHelper.Decoder[AddMessageRequest] = {
    implicit val jsonDecoder: Decoder[AddMessageRequest] = new Decoder[AddMessageRequest] {
      override def apply(c: HCursor): Result[AddMessageRequest] = {
        c.get[String]("message").map(AddMessageRequest.apply)
      }
    }

    new RequestHelper.Decoder[AddMessageRequest] {
      override def decode(in: String): Option[AddMessageRequest] = parser.decode[AddMessageRequest](in).toOption
    }
  }

  private implicit val NoEncoder: RequestHelper.Encoder[Unit] = new RequestHelper.Encoder[Unit] {
    override def encode(out: Unit): String = ""
  }

  private implicit val NoDecoder: RequestHelper.Decoder[Unit] = new RequestHelper.Decoder[Unit] {
    override def decode(in: String): Option[Unit] = Some(())
  }

  case class MessagesResponse(messages: List[String])

  private implicit val listResponseEncoder: RequestHelper.Encoder[MessagesResponse] = {
    val jsonEncoder: Encoder[MessagesResponse] = Encoder.instance { a =>
      Json.obj(
        "messages" -> Json.arr(a.messages.map(Json.fromString): _*)
      )
    }
    new RequestHelper.Encoder[MessagesResponse] {
      override def encode(out: MessagesResponse): String = jsonEncoder(out).noSpaces
    }
  }

}
