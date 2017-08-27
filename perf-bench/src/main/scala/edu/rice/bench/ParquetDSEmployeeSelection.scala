/**
 * Created by Jia on Apr 11, 2017
 */

package edu.rice.bench

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object ParquetDSEmployeeSelection {
    def main (args:Array[String]) {
        if (args.length < 2) {
            System.err.println(
              "Usage: EmployeeSelection <INPUT_HDFS> <OUTPUT_HDFS>"
            )
            System.exit(1)
        }
        val spark = SparkSession.builder.appName("ParquetDSEmployeeSelection").getOrCreate()
        import spark.implicits._
        val begin1 = System.currentTimeMillis()
        val data = spark.read.parquet(args(0)).as[Employee].persist
        //data.printSchema
        //data.show
        //data.filter($"name".like("Frank")).select("name").write.save(args(1))
        //data.filter($"name".like("Frank")).show
        //data.filter($"name".like("Frank")).select("name").show
        val begin = System.currentTimeMillis()
        data.filter(_.name=="Frank").map(_.name).write.save(args(1))
        val end = System.currentTimeMillis()
        println("LoadTime: %f".format((begin-begin1)/1e3))
        println("ProcessTime: %f".format((end-begin)/1e3))
        spark.stop
    }

}
