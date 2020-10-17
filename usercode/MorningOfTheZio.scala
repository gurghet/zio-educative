import zio.console.{Console, putStr}
import zio.{ExitCode, URIO}

object MorningOfTheZio extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode

  val program: URIO[Console, Unit] = for {
    _ <- putStr("I’m awake!")
    _ <- putStr("3,141592653")
  } yield ()
}

import zio.test.Assertion.equalTo
import zio.test.Assertion.hasFirst
import zio.test._
import zio.test.environment.{TestConsole, TestEnvironment}

object MorningOfTheZioSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("making sure zio sleeps enough")(
      testM("zio starts the day awake") {
        val consoleBuffer = MorningOfTheZio.program *> TestConsole.output
        assertM(consoleBuffer)(hasFirst(equalTo("I’m awake!")))
      }
    )
}
