/**
 * Created by Jia on Aug 27, 2017
 */

package edu.rice.bench

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkException}
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors

object ObjectRDDKMeans {
    def main (args:Array[String]) {
        if (args.length < 4) {
            System.err.println(
              "Usage: ObjectRDDKMeans <INPUT_HDFS> <OUTPUT_HDFS> <K> <NUM_ITERATION>"
            )
            System.exit(1)
        }
        val sparkConf = new SparkConf().setAppName("ObjectRDDKMeans")
        val sc = new SparkContext(sparkConf)
        val begin1 = System.currentTimeMillis()
        val data = sc.objectFile[org.apache.spark.mllib.linalg.Vector](args(0))
        data.persist(StorageLevel.MEMORY_AND_DISK)
        val begin = System.currentTimeMillis()
        val model = new KMeans().setInitializationMode ("random")
                                .setK(args(2).toInt)
                                .setMaxIterations(args(3).toInt)
                                .run(data)

        //val cost = model.computeCost(data)
        //println(s"Total cost = $cost.")
        // Shows the result.
        println("Cluster Centers: ")
        model.clusterCenters.foreach(println)
        val end = System.currentTimeMillis()
        //due to Spark pipelining optimization, below output may not be correct.
        println("LoadTime: %f".format((begin-begin1)/1e3))
        println("ProcessTime: %f".format((end-begin)/1e3))
        sc.stop()
    }

}
