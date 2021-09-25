import akka.actor.typed.{Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import akka.NotUsed

object Initializer {

  def apply(): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      val counter           = context.spawn(Counter(), "counter")
      val requestHelper     = new RequestHelper
      val counterController = new CounterController(counter, requestHelper)
      val router: Router    = new RouterImpl(counterController)
      context.spawn(WebServer(router), "webserver")

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }
  }

}
