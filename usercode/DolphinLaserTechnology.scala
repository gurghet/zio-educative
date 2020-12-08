import zio.Schedule.{ fixed, recurs }
import zio.clock.{ Clock, sleep }
import zio.console.{ Console, putStrLn }
import zio.duration._
import zio._

object DolphinLaserTechnology extends zio.App {
  final case class Laser(id: Long) extends AnyVal
  case class NoAvailableLaserError()
  case class Dolphin(strappedLaser: Laser) {
    def shootLaser(target: String): URIO[Console, Unit] =
      putStrLn(s"🐬 shooting laser (${strappedLaser.id}) at $target: pew pew!")
  }

  type Arsenal = Has[Ref[List[Laser]]]
  val arsenal = List(
    Laser(31L),
    Laser(32L),
    Laser(62L),
    Laser(66L),
    Laser(138L)
  )
  val initializeInventory: UIO[Arsenal] = Ref.make(arsenal).asService

  val draft: ZIO[ZEnv with Arsenal, NoAvailableLaserError, Dolphin] =
    for {
      inventory           <- ZIO.access[Arsenal](_.get)
      firstAvailableLaser <- acquireLaser(inventory)
      _                   <- putStrLn(s"strapping ${firstAvailableLaser.id} to dolphin")
    } yield Dolphin(firstAvailableLaser)

  val deployDolphin: ZIO[ZEnv with Arsenal, NoAvailableLaserError, Unit] =
    ZIO.bracket(acquire = draft)(release = discharge)(_.shootLaser("enemy"))

  def acquireLaser(
    inventory: Ref[List[Laser]]
  ): IO[NoAvailableLaserError, Laser] = inventory.getAndUpdateSome {
    case List()       => List()
    case head :: tail => tail
  }.flatMap {
    case List()       => ZIO.fail(NoAvailableLaserError())
    case head :: tail => ZIO.succeed(head)
  }

  def discharge(dolphin: Dolphin): URIO[ZEnv with Arsenal, Unit] = {
    val releaseLaser: URIO[Arsenal, Unit] = ZIO.accessM[Arsenal] { inventory =>
      inventory.get.update(_.appended(dolphin.strappedLaser))
    }
    releaseLaser *>
      putStrLn(s"unstrapping dolphin with ${dolphin.strappedLaser.id}")
  }

  val waitForBattleToEnd: URIO[Clock, Unit] = sleep(30.seconds)

  val battle: ZIO[ZEnv with Arsenal, Nothing, Unit] = for {
    battleFiber <- deployDolphin.fork.repeat(fixed(1.seconds)).fork
    _           <- waitForBattleToEnd *> battleFiber.interrupt
  } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    initializeInventory
      .flatMap(inventory => battle.provideSome[ZEnv](_ ++ inventory))
      .exitCode
}
