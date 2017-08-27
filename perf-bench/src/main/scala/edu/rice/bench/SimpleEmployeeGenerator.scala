
package edu.rice.bench
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkException}
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

case class SimpleEmployee (name: String, age: Long, salary: Double, department: String)

object SimpleEmployeeGenerator {
   def main (args:Array[String]) {
       if (args.length < 3) {
           System.err.println(
               "Usage: SimpleEmployeeGenerator <INPUT_HDFS> <OUTPUT_HDFS> <OUTPUT_TYPE> <Parallelism (optional)>"
           )
           System.exit(1)
       }
       var parallelism = 0;
       if (args.length == 4) {
           parallelism = args(3).toInt;
       }
       if (args(2)=="parquet") {
           val spark = SparkSession.builder.appName("SimpleEmployeeGenerator").getOrCreate()
           var data = spark.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load(args(0)).persist
           if (parallelism > 0) {
               data = data.repartition(parallelism)
           }
           println(data.count())
           data.write.format("parquet").save(args(1))
           spark.stop
       } else if (args(2)=="object") {
           val sparkConf = new SparkConf().setAppName("SimpleEmployeeGenerator")
           val sc = new SparkContext(sparkConf)
           var data = sc.textFile(args(0))
           if (parallelism > 0) {
               data = data.repartition(parallelism)
           }
           println(data.count())
           data.persist(StorageLevel.MEMORY_AND_DISK)
           val objectsMap = data.filter(x => (x.contains("name,")==false)).map(x => x.split(","))
           val objects = objectsMap.map( p => SimpleEmployee(p(0), p(1).toLong, p(2).toDouble, p(3)))
           println(objects.count())
           objects.saveAsObjectFile(args(1)) 
           sc.stop()       
       } else if (args(2)=="removeHeader") {
           val sparkConf = new SparkConf().setAppName("SimpleEmployeeGenerator")
           val sc = new SparkContext(sparkConf)
           val data = sc.textFile(args(0))
           println(data.count())
           val newData = data.filter(x => (x.contains("name,")==false))
           println(newData.count())
           newData.saveAsTextFile(args(1))
           sc.stop()
       } else {
           println("Type %s not supported".format(args(2)))
       }
   }
} 
