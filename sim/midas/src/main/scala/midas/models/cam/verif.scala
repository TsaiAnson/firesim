package verif

import org.scalatest._

import chisel3._
import chiseltest._
import chisel3.util._

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

// Using hardcoded for now (ParameterizedCAMAssociative instead of Model) etc
class GenericDriver[T <: CAMIOIn] (c : ParameterizedCAMAssociative) {
	def push(tx:Seq[T]): Unit = {
		// TODO: Figure out how to assert that interfaces are compatible
		// assert(c.io.in.getClass == T.io)

		// Getting Fields of Bundle
  	val accFields = c.io.getClass.getDeclaredFields
  	accFields.map(_.setAccessible(true))

		for (t <- tx) {
			// for (field <- accFields) {
			// 	// println(field.get(c.io), field.get(t.io))
			// 	field.get(c.io).poke(field.get(t.io.bits))
			// }
			// Using hardcoded for now
			c.io.en.poke(t.io.en)
			c.io.we.poke(t.io.we)
			c.io.keyRe.poke(t.io.keyRe)
			c.io.keyWr.poke(t.io.keyWr)
			c.io.dataWr.poke(t.io.dataWr)
			c.clock.step()
		}
	}
}

// Using hardcoded for now (ParameterizedCAMAssociative instead of Model)
class GenericMonitor[T <: CAMIOOut] (c : ParameterizedCAMAssociative) {
	var monitoredTransactions = Seq[T]()

	def getMonitoredTransactions(): Seq[T] = {
		return monitoredTransactions;
	}

	def clearMonitoredTransactions(): Unit = {
		monitoredTransactions = Seq[T]()
	}	

	fork.withRegion(Monitor) {
		while(true) {
			// Hardcoded for now, can work with MACROs later
			monitoredTransactions = :+ T(c.io.found, c.io.dataRe)
			c.clock.step()
		}
	}
}
