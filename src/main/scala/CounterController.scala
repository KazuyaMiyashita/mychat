import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout

import scala.concurrent.duration._
import io.circe.{Encoder, Json}

class CounterController(
    counter: ActorRef[Counter.Command],
    requestHelper: RequestHelper
) {

  import CounterController._

  def countUp(): Flow[HttpRequest, HttpResponse, NotUsed] = requestHelper.withCodec[Unit, CountResponse] {
    implicit val timeout: Timeout = 1.seconds

    val flow: Flow[Unit, Counter.Count, NotUsed] = ActorFlow.ask(parallelism = 8)(ref = counter)(
      makeMessage = (message, ref) => Counter.CountUp(ref)
    )

    flow.map { count => CountResponse(count.value) }
  }
}

object CounterController {

  case class CountResponse(value: Int)

  private implicit val NoDecoder: RequestHelper.Decoder[Unit] = new RequestHelper.Decoder[Unit] {
    override def decode(in: String): Option[Unit] = Some(())
  }

  private implicit val CountResponseEncoder: RequestHelper.Encoder[CountResponse] = {
    val jsonEncoder: Encoder[CountResponse] = Encoder.instance { a =>
      Json.obj(
        "count" -> Json.fromInt(a.value)
      )
    }
    new RequestHelper.Encoder[CountResponse] {
      override def encode(out: CountResponse): String = jsonEncoder(out).noSpaces
    }
  }

}
