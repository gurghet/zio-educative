import zio._
import zio.clock.sleep
import zio.console.putStrLn
import zio.duration.{Duration, durationInt}
import zio.test.Assertion.{containsString, equalTo, hasLast}
import zio.test.TestAspect.sequential
import zio.test._
import zio.test.environment.{TestClock, TestConsole, TestEnvironment}
object UninterruptibleSurgeon {
  val patientGutsM: UIO[Ref[String]] = Ref.make("broken heart")

  def surgeon(patientGuts: Ref[String]): URIO[ZEnv, Unit] =
    for {
      _ <- putStrLn("Today is a good day! I'll have breakfast")
      _ <- sleep(30.minutes)
      _ <- putStrLn("Time to go to work!")
      _ <- sleep(30.minutes)
      _ <- putStrLn("Operating the patient")
      surgery =
        patientGuts.set("open guts") *>
          sleep(7.hours) *>
          patientGuts.set("mended heart")
      _ <- surgery.uninterruptible
      _ <- putStrLn("All done, time to go home!")
    } yield ()

  def surgeonInstagram(notificationTimer: Duration): URIO[ZEnv, Unit] =
    sleep(notificationTimer) *> putStrLn("Ariana Grande published a photo!")

  def program(
      patientGuts: Ref[String],
      notificationTimer: Duration
  ): URIO[ZEnv, Either[Unit, Unit]] =
    surgeon(patientGuts) <|> surgeonInstagram(notificationTimer)
}

object UninterruptibleSurgeonSpecs extends DefaultRunnableSpec {
  import UninterruptibleSurgeon._
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("Uninterruptible Surgeon")(
      testM("should interrupt breakfast to check Instagram") {
        for {
          patientGuts  <- patientGutsM
          _            <- program(patientGuts, 45.minutes) <|> TestClock.adjust(45.minutes)
          outputBuffer <- TestConsole.output
        } yield assert(outputBuffer)(hasLast(containsString("Ariana Grande")))
      },
      testM("should not interrupt surgery to check Instagram") {
        for {
          patientGuts     <- patientGutsM
          _               <- program(patientGuts, 4.hours) <|> TestClock.adjust(8.hours)
          currentGutState <- patientGuts.get
        } yield assert(currentGutState)(equalTo("mended heart"))
      }
    ) @@ sequential
}
