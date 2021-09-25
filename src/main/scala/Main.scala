import akka.actor.typed.ActorSystem

object Main extends App {

  val interface = "0.0.0.0"
  val port      = sys.env.getOrElse("PORT", "8080").toInt

  val system = ActorSystem(Initializer(interface, port), "main")

  println(s"\nServer online at http://$interface:$port/\nPress RETURN to stop...\n")

}
