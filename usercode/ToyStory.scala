import zio.clock.{Clock, sleep}
import zio.console.{Console, putStrLn}
import zio.duration.durationInt
import zio.{Ref, UIO, URIO}
object ToyStory {
  sealed trait ToyBehaviour
  case object StayingStill extends ToyBehaviour
  case object BeingAlive   extends ToyBehaviour

  val buzzLightYear: UIO[Ref[ToyBehaviour]] = Ref.make(StayingStill)

  def buzzRoutine(toyState: Ref[ToyBehaviour]): URIO[Clock with Console, Unit] =
    toyState.set(BeingAlive) *> sleep(8.hours)
      .onInterrupt(putStrLn("Someone is at the door!") *> toyState.set(StayingStill))

  val andyRoutine: URIO[Clock with Console, Unit] =
    putStrLn("Going for an ice cream!") *> sleep(1.hours)

  def andyReaction(toyState: Ref[ToyBehaviour]): URIO[Console, Unit] =
    toyState.get.flatMap { toyBehaviour =>
      if (toyBehaviour == StayingStill)
        putStrLn("Everything seems normal")
      else
        putStrLn("Wait, toys are alive?")
    }

  val film: URIO[Console with Clock, Unit] = for {
    buzzState <- buzzLightYear
    _         <- buzzRoutine(buzzState) raceEither andyRoutine
    _         <- andyReaction(buzzState)
  } yield ()
}

import zio.test.Assertion.{containsString, hasLast}
import zio.test._
import zio.test.environment.{TestClock, TestConsole, TestEnvironment}

object ToyStorySpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("A Toy Story")(
      testM("Toys should not be alive when observed") {
        assertM((ToyStory.film raceEither TestClock.adjust(1.hours)) *> TestConsole.output)(
          hasLast(containsString("Everything seems normal"))
        )
      }
    )
}
