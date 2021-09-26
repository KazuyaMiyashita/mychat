import akka.actor.typed.ActorSystem
import app.Initializer

object Main extends App {

  val interface = "0.0.0.0"
  val port      = sys.env.getOrElse("PORT", "8080").toInt

  val system = ActorSystem(Initializer(interface, port), "main")

  println(s"\nServer online at http://$interface:$port/\nPress RETURN to stop...\n")

  if (sys.env.get("ENV").contains("LOCAL")) {
    scala.io.StdIn.readLine()
    system.terminate()
  }

}
