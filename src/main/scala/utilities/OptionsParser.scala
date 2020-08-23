package utilities

object OptionsParser {

  type OptionMap = Map[Symbol, Any]

  def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
    def isSwitch(s : String) = (s(0) == '-')
    list match {
      case Nil => map
      case "--indir" :: value :: tail =>
        nextOption(map ++ Map('indir -> value), tail)
      case "--outdir" :: value :: tail =>
        nextOption(map ++ Map('outdir -> value), tail)
      case "--outFileSlaves" :: value :: tail =>
        nextOption(map ++ Map('outFileSlaves -> value), tail)
      case "--inFileSlaveIds" :: value :: tail =>
        nextOption(map ++ Map('inFileSlaveIds -> value), tail)
      //case string :: opt2 :: tail if isSwitch(opt2) =>
      //  nextOption(map ++ Map('infile -> string), list.tail)
      //case string :: Nil =>  nextOption(map ++ Map('infile -> string), list.tail)
      case option :: tail => println("Unknown option "+option); nextOption(map , list.tail)
    }
  }


}
