package org.example.application

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object Application extends App{

  val config = ConfigFactory.load()
  val sparkConf = new SparkConf()

  import org.apache.log4j.{Level, Logger}

  Logger.getLogger("org").setLevel(Level.ERROR)
  val spark = SparkSession.builder()
    .config(sparkConf)
    .getOrCreate()

  val inputPath = getClass.getResource("/stream.jsonl").getPath
  val jsonDF = spark.read.json(inputPath)

  val kafkaDF = jsonDF
    .selectExpr("CAST(null AS STRING) AS key", "to_json(struct(*)) AS value")

  kafkaDF.write
    .format("kafka")
    .option("kafka.bootstrap.servers", config.getString("kafka.bootstrap-servers"))
    .option("topic", config.getString("kafka.dataTopic"))
    .save()

  spark.stop()
}
