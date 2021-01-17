import zio.{ App, ExitCode, URIO }
object Nucular extends zio.App {
  val enrichUranium: Unit                                        = println("enriching uranium")
  val miningUranium: Unit                                        = println("mining uranium")
  val reachCritical: Unit                                        = println("uranium reached critical mass")
  def nuke(target: String): Unit                                 = println(s"nuking $target")
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = ???
}
