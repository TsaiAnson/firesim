//See LICENSE for license details.

package firesim.util

import chisel3._

import freechips.rocketchip.config.{Field, Config, Parameters}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp}

import firesim.bridges._
import firesim.configs.MemModelKey
import midas.widgets.{Bridge, PeekPokeBridge, RationalClockBridge}

// Creates a wrapper FireSim harness module that instantiates bridges based
// on the scala type of the Target (_not_ its IO). This avoids needing to
// duplicate harnesses (essentially test harnesses) for each target.
//
// You could just as well create a custom harness module that instantiates
// bridges explicitly, or add methods to
// your target traits that instantiate the bridge there (i.e., akin to
// SimAXI4Mem). Since cake traits live in Rocket Chip it was easiest to match
// on the types rather than change trait code.

// A sequence of partial functions that match on the type the DUT (_not_ it's
// IO) to generate an appropriate bridge. You can add your own bridge by prepending 
// a custom PartialFunction to this Seq
case object BridgeBinders extends Field[Seq[PartialFunction[Any, Seq[Bridge[_,_]]]]](Seq())

// Config sugar that accepts a partial function and prepends it to BridgeBinders
class RegisterBridgeBinder(pf: =>PartialFunction[Any, Seq[Bridge[_,_]]]) extends Config((site, here, up) => {
  case BridgeBinders => pf +: up(BridgeBinders, site)
})

// Determines the number of times to instantiate the DUT in the harness.
// Subsumes legacy supernode support
case object NumNodes extends Field[Int](1)

class WithNumNodes(n: Int) extends Config((pname, site, here) => {
  case NumNodes => n
})

case class FireSimClockParameters(additionalClocks: Seq[(Int,Int)]) {
  def numClocks(): Int = additionalClocks.size + 1
}
case object FireSimClockKey extends Field[FireSimClockParameters](FireSimClockParameters(Seq()))

trait HasAdditionalClocks extends LazyModuleImp {
  val clocks = IO(Vec(p(FireSimClockKey).numClocks, Input(Clock())))
}

class DefaultFireSimHarness[T <: LazyModule](dutGen: () => T)(implicit val p: Parameters) extends RawModule {
  val lazyDut = LazyModule(dutGen())
  val clockBridge = Module(new RationalClockBridge(1000, p(FireSimClockKey).additionalClocks:_*))
  val refClock = clockBridge.io.clocks(0)
  val reset = WireInit(false.B)
  withClockAndReset(refClock, reset) {
    // Instantiate multiple instances of the DUT to implement supernode
    val targets = Seq.fill(p(NumNodes))(Module(lazyDut.module))
    val peekPokeBridge = PeekPokeBridge(refClock, reset)
    // A Seq of partial functions that will instantiate the right bridge only
    // if that Mixin trait is present in the target's class instance
    //
    // Apply each partial function to each DUT instance
    for ((target) <- targets) {
      p(BridgeBinders).map(_.lift).flatMap(elaborator => elaborator(target))
    }
    targets.collect({ case t: HasAdditionalClocks => t.clocks := clockBridge.io.clocks })
  }
}