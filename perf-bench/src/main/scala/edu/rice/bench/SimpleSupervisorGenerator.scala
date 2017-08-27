
package edu.rice.bench
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkException}
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.Row

case class SimpleSupervisor (name: String, age: Long, salary: Double, department: String, employees: Seq[SimpleEmployee])

object SimpleSupervisorGenerator {
   def main (args:Array[String]) {
       if (args.length < 3) {
           System.err.println(
               "Usage: SimpleSupervisorGenerator <INPUT_HDFS> <OUTPUT_HDFS> <OUTPUT_TYPE> <Parallelism (optional)>"
           )
           System.exit(1)
       }
       var parallelism = 0;
       if (args.length == 4) {
           parallelism = args(3).toInt;
       }
       if (args(2)=="json") {
           val spark = SparkSession.builder.appName("SimpleSupervisorGenerator").getOrCreate()
           var data = spark.read.json(args(0)).persist
           if (parallelism > 0) {
               data = data.repartition(parallelism)
           }
           println(data.count())
           data.write.format("json").save(args(1))
           spark.stop
       } else if (args(2)=="object") {
           val spark = SparkSession.builder.appName("SimpleSupervisorGenerator").config("spark.serializer", "org.apache.spark.serializer.KryoSerializer").getOrCreate()
           spark.sparkContext.getConf.registerKryoClasses(Array(classOf[edu.rice.bench.SimpleSupervisor], classOf[edu.rice.bench.SimpleEmployee]))
           import spark.implicits._
           var data = spark.read.json(args(0))
           if (parallelism > 0) {
               data = data.repartition(parallelism)
           }
           println(data.count())
           data.persist(StorageLevel.MEMORY_AND_DISK)
           val objects = data.rdd.map( row => SimpleSupervisor(row.getAs[String](3), row.getAs[Long](0), row.getAs[Double](4), row.getAs[String](1), (row.getAs[Seq[Row]](2).map( employee => SimpleEmployee(employee.getAs[String](2), employee.getAs[Long](0), employee.getAs[Double](3), employee.getAs[String](1)))).toSeq))
           println(objects.count())
           objects.saveAsObjectFile(args(1)) 
           spark.stop()       
       } else {
           println("Type %s not supported".format(args(2)))
       }
   }
} 
