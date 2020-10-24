import zio.clock.sleep
import zio.console.putStrLn
import zio.random.nextIntBounded
import zio.duration._
import zio._

object CatAndMouseChasing extends zio.App {
  val makeMouseLead: UIO[Ref[Int]] = Ref.make(10)

  def catProgram(mouseLead: Ref[Int]): ZIO[ZEnv, MouseEscapedException, Unit] =
    for {
      cm   <- nextIntBounded(5)
      lead <- mouseLead.updateAndGet(_ - cm)
      _    <- putStrLn(s"Cat advances by $cm cm, mouse is $lead cm away")
      _    <- sleep(100.milliseconds)
      _    <- catchDetector(lead, mouseLead)
    } yield ()

  def catchDetector(currentLead: Int, mouseLead: Ref[Int]): ZIO[ZEnv, MouseEscapedException, Unit] =
    currentLead match {
      case 0                => putStrLn("Cat catches the mouse!")
      case lead if lead > 0 => catProgram(mouseLead)
      case lead if lead < 0 => ZIO.fail(MouseEscapedException(lead))
    }

  case class MouseEscapedException(catLead: Int)

  def mouseProgram(mouseLead: Ref[Int]): URIO[ZEnv, Nothing] =
    (for {
      cm <- nextIntBounded(2)
      _  <- mouseLead.update(_ + cm)
      _  <- putStrLn(s"Mouse advances by $cm cm")
      _  <- sleep(100.milliseconds)
    } yield ()).forever

  val compositeProgram: URIO[zio.ZEnv, ExitCode] =
    for {
      lead <- makeMouseLead
      exitCode <- catProgram(lead)
        .raceFirst(mouseProgram(lead))
        .catchAll(exc => putStrLn(s"Cat was ${-exc.catLead} cm ahead when it lost the mouse."))
        .as(ExitCode.success)
    } yield exitCode

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = compositeProgram
}
