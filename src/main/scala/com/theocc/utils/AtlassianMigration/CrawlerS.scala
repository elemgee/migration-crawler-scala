package com.theocc.utils.AtlassianMigration

import org.apache.commons.csv.{CSVFormat, CSVParser, CSVPrinter, CSVRecord}
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.jsoup.{HttpStatusException, Jsoup}

import java.io.{File, FileReader, FileWriter}
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`


object CrawlerS {

  val logger: Logger = LoggerFactory.getLogger(CrawlerS.getClass)

  val HEADER_AUTH = "Authorization"
  val URL_HEADER = "url"
  val REF_HEADER = "referrer"
  val TITLE_HEADER = "title"
  val STATUS_HEADER = "statusCode"


  private val TSFMT = new SimpleDateFormat("yyyyMMddHHmmss")

  val MAX_DEPTH = 3;


  def crawl(targ: String, bearerToken: String, depth: Int = 3, rep: CSVPrinter): Unit = {


    def _crawl(urls2crawl: List[String], urlsCrawled: List[String], currentDepth: Int): List[String] = urls2crawl match {
      case Nil => urlsCrawled
      case u :: urls => {

        logger.info(s"checking ${u}")
        if (!urlsCrawled.contains(u)) {
          val cnx = Jsoup.connect(u)
          //cnx.header(HEADER_AUTH, s"Bearer ${bearerToken}")
          val doc = cnx.get()

          val statusCode = cnx.response().statusCode()

          logger.debug(s"Status Code for ${u}: ${statusCode}")
          statusCode match {
            case 200 => {

              val links = doc.select("a[href]")
                .stream().map(_.absUrl("href")).toList

              links.foreach(l => rep.printRecord(l, u, doc.title(), statusCode))
              if (currentDepth > MAX_DEPTH) {
                _crawl(urls, u :: urlsCrawled, currentDepth)
              } else {
                val urls2 = links.filter(!urls2crawl.contains(_))
                _crawl(urls2.toList, u :: urlsCrawled, depth)
              }
            }
            case _ => {
              rep.printRecord("", u, "", statusCode)
              _crawl(urls, u :: urlsCrawled, depth + 1)
            }
          }


        } else {
          _crawl(urls, urlsCrawled, depth + 1)
        }
      }
    }

    val completedCrawling = _crawl(List(targ), List(), 1)
    logger.info(s"finished ${completedCrawling.size} URLs")
  }

  def summarize(rep2summarize: String, linkSummaryReport: String, errorReportfilename: String) = {
    val linkStatusReport = new CSVPrinter(new FileWriter(linkSummaryReport), CSVFormat.EXCEL)
    linkStatusReport.printRecord("link", "statusCode", "title", "found in")

    val errorReport = new CSVPrinter(new FileWriter(errorReportfilename), CSVFormat.EXCEL)
    errorReport.printRecord("url", "statusCode", "found in")

    val records = CSVFormat.EXCEL.parse(new FileReader(rep2summarize)).getRecords.toList
    records.groupBy(r => r.get(0))
      .map(r => {
        println(s"${r._1}: ${r._2.size}")
        var url = new URL(r._1)
        val referrers = r._2.groupBy(refs => refs.get(2))
          .map(refs => s"${refs._1} (${refs._2.head.get(1)})" )
          .toList
          .mkString("\n")

        url.getProtocol match {
          case "mailto" => linkStatusReport.printRecord(r._1, "", "", referrers)
          case _ => {


            // check if Nasdaq url... if so, use auth token
            val cnx = url.getHost match {
              case "customer-support.nasdaq.com" => Jsoup.connect(r._1) // get bearer auth
              case _ => Jsoup.connect(r._1)
            }
            val response = cnx.execute()
            try {
              linkStatusReport.printRecord(r._1, response.statusCode(), cnx.get().title(), referrers)
            } catch {
              case httpx: org.jsoup.HttpStatusException => errorReport.printRecord(r._1, response.statusCode(), "", referrers)
            }
          }
        }

      })


  }

  def bearerTok(bearerFile: File) = scala.io.Source.fromFile(bearerFile).mkString

  def main(args: Array[String]): Unit = {

    val targetUrl = args(0)
    val context = args(1)
    val reportDir = args(3)
    val tokFile = args(2)
    val ts: String = TSFMT.format(new Date)

    val crawlReportFilename: String = s"$reportDir/$context-crawl-$ts.csv"
    val crawlReport = new CSVPrinter(new FileWriter(crawlReportFilename), CSVFormat.EXCEL)
    val linkStatusReportFilename: String = s"$reportDir/$context-linkstat-$ts.csv"
    val errorReportFilename: String = s"$reportDir/$context-errors-$ts.csv"


    crawlReport.printRecord(URL_HEADER, REF_HEADER, TITLE_HEADER, STATUS_HEADER)

    crawl(targetUrl, "test", 3, crawlReport)
    crawlReport.close()

    summarize(crawlReportFilename, linkStatusReportFilename, errorReportFilename)

  }
}