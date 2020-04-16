package verif

// Case Class for Input and Output Signals
case class SWInput(en: Boolean, we: Boolean, keyRe: Int, keyWr: Int, dataWr: Int)
case class SWOutput(found: Boolean, dataRe: Int)

class SWAssocCAM (keyWidth: Int, dataWidth: Int, memSizeWidth: Int) {
  var valid = new Array[Boolean](memSizeWidth)
  var keys = new Array[Int](memSizeWidth)
  var values = new Array[Int](memSizeWidth)
  var writeIndex = 0

  def process (input:SWInput) : SWOutput = {
    // Processing Writes First
    if (input.we) {
      valid(writeIndex) = true
      keys(writeIndex) = input.keyWr
      values(writeIndex) = input.dataWr
      writeIndex = if (writeIndex == (memSizeWidth - 1)) 0 else (writeIndex + 1)
    }

    println(keys.mkString(" "))
    println(values.mkString(" "))

    // Processing Reads
    var temp_index = -1
    if (input.en) {
      temp_index = keys.indexOf(input.keyRe)
    }

    var found = false
    var dataRe = values.last
    if (temp_index >= 0 && valid(temp_index)) {
      found = true
      dataRe = values(temp_index)
    }

    SWOutput(found, dataRe)
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    var swcam = new SWAssocCAM(8,8,8)
    var out = swcam.process(SWInput(true, false, 123, 0, 0))
    println(out.found)
    out = swcam.process(SWInput(true, false, 0, 0, 0))
    println(out.found)
    out = swcam.process(SWInput(true, true, 0, 123, 123))
    println(out.found)
    out = swcam.process(SWInput(true, true, 0, 456, 789))
    println(out.found)
    out = swcam.process(SWInput(true, true, 0, 0, 7809))
    println(out.found)
  }
}

