package utilities


import scala.collection.Seq
import scala.util.Random
import scala.xml.{Node, PCData, Text}
import scala.xml.transform.RewriteRule

object FilterDuplicates extends Function1[Seq[Node], Seq [Node]] {
  override def apply(v1: Seq[Node]): Seq[Node] = {
    for {
      elem <- (v1 \\ "field")
      setItem: String <- elem.text.split("##xx##").toSeq.toSet
      //todo: make it exception secure with Try
    } yield <field name={elem.attribute("name").get.text}>{setItem.trim}</field>
  }
}

object EnrichFulltext extends Function1[String, Seq [Node]] {

  Random.nextBoolean()
  override def apply(v1: String): Seq[Node] = {
    if (Random.nextBoolean()) {
      <fulltext>{PCData(v1)}</fulltext>
    } else {
      Seq[Node]()
    }
  }
}

object XmlTransformerEnrichFulltextRules extends RewriteRule {

  override def transform(n: Node): Seq[Node] = n match {
    case <preparefulltext>{Text(text)}</preparefulltext> =>
      println(text)
      //<fulltext>{ Unparsed(s"![CDATA[$text]]")}</fulltext>
      <fulltext>{ PCData(text)}</fulltext>

  }


}
