import timers.{getSecondsLeft, startCountdown}
import zio.clock.{Clock, instant}
import zio.console.putStrLn
import zio.duration.durationInt
import zio.test.Assertion.equalTo
import zio.test.TestAspect.ignore
import zio.test.environment.{TestClock, TestEnvironment}
import zio.test._
import zio._

object TestOne extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("big red button")(
      testM("One can move time very fast") {
        (for {
          fiber <- instant.tap(i => putStrLn(i.toString)).fork
          _     <- TestClock.adjust(7.seconds)
//          sleeps <- TestClock.sleeps
//          _       <- putStrLn(sleeps.mkString(", "))
          instant <- fiber.join
          _       <- putStrLn(instant.toString)
        } yield assertCompletes).provideSomeLayer[TestEnvironment](timers.live)
      } @@ ignore,
      testM("One can control time as he see fit") {
        for {
          promise <- Promise.make[Unit, Int]
          int     <- Ref.make(60)
          _       <- (ZIO.sleep(10.seconds) *> promise.succeed(1)).fork
          _       <- int.updateAndGet(_ - 1).tap(d => putStrLn(d.toString)).repeat(Schedule.spaced(1.second)).fork
          _       <- TestClock.adjust(10.seconds)
          readRef <- promise.await
          result  <- int.get
        } yield assert(result)(equalTo(3))
      } @@ ignore,
      testM("real") {
        val partialLayer = ZLayer.identity[Clock] >>> timers.live
        val test = for {
          _  <- startCountdown()
          _  <- TestClock.adjust(10.seconds)
          sl <- getSecondsLeft
        } yield sl
        val result = test.provideSomeLayer[TestEnvironment](partialLayer)
        assertM(result)(equalTo(4))
      }
    )
}

// external classes
class Button private (var clicks: Int) {
  def click(): Unit = clicks += 1
}
object Button {
  def apply(): Button = new Button(0)
}

// external module
object timers {
  type Countdown = Has[Countdown.Service]
  object Countdown {
    trait Service {
      def resetCountdown(): UIO[Unit]
      def getSecondsLeft: UIO[Int]
      def startCountdown(): UIO[Unit]
    }
  }

  def resetCountdown(): URIO[Countdown, Unit] = ZIO.accessM[Countdown](_.get.resetCountdown())
  def getSecondsLeft: URIO[Countdown, Int]    = ZIO.accessM[Countdown](_.get.getSecondsLeft)
  def startCountdown(): URIO[Countdown, Unit] = ZIO.accessM[Countdown](_.get.startCountdown())

  val live: URLayer[Clock, Countdown] = (Ref.make(60) <* UIO(println("creating ref")))
    .flatMap { secondsRef =>
      UIO(println("accessing clock")) *> ZIO.access[Clock](clock =>
        new Countdown.Service {
          private val subtractOneEachSecond =
            secondsRef
              .updateAndGet(seconds => seconds - 1)
              .tap(d => UIO(println("bau = " + d)))
              .repeat(Schedule.spaced(1.second))
              .delay(1.second)
          override def resetCountdown(): UIO[Unit] = secondsRef.set(0)
          override def getSecondsLeft: UIO[Int]    = secondsRef.get <* UIO(println("getting seconds left"))
          override def startCountdown(): UIO[Unit] = subtractOneEachSecond.fork.ignore.provide(clock) <* UIO(println("starting..."))
        }
      )
    }
    .toLayer
}
