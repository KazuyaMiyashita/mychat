import scala.io.StdIn
import akka.actor.typed.ActorSystem

object Main extends App {

  val system = ActorSystem(Initializer(), "main")

  println("\nServer online at http://localhost:8080/\nPress RETURN to stop...\n")
  StdIn.readLine()
  system.terminate()

}
