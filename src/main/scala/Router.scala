import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import akka.http.scaladsl.model.{HttpEntity, HttpMethod, HttpRequest, HttpResponse, Uri}
import akka.NotUsed

trait Router {

  protected val route: PartialFunction[(HttpMethod, Uri.Path), Flow[HttpRequest, HttpResponse, NotUsed]]

  final def handleRequest(implicit mat: Materializer): Flow[HttpRequest, HttpResponse, NotUsed] = {
    Flow[HttpRequest].flatMapConcat { (httpRequest: HttpRequest) =>
      val method = httpRequest.method
      val uri    = httpRequest.uri.path

      if (route.isDefinedAt((method, uri))) {
        val flow: Flow[HttpRequest, HttpResponse, NotUsed] = route(method, uri)
        Source.single(httpRequest).via(flow)
      } else {
        httpRequest.discardEntityBytes()
        Source.single(HttpResponse(404, entity = HttpEntity("Unknown resource!")))
      }

    }
  }

}
