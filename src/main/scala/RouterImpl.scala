import akka.NotUsed
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpEntity, HttpMethod, HttpRequest, HttpResponse, Uri}
import akka.stream.scaladsl.Flow

class RouterImpl(
    counterController: CounterController
) extends Router {

  override protected val route: PartialFunction[(HttpMethod, Uri.Path), Flow[HttpRequest, HttpResponse, NotUsed]] = {
    case GET -> Uri.Path("/") =>
      Flow.fromFunction((_: HttpRequest) => HttpResponse(200, entity = HttpEntity("API works!")))
    case GET -> Uri.Path("/count") =>
      counterController.countUp()
  }

}
