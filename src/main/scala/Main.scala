import akka.actor.typed.ActorSystem
import app.{HttpSettings, Initializer}

object Main extends App {

  val httpSettings = HttpSettings.load()
  val system       = ActorSystem(Initializer(httpSettings), "main")

  println(s"\nServer online at http://${httpSettings.interface}:${httpSettings.port}/\nPress RETURN to stop...\n")

  if (sys.env.get("ENV").contains("LOCAL")) {
    scala.io.StdIn.readLine()
    system.terminate()
  }

}
