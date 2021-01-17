import zio.clock.Clock
import zio.console.{ Console, putStr, putStrLn }
import zio.{ ExitCode, IO, Promise, Ref, UIO, URIO, ZEnv, ZIO, clock, random }
import zio.duration._
import zio.random.Random
import zio.stream.ZStream

object Laundry extends zio.App {
  case class Clothing(millisecondsRequiredForCleaning: Int)
  case class CouldNotClean() extends Exception

  def cleaningFiber(clothing: Clothing): URIO[Clock, Clothing] =
    clock.sleep(clothing.millisecondsRequiredForCleaning.milliseconds) *>
      UIO(clothing)

  val clientWalkInSimulator: URIO[ZEnv, Clothing] = for {
    randomCleaningTime <- random.nextIntBetween(100, 800)
    randomClothing      = Clothing(randomCleaningTime)
    _                  <- putStrLn(s"Please clean this in $randomCleaningTime milliseconds")
  } yield randomClothing

  def clientWalksOutSimulator(
    blueTicket: Promise[CouldNotClean, Clothing]
  ): URIO[Console, Unit] =
    (blueTicket.await *> putStrLn("Thanks! Bye!")).catchSome {
      case CouldNotClean() => putStrLn("This is still dirty!")
    }.orDie

  val program = for {
    clock      <- ZIO.environment[Clock]
    clothing   <- clientWalkInSimulator
    blueTicket <- Promise.make[CouldNotClean, Clothing]
    _ <- blueTicket.complete(cleaningFiber(clothing).provide(clock)) *>
           clientWalksOutSimulator(blueTicket)
  } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program.exitCode
}

object BreadFactory extends zio.App {
  case class Bread(shape: String)

  var counter = Array.empty[Bread]

  def bake(shape: String): URIO[ZEnv, Promise[Nothing, Bread]] = for {
    promise <- Promise.make[Nothing, Bread]
    _       <- (clock.sleep(3.second) *> promise.complete(UIO(Bread(shape)))).fork
    _       <- putStrLn(s"One order for $shape!")
  } yield promise

  def randomShape: URIO[Random, String] = {
    val shapeLibrary = List("Ciabatta", "Panino", "Libretto", "Baguette")
    random.nextIntBetween(0, 4).map(shapeLibrary)
  }

  def waitBreadAndPutOnCounter(
    breadPromise: Promise[Nothing, Bread]
  ): URIO[ZEnv, Unit] = breadPromise.await.flatMap(bread =>
    UIO(counter :+= bread) *> putStrLn(s"Thanks for the $bread")
  )

  val customerGenerator: ZStream[ZEnv, Nothing, String] =
    ZStream.repeatEffect(randomShape).throttleShape(1, 500.milliseconds)(_ => 1)

  val breadCollector: URIO[ZEnv, Unit] = customerGenerator
    .mapM(requestedShape => bake(requestedShape))
    .tap(promiseOfBread => waitBreadAndPutOnCounter(promiseOfBread).fork)
    .runDrain

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    breadCollector.exitCode
}
