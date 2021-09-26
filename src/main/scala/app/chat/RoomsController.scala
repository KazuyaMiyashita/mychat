package app.chat

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout

import scala.concurrent.duration._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.parser

import app.RequestHelper

class RoomsController(
    rooms: ActorRef[Rooms.Command],
    requestHelper: RequestHelper
) {

  import RoomsController._

  def create(): Flow[HttpRequest, HttpResponse, NotUsed] = requestHelper.withCodec[CreateRequest, CreateResponse] {
    implicit val timeout: Timeout = 1.seconds

    val flow: Flow[CreateRequest, Rooms.RoomName, NotUsed] = ActorFlow.ask(parallelism = 8)(ref = rooms)(
      makeMessage = (message, ref) => Rooms.Create(Rooms.RoomName(message.name), ref)
    )

    flow.map { roomName => CreateResponse(roomName.value) }
  }

  def list(): Flow[HttpRequest, HttpResponse, NotUsed] = requestHelper.withCodec[Unit, ListResponse] {
    implicit val timeout: Timeout = 1.seconds

    val flow: Flow[Unit, List[Rooms.RoomName], NotUsed] = ActorFlow.ask(parallelism = 8)(ref = rooms)(
      makeMessage = (message, ref) => Rooms.GetList(ref)
    )

    flow.map { roomNames => ListResponse(roomNames.map(_.value)) }
  }

}

object RoomsController {

  case class CreateRequest(name: String)
  case class CreateResponse(name: String)

  private implicit val createRequestDecoder: RequestHelper.Decoder[CreateRequest] = {
    implicit val jsonDecoder: Decoder[CreateRequest] = new Decoder[CreateRequest] {
      override def apply(c: HCursor): Result[CreateRequest] = {
        c.get[String]("name").map(CreateRequest.apply)
      }
    }

    new RequestHelper.Decoder[CreateRequest] {
      override def decode(in: String): Option[CreateRequest] = parser.decode[CreateRequest](in).toOption
    }
  }

  private implicit val createResponseEncoder: RequestHelper.Encoder[CreateResponse] = {
    val jsonEncoder: Encoder[CreateResponse] = Encoder.instance { a =>
      Json.obj(
        "name" -> Json.fromString(a.name)
      )
    }
    new RequestHelper.Encoder[CreateResponse] {
      override def encode(out: CreateResponse): String = jsonEncoder(out).noSpaces
    }
  }

  private implicit val NoDecoder: RequestHelper.Decoder[Unit] = new RequestHelper.Decoder[Unit] {
    override def decode(in: String): Option[Unit] = Some(())
  }

  case class ListResponse(rooms: List[String])

  private implicit val listResponseEncoder: RequestHelper.Encoder[ListResponse] = {
    val jsonEncoder: Encoder[ListResponse] = Encoder.instance { a =>
      Json.obj(
        "rooms" -> Json.arr(a.rooms.map(Json.fromString): _*)
      )
    }
    new RequestHelper.Encoder[ListResponse] {
      override def encode(out: ListResponse): String = jsonEncoder(out).noSpaces
    }
  }

}
