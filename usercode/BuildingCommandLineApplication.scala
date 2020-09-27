import zio.console.{Console, getStrLn, putStrLn}
import zio.random.{Random, nextIntBetween}
import zio.{ExitCode, RIO, Task, URIO}

object BuildingCommandLineApplication extends zio.App {
  val newGame: RIO[Console with Random, Unit] = for {
    number <- nextIntBetween(1, 10)
    _      <- putStrLn("Can you guess the number?")
    _      <- guessLoop(number)
  } yield ()

  val program: RIO[Console with Random, Unit] = for {
    _ <- putStrLn("Welcome to Guess The Number!")
    _ <- newGame
  } yield ()

  val prompt: RIO[Console, Int] = getStrLn.flatMap(parseInt)

  def parseInt(input: String): RIO[Console, Int] =
    Task(input.toInt)
      .catchAll(_ => putStrLn(s"""Cannot parse "$input"""") *> prompt)

  def guessLoop(number: Int): RIO[Console with Random, Unit] =
    for {
      guess <- prompt
      _ <-
        if (guess == number)
          putStrLn("Correct!") *> newGame
        else
          giveHint(guess, number) *> guessLoop(number)
    } yield ()

  def giveHint(guess: Int, number: Int): URIO[Console, Unit] =
    if (guess > number)
      putStrLn("Too high, try again...")
    else
      putStrLn("Too low, try again...")

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode
}
