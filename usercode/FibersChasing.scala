import zio.console.Console.Service.live.putStrLn
import zio.{ExitCode, Ref, UIO, URIO, ZEnv, ZIO}
object FibersChasing extends zio.App {
  val variable = Ref.make(0)

  def program(variable: Ref[Int]): URIO[ZEnv, Unit] =
    variable
      .updateAndGet(_ + 1)
      .flatMap(v =>
        if (v == 40000000) ZIO.succeed(())
        else
          putStrLn("loading") *> program(variable) *> putStrLn("unload")
      )

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    variable.flatMap(v => program(v) <|> program(v)).exitCode
}
