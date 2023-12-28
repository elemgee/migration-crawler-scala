import java.io.{File,FileWriter}
import java.text.SimpleDateFormat
import java.util.Date

//import org.apache.commons.csv.CSVFormat
//import org.apache.commons.csv.CSVPrinter

class Reporter(reportDir: String, context: String) {
  private val dateformat = new SimpleDateFormat("yyyyMMddHHmmss")
  val ts: String = dateformat.format(new Date)
  private var referfw: FileWriter = null

//  private var refercsv: CSVPrinter = null

  val refFname: String = s"$reportDir/$context-referrer-$ts.csv"
  val fileWriter = new FileWriter(new File(refFname), true)


  def write(line: String): Unit = {
    fileWriter.write(line)
  }

}
