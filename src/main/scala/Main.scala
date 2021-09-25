import scala.io.StdIn
import akka.actor.typed.ActorSystem

object Main extends App {

  val system = ActorSystem(Initializer(), "main")

  println("\naaa Server online at http://0.0.0.0:8080/\nPress RETURN to stop...\n")
  StdIn.readLine()
  system.terminate()

}
