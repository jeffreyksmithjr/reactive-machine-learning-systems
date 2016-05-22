package com.reactivemachinelearning

import com.github.nscala_time.time.Imports._

object DataUtils extends App {

  type TransactionId = Long
  type CustomerId = Long
  type MerchantId = Long

  case class Transaction(transactionId: TransactionId,
                         customerId: CustomerId,
                         merchantId: MerchantId,
                         dateTime: DateTime,
                         amount: BigDecimal)

  val TrainingPercent = 80

  def trainingCustomer(id: CustomerId): Boolean = {
    val hashValue = id.hashCode() % 100
    hashValue < TrainingPercent
  }

  val sampleTransaction = Transaction(123, 456, 789, DateTime.now(), 42.01)

  val datasetResult = trainingCustomer(sampleTransaction.customerId)

  println(s"Is the sample transaction in the training set? $datasetResult")

}
