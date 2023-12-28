import org.jsoup.Jsoup
import org.jsoup.select.Elements

import collection.JavaConverters._
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`


object CrawlerS {

val MAX_DEPTH = 3;
  def crawl(targ: String, depth: Int = 3, rep: Reporter): Unit = {



    def _crawl(urls2crawl: List[String], urlsCrawled: List[String], currentDepth: Int): List[String] = urls2crawl match {
      case Nil =>  urlsCrawled
      case u :: urls => {

        println(s"checking ${u}")
        if (!urlsCrawled.contains(u)){
          val cnx = Jsoup.connect(u)
          val doc = cnx.get()
          val code = cnx.response().statusCode()

          val links = doc.select("a[href]")
            .stream().map(_.absUrl("href")).toList

          links.forEach( l => rep.write(s"${l},${u}\n"))

          if ( currentDepth > MAX_DEPTH) {
            _crawl(urls, u::urlsCrawled, currentDepth)
          } else {
            val urls2 = links.filter(!urls2crawl.contains(_))
            _crawl(urls2.toList, u :: urlsCrawled, depth + 1)
          }

        } else {
          _crawl(urls, urlsCrawled, depth + 1)
        }
      }
    }

   val completedCrawling =  _crawl(List(targ), List(), 1)
    println(s"finished ${completedCrawling.size} URLs")
  }

  def main(args: Array[String]): Unit = {

    val targetUrl = args(0)
    val context = args(1)
    val reportDir = args(2)

    val reporter = new Reporter(reportDir, context)

    reporter.write("url,referrer")


    crawl(targetUrl, 3, reporter)
    reporter.fileWriter.close()


  }
}