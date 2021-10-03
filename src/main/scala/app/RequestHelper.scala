package app

import java.nio.charset.StandardCharsets

import akka.NotUsed
import akka.http.scaladsl.model.headers.{HttpOrigin, RawHeader, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import app.RequestHelper.{Decoder, Encoder}

class RequestHelper(
    allowOrigins: List[HttpOrigin]
) {

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
            HttpResponse(
              200,
              headers = accessControlAllowOrigin(req),
              entity = HttpEntity.apply(ContentTypes.`application/json`, byteString)
            )
          }
      }
    }
  }

  private def accessControlAllowOrigin(req: HttpRequest): List[HttpHeader] = {
    req.headers.find(_.lowercaseName == "origin") match {
      case Some(origin) if allowOrigins.contains(HttpOrigin(origin.value)) =>
        `Access-Control-Allow-Origin`(HttpOrigin(origin.value)) ::
          RawHeader("Vary", "Origin") ::
          Nil
      case _ => RawHeader("Vary", "Origin") :: Nil
    }
  }

}

object RequestHelper {

  def apply(_allowOrigins: List[String]): RequestHelper = {
    val allowOrigins: List[HttpOrigin] = _allowOrigins.map(HttpOrigin.apply)
    new RequestHelper(allowOrigins)
  }

  trait Decoder[A] {
    def decode(in: String): Option[A]
  }

  trait Encoder[B] {
    def encode(out: B): String
  }

}
