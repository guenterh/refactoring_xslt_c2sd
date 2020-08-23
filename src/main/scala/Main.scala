import java.io.{File, FileInputStream, InputStream}
import java.nio.file.{Files, Paths}
import java.util.stream.Collectors
import java.util.zip.GZIPInputStream

import helper.{TemplateCreator, TemplateTransformer, XSLTDataObject}
import utilities.OptionsParser
import utilities.OptionsParser.OptionMap

import scala.io.Source
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, Node,XML}

object Main extends App {

  val options: OptionMap = OptionsParser.nextOption(Map(), args.toList)
  assert(options.contains(Symbol("indir")))

  private object FileList {

    def getFileList: java.util.List[String] = {
      Files.walk(Paths.get(options(Symbol("indir")).toString)).
        filter(Files.isRegularFile(_)).map[String](_.toString).collect(Collectors.toList[String])

    }

    def getStream(fileName: String): InputStream = {
      val infile = new File(fileName)

      val nameInFile: String = infile.getAbsoluteFile.getName
      val zipped = if (nameInFile.matches(""".*?.gz$""")) true else false
      val source: InputStream = if (zipped) {
        new GZIPInputStream(new FileInputStream(infile))
      } else {
        new FileInputStream(infile)
      }
      source
    }

  }

  FileList.getFileList.forEach(f => {

    val source = FileList.getStream(f)
    XMLTransformation.transformSource(source)
  })


  object XMLTransformation {

    private def isRecord(line: String) = line.startsWith("<record>")

    private val TRANSFORMERIMPL = "net.sf.saxon.TransformerFactoryImpl"
    private val holdingsStructure = "collect.holdings.xsl"
    private val weedholdings = "weedholdings.xsl"
    private val solrstep1 = "swissbib.solr.step1.xsl"
    private val specialgreen = "vufind.special.green.xsl"
    private val solrstep2 = "swissbib.solr.vufind2.xsl"


    private lazy val startTransformer = {
      new TemplateCreator(TRANSFORMERIMPL, holdingsStructure).createTransformerFromResource
    }

    private lazy val weedTransformer = {
      new TemplateCreator(TRANSFORMERIMPL, weedholdings).createTransformerFromResource
    }

    private lazy val step1Transformer = {
      new TemplateCreator(TRANSFORMERIMPL, solrstep1).createTransformerFromResource
    }

    private lazy val specialGreenTransformer = {
      new TemplateCreator(TRANSFORMERIMPL, specialgreen).createTransformerFromResource
    }

    private lazy val step2Transformer = {
      new TemplateCreator(TRANSFORMERIMPL, solrstep2).createTransformerFromResource
    }

    protected def parseRecord(line: String): Elem = XML.loadString(line)

    def transformSource(stream: InputStream): Unit = {
      val it = Source.fromInputStream(stream).getLines()
      for (line <- it if isRecord(line)) {

        //val elem = parseRecord(line)
        val holdings = new TemplateTransformer(line).transform(startTransformer)

        val dataObject = new XSLTDataObject
        dataObject.additions.put("holdingsStructure", holdings)
        dataObject.record = line

        val weedHoldingsTransformation = new TemplateTransformer(dataObject.record).transform(weedTransformer)
        dataObject.record = weedHoldingsTransformation

        val step1Transformation = new TemplateTransformer(dataObject.record).transform(step1Transformer)
        dataObject.record = step1Transformation
        val specialGreenTransformation = new TemplateTransformer(dataObject.record).transform(specialGreenTransformer)

        dataObject.record = specialGreenTransformation

        step2Transformer.setParameter("holdingsStructure", dataObject.additions.get("holdingsStructure"))
        val step2Transformation = new TemplateTransformer(dataObject.record).transform(step2Transformer)


        val elem = parseRecord(step2Transformation)

        val last = ruleDuplicate(elem)
        println(last)


      }

    }

    def ruleDuplicate(structure: Elem): collection.Seq[Node] = {

      val filterDuplicates: RewriteRule = new RewriteRule {
        override def transform(n: Node): Seq[Node] = n match {
          case <preparededup>{childfield @ _*}</preparededup> =>
            for {
              elem <- (childfield \\ "field")
              setItem: String <- elem.text.split("##xx##").toSeq.toSet
              //todo: make it exception secure with Try
            } yield <field name={elem.attribute("name").get.text}>{setItem.trim}</field>
          case item => item
        }
      }
      new RuleTransformer(filterDuplicates).transform(structure)

    }
  }
}
