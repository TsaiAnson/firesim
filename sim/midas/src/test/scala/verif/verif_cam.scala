package verif

import org.scalatest._

import chisel3._
import chiseltest._
import chisel3.util._

import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation

case class CAMIOInTr(en:Boolean, we:Boolean, keyRe: Int, keyWr: Int, dataWr: Int) extends Transaction

case class CAMIOOutTr(found:Boolean, dataRe:Int) extends Transaction

class CamTest extends FlatSpec with ChiselScalatestTester {
	behavior of "Testers2 for CAM"

	it should "basic test to see if Transactions are working" in {
		test(new ParameterizedCAMAssociative(8,8,8)).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
			val camInAgent = new GenericDriver[CAMIOInTr](c)
			val camOutAgent = new GenericMonitor[CAMIOOutTr](c)
			val inputTransactions = Seq(
				CAMIOInTr(false, true, 0, 25, 123),
				CAMIOInTr(true, false, 25, 2, 234)
			)
			camInAgent.push(inputTransactions)
			c.clock.step(100)
			val output = camOutAgent.getMonitoredTransactions()
			for (t <- output) {
				println(t.found, t.dataRe)
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