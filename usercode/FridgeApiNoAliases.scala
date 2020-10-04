import java.net.InetSocketAddress

import uzhttp.Response
import uzhttp.server.Server
import zio._

import scala.util.Random

object FridgeApiNoAliases extends zio.App {
  object Fridge {
    trait Service {
      def temperature: Double
      def doorState: String
    }

    object Service {
      val simulator: Service = new Service {
        private val random = new Random
        override def temperature: Double = 2.5 + random.nextGaussian()
        override def doorState: String = Seq("open\n", "closed\n")(random.nextInt(2))
      }
    }

  }
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.builder(new InetSocketAddress("0.0.0.0", 5000))
      .handleSome {
        case req if req.uri.getPath == "/temperature" =>
          ZIO.access[Has[Fridge.Service]](fridge =>
            Response.plain(s"${fridge.get.temperature.formatted("%.1f")}ºC\n")
          )
        case req if req.uri.getPath == "/door-state" =>
          ZIO.access[Has[Fridge.Service]](fridge =>
            Response.plain(fridge.get.doorState)
          )
      }.serve.useForever.orDie.provideSome[ZEnv](zenv => zenv ++ Has(Fridge.Service.simulator))
}
