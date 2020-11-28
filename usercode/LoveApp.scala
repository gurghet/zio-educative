import predictor.Matcher
import zio.console.{ Console, getStrLn, putStr, putStrLn }
import zio.macros.accessible
import zio.{ ExitCode, Has, UIO, ULayer, URIO, ZIO, ZLayer }

object MatchCalculator extends zio.App {
  val program: ZIO[Console with Matcher, Serializable, Unit] = for {
    name1  <- putStr("Enter your name: ") *> getStrLn
    name2  <- putStr("Enter your crush name: ") *> getStrLn
    result <- Matcher.computeMatch(name1, name2)
    _      <- putStrLn(s"You are a $result% match!")
  } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program.provideCustomLayer(Matcher.live).exitCode
}

object predictor {
  type Matcher = Has[Matcher.Service]

  @accessible
  object Matcher {
    trait Service {
      def computeMatch(name1: String, name2: String): UIO[Int]
    }

    val live: ULayer[Matcher] =
      ZLayer.succeed((name1: String, name2: String) => UIO(100))
  }
}
