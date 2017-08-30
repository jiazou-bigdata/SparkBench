
/**
 * Created by Jia on Aug 27, 2017
 */


package edu.rice.bench
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkException}
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors

object KMeansDataGenerator {
   def main (args:Array[String]) {
       if (args.length < 3) {
           System.err.println(
               "Usage: KMeansDataGenerator <INPUT_HDFS> <OUTPUT_HDFS> <OUTPUT_TYPE> <Parallelism (optional)>"
           )
           System.exit(1)
       }
       var parallelism = 0;
       if (args.length == 4) {
           parallelism = args(3).toInt;
       }
       if (args(2)=="libsvm-parquet") {
           val spark = SparkSession.builder.appName("KMeansDataGenerator").getOrCreate()
           var data = spark.read.format("libsvm").load(args(0)).persist
           if (parallelism > 0) {
               data = data.repartition(parallelism)
           }
           println(data.count())
           data.write.format("parquet").save(args(1))
           spark.stop
       } else if (args(2)=="object") {
           val sparkConf = new SparkConf().setAppName("KMeansDataGenerator")
           val sc = new SparkContext(sparkConf)
           var data = sc.textFile(args(0))
           if (parallelism > 0) {
               data = data.repartition(parallelism)
           }
           println(data.count())
           data.persist(StorageLevel.MEMORY_AND_DISK)
           val objects =data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()
           println(objects.count())
           objects.saveAsObjectFile(args(1)) 
           sc.stop()       
       } else if (args(2) == "parquet") {
           val spark = SparkSession.builder.appName("KMeansDataGenerator").getOrCreate()
           import spark.implicits._
           var data = spark.read.text(args(0)).as[Array[Double]]
           if (parallelism > 0) {
               data = data.repartition(parallelism)
           }
           println(data.count())
           data.write.format("parquet").save(args(1))
           spark.stop

       } else {
           println("Type %s not supported".format(args(2)))
       }
   }
} 
