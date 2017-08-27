/**
 * Created by Jia on Jun 11, 2017
 */

package edu.rice.bench

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object ParquetDFSimpleSupervisorAggregation {
    def main (args:Array[String]) {
        if (args.length < 2) {
            System.err.println(
              "Usage: ParquetDFSimpleSupervisorAggregation <INPUT_HDFS> <OUTPUT_HDFS>"
            )
            System.exit(1)
        }
        val spark = SparkSession.builder.appName("ParquetDFSimpleSupervisorAggregation").getOrCreate()
        import spark.implicits._
        val begin1 = System.currentTimeMillis()
        val data = spark.read.load(args(0)).persist
        val begin = System.currentTimeMillis()
        val flatten = udf((xs: Seq[Seq[SimpleEmployee]]) => xs.flatten)
        data.groupBy("department").agg(flatten(collect_list($"employees")).alias("employees")).write.save(args(1))
        val end = System.currentTimeMillis()
        println("LoadTime: %f".format((begin-begin1)/1e3))
        println("ProcessTime: %f".format((end-begin)/1e3))
        spark.stop
    }

}
