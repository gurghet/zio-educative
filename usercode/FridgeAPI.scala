import java.net.InetSocketAddress
import uzhttp.Response
import uzhttp.server.Server
import zio._
import scala.util.Random

object FridgeAPI extends zio.App {
  object Fridge {
    type Fridge = Has[Fridge.Service]
    trait Service {
      def temperature: Double
      def doorState: String
    }

    def temperature: URIO[Fridge, Double] = ZIO.access[Fridge](_.get.temperature)
    def doorState: URIO[Fridge, String] = ZIO.access[Fridge](_.get.doorState)

    object Service {
      val simulator: Service = new Service {
        private val random = new Random
        override def temperature: Double = 2.5 + random.nextGaussian()
        override def doorState: String = Seq("open\n", "closed\n")(random.nextInt(2))
      }
    }

    val simulator: ULayer[Fridge] =
      ZLayer.succeed(Fridge.Service.simulator)
  }
  import Fridge.{doorState, temperature}
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server.builder(new InetSocketAddress("0.0.0.0", 5000))
      .handleSome {
        case req if req.uri.getPath == "/temperature" =>
          temperature.flatMap(t => ZIO.succeed(Response.plain(s"${t.formatted("%.1f")}ºC\n")))
        case req if req.uri.getPath == "/door-state" =>
          doorState.flatMap(d => ZIO.succeed(Response.plain(d)))
      }.serve.useForever.orDie.provideCustomLayer(Fridge.simulator)
}
