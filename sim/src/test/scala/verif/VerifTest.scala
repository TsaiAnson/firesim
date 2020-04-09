// package verif

// import chisel3._
// import chisel3.util._
// import chisel3.iotesters._
// import org.scalatest.{Matchers, FlatSpec}

trait Transaction[B <: Bundle] {
	var io : B
	// Will define later when working with constraint solver
  def rand(): Int = 0
}

// // Playing around with scala
// trait Transaction {
//   def rand(): Unit = {
//   	// Gets all fields and sets them to a random int
//   	val r = scala.util.Random
//   	for (field <- this.getClass.getDeclaredFields) {
//   		field.setAccessible(true)
//   		// println(field.getName)
//   		// println(field.get(this))
//   		field.set(this, r.nextInt())
//   	}
//   }
// }

// (c : Module) extends PeekPokeTester(c) {
// How do I mention that T <: Transaction[ANYTHING]
class GenericAgent[T <: Transaction[CAMIOIn]] (c : TestModule) {
	def push(tx:Seq[T]): Unit = {
		// TODO: Figure out how to assert that interfaces are compatible
		// assert(c.io.in.getClass == T.io)

		// Getting Fields of Bundle
  	val accFields = c.io.getClass.getDeclaredFields
  	accFields.map(_.setAccessible(true))

		for (t <- tx) {
			for (field <- accFields) {
				// Need to test whether this works dynamically?
				// poke(field.get(c.io.bits), field.get(t.io))
				println(field.get(c.io), field.get(t.io))
			}
			println("==========")
		}
	}
	// Will implement getMonitoredTransactions() Need to ask Vighnesh
}

// Creating fake "Bundle" class
class Bundle {}

class CAMIOIn extends Bundle {
	var en = "enable"
	var we = "write enable"
	var keyRe = "key to read"
	var keyWr = "key to write"
	var dataWr = "data to write"
}

case class CAMIOInTr(en:String, we:String, keyRe:String, keyWr:String, dataWr:String) extends Transaction[CAMIOIn] {
	var io = new CAMIOIn
	// Would need to automatically set values? Would like to automate it
	// In chisel, we would do this.io.bits.(field) = (value)
	this.io.en = en
	this.io.we = we
	this.io.keyRe = keyRe
	this.io.keyWr = keyWr
	this.io.dataWr = dataWr
}

// Example of what our "module" looks like
class TestModule {
	var io = new CAMIOIn
}

object Test {
	def main(args: Array[String]): Unit = {
    val moduleInst = new TestModule
    val inputAgent = new GenericAgent[CAMIOInTr](moduleInst)
    val inputTransactions = Seq(
    	CAMIOInTr("0", "1", "2", "3", "4"),
    	CAMIOInTr("4", "2", "3", "1", "0"),
    	CAMIOInTr("0", "0", "0", "0", "0"),
    	CAMIOInTr("1", "1", "1", "1", "1"),
    	CAMIOInTr("2", "2", "2", "2", "2"),
    	CAMIOInTr("3", "3", "3", "3", "3"),
    	CAMIOInTr("4", "4", "4", "4", "4")
    )
    inputAgent.push(inputTransactions)
  }
}
