import zio.console.putStrLn
import zio.test.Assertion.{containsString, hasFirst}
import zio.test._
import zio.test.environment.{TestConsole, TestEnvironment}
import zio._
import zio.clock.sleep
import zio.duration.durationInt

object RocketBOM {
  val engine: IO[String, String]   = ZIO.fail("missing steel")
  val fuselage: URIO[ZEnv, String] = sleep(5.hours) *> UIO("===>")
  def assembleRocket(bom: (String, String)): URIO[ZEnv, Unit] =
    putStrLn(bom._1 + bom._2)
  val rocketBOM: ZIO[ZEnv, String, Unit] =
    engine zipPar fuselage >>= assembleRocket
}

object RocketBOMSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("Rocket")(
      testM("should be assembled") {
        for {
          _            <- RocketBOM.rocketBOM
          outputBuffer <- TestConsole.output
        } yield assert(outputBuffer)(hasFirst(containsString("[**]===>")))
      }
    )
}
