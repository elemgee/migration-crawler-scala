package com.theocc.utils.AtlassianMigration

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.{File, FileReader, FileWriter}
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class Reporter(reportDir: String, context: String) {
  private val dateformat = new SimpleDateFormat("yyyyMMddHHmmss")
  val ts: String = dateformat.format(new Date)

//  private var refercsv: CSVPrinter = null

  val refFname: String = s"$reportDir/$context-referrer-$ts.csv"
  val csvname: String = s"$reportDir/$context-apache-referrer-$ts.csv"
  val fileWriter = new FileWriter(new File(refFname), true)
   val csvp = new CSVPrinter(new FileWriter(csvname), CSVFormat.EXCEL)


  def write(line: String): Unit = {
    fileWriter.write(line)
  }

  def summarize() = {
    val reader = new FileReader(csvname)

  }

}

object Reporter {
val logger:Logger = LoggerFactory.getLogger(Reporter.getClass)
}
