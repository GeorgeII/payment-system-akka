import com.typesafe.config.ConfigFactory

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.Random

object DataGenerator {

  val numberOfFiles = 200
  val maxLinesNumber = 25000

  val maxValue = 1000000
  val maxNameLength = 15

  val config = ConfigFactory.load("application.conf")
  val configField = "payments-folder"

  private val rnd = new Random

  def generateName: String = {
    val lengthOfName = rnd.nextInt(maxNameLength) + 1
    val name = rnd.alphanumeric.take(lengthOfName).mkString

    name
  }

  def generateValue: Int = {
    rnd.nextInt(maxValue) + 1
  }

  def writeFile(iteration: Int): Unit = {
    val numberOfLines = rnd.nextInt(maxLinesNumber) + 1
    val filename = s"${config.getString(configField)}/transactions-$iteration.txt"
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))

    for (line <- 0 until numberOfLines) {
      val paymentFromName = generateName
      val paymentToName   = generateName
      val paymentAmount   = generateValue

      val payment = s"$paymentFromName->$paymentToName:$paymentAmount"
      bw.write(payment)
      bw.newLine()
    }

    bw.close()
  }

  /**
   * Generates 'numberOfFiles' (see object variables above) files with generated payments.
   */
  def main(args: Array[String]): Unit = {
    for (i <- 1 to numberOfFiles) {
      writeFile(i)
    }
  }
}
