/**
 * Created by Jia on Apr 11, 2017
 */

package edu.rice.bench

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkException}
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf


object RDDEmployeeSelection {
    def main (args:Array[String]) {
        if (args.length < 2) {
            System.err.println(
              "Usage: EmployeeSelection <INPUT_HDFS> <OUTPUT_HDFS>"
            )
            System.exit(1)
        }
        val sparkConf = new SparkConf().setAppName("RDDEmployeeSelection")
        val sc = new SparkContext(sparkConf)
        val begin1 = System.currentTimeMillis()
        val data = sc.textFile(args(0))
        data.persist(StorageLevel.MEMORY_AND_DISK)
        //val names = data.map(line => line.split(",")).map(arr => arr(0) ).filter(row => row == "Frank")
        //val frankNames = data.filter(line => line.contains("Frank"))
        //frankNames.saveAsTextFile(args(1))
        val begin = System.currentTimeMillis()
        val names = data.filter(line => line.split(",")(0) == "Frank").map(line => line.split(",")(0))
        //val names = data.filter(line => line.contains("Frank")).map(line => line.split(",")(0))
        val end1 = System.currentTimeMillis()
        names.saveAsTextFile(args(1))
        val end = System.currentTimeMillis()
        println("LoadTime: %f".format((begin-begin1)/1e3))
        println("PureProcessTime: %f".format((end1-begin)/1e3))
        println("ProcessTime: %f".format((end-begin)/1e3))
        sc.stop()
    }

}
