import java.nio.charset.StandardCharsets

import akka.stream.scaladsl.{Flow, Source}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, HttpEntity, ContentTypes}
import akka.util.ByteString
import akka.NotUsed

import RequestHelper._

class RequestHelper {

  def withCodec[A, B](
      flow: Flow[A, B, NotUsed]
  )(implicit decoder: Decoder[A], encoder: Encoder[B]): Flow[HttpRequest, HttpResponse, NotUsed] = {
    Flow[HttpRequest].flatMapConcat { req =>
      val sourceOptA: Source[Option[A], Any] =
        req.entity.dataBytes.map(_.decodeString(StandardCharsets.UTF_8)).map(decoder.decode)
      sourceOptA.flatMapConcat {
        case None => Source.single(HttpResponse(400, entity = HttpEntity("invalid request")))
        case Some(a) =>
          Source.single(a).via(flow).map { b =>
            val byteString = ByteString.apply(encoder.encode(b), StandardCharsets.UTF_8)
            HttpResponse(200, entity = HttpEntity.apply(ContentTypes.`application/json`, byteString))
          }
      }
    }
  }

}

object RequestHelper {

  trait Decoder[A] {
    def decode(in: String): Option[A]
  }

  trait Encoder[B] {
    def encode(out: B): String
  }

}
