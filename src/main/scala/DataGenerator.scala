import com.typesafe.config.ConfigFactory

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.Random

object DataGenerator {

  val numberOfFiles = 200
  val maxLinesNumber = 25000
  val probabilityOfValidGeneration = 0.85

  val maxValue = 1000000
  val maxNameLength = 15

  val config = ConfigFactory.load("application.conf")
  val configField = "payments-folder"

  private val rnd = new Random

  def generateValidName: String = {
    val lengthOfName = rnd.nextInt(maxNameLength) + 1
    val name = rnd.alphanumeric.take(lengthOfName).mkString

    name
  }

  def generateValidValue: Int = {
    rnd.nextInt(maxValue) + 1
  }

  /**
   * Even though the method does not guarantee to return an invalid name, it is highly likely
   * to happen.
   * @return String that contains ASCII characters from range 33-126.
   */
  def generateLikelyInvalidName: String = {
    val lengthOfName = rnd.nextInt(maxNameLength) + 1
    val name = (0 to lengthOfName).map(_ => rnd.nextPrintableChar).mkString

    name
  }

  /**
   * There's still a chance to generate a valid value. But it's very unlikely to happen.
   * @return String of length 7 that contains ASCII chars from range 33-126.
   */
  def generateLikelyInvalidValue: String = {
    rnd.alphanumeric.take(7).mkString
  }

  def generateValidPayment: String = {
    val paymentFromName = generateValidName
    val paymentToName   = generateValidName
    val paymentValue    = generateValidValue

    s"$paymentFromName->$paymentToName:$paymentValue"
  }

  def generateLikelyInvalidPayment: String = {
    val paymentFromName = generateLikelyInvalidName
    val paymentToName   = generateLikelyInvalidName
    val paymentValue    = generateLikelyInvalidValue

    s"$paymentFromName->$paymentToName:$paymentValue"
  }

  def writeFile(iteration: Int): Unit = {
    val numberOfLines = rnd.nextInt(maxLinesNumber) + 1
    val filename = s"${config.getString(configField)}/transactions-$iteration.txt"
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))

    for (line <- 0 until numberOfLines) {
      // generate valid or invalid payment based on a given probability.
      val isGenerateValid = rnd.nextDouble < probabilityOfValidGeneration
      val payment = if (isGenerateValid) generateValidPayment else generateLikelyInvalidPayment

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
