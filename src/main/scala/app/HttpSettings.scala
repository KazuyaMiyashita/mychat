package app

import com.typesafe.config.{Config, ConfigFactory}
import scala.jdk.CollectionConverters._

case class HttpSettings(
    interface: String,
    port: Int,
    allowOrigins: List[String]
)

object HttpSettings {

  def load(): HttpSettings = {
    val conf: Config = ConfigFactory.load()

    HttpSettings(
      interface = conf.getString("interface"),
      port = conf.getInt("port"),
      allowOrigins = conf.getStringList("allowOrigins").asScala.toList
    )
  }

}
