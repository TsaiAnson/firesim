package verif

import org.scalatest._

import chisel3._
import chiseltest._
import chisel3.util._

class CAMIOIn extends Bundle {
	val en = Input(Bool())
  val we = Input(Bool())
  val keyWr = Input(UInt(8.W))
  val keyRe = Input(UInt(8.W))
  val dataWr = Input(UInt(8.W))
  
}

class CAMIOOut extends Bundle {
	val found = Output(Bool())
	val dataRe = Output(UInt(8.W))
}

case class CAMIOInTr(en:Bool, we:Bool, keyRe: Int, keyWr: Int, dataWr: Int) extends Transaction[CAMIOIn] {
	var io = new CAMIOIn

	// Use MACRO to generate these?
	this.io.en.bits = en
	this.io.we.bits = we
	this.io.keyRe.bits = keyRe.asUInt(8.W)
	this.io.keyWr.bits = keyWr.asUInt(8.W)
	this.io.dataWr.bits = dataWr.asUInt(8.W)
}

case class CAMIOOutTr(found:Bool, dataRe:Int) extends Transaction[CAMIOIn] {
	var io = new CAMIOOut
	// Would need to automatically set values? Would like to automate it
	// In chisel, we would do this.io.bits.(field) = (value)
	this.io.found.bits = found
	this.io.dataRe.bits = dataRe.asUInt(8.W)
}

class CamTest extends FlatSpec with ChiselScalatestTester {
	behavior of "Testers2 for CAM"

	it should "basic test to see if Transactions are working" in {
		test(() => ParameterizedCAMAssociative(8, 8, 8)) { c =>
			val camInAgent = new GenericDriver[CAMIOInTr](c)
			val camOutAgent = new GenericMonitor[CAMIOOutTr](c)
			val inputTransactions = Seq(
				CAMIOInTr(true.B, false.B, 25, 0, 123),
				CAMIOInTr(false.B, true.B, 25, 25, 234)
			)
			camInAgent.push(inputTransactions)
			val output = camOutAgent.getMonitoredTransactions()
			for (t <- output) {
				println(t.io.found, t.io.dataRe)
				println("========")
			}
		}
	}
}

// object Test {
// 	def main(args: Array[String]): Unit = {
//     val moduleInst = new TestModule
//     val inputAgent = new GenericAgent[CAMIOInTr](moduleInst)
//     val inputTransactions = Seq(
//     	CAMIOInTr("0", "1", "2", "3", "4"),
//     	CAMIOInTr("4", "2", "3", "1", "0"),
//     	CAMIOInTr("0", "0", "0", "0", "0"),
//     	CAMIOInTr("1", "1", "1", "1", "1"),
//     	CAMIOInTr("2", "2", "2", "2", "2"),
//     	CAMIOInTr("3", "3", "3", "3", "3"),
//     	CAMIOInTr("4", "4", "4", "4", "4")
//     )
//     inputAgent.push(inputTransactions)
//   }
// }