class Cappuccino(var coffee: Int, var milk: Int)
object CoffeeMachine extends App {
  def pourCoffee(cappuccino: Cappuccino): Unit = cappuccino.coffee += 1
  def pourMilk(cappuccino: Cappuccino): Unit = cappuccino.milk +=
    cappuccino.coffee * 5

  // recipe
  // get a new cappuccino
  val cappuccino = new Cappuccino(0, 0)
  // pour one part of coffee
  pourCoffee(cappuccino)
  // pour five parts of milk
  pourMilk(cappuccino)
  // your cappuccino is ready!
  println(
    s"Your cappuccino is ${cappuccino.coffee} parts coffee and ${cappuccino.milk} parts milk"
  )
}
