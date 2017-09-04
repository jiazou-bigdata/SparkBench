/**
 * Created by Jia on Aug 27, 2017
 */

package edu.rice.bench

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.ml.clustering.KMeans

object ParquetDFKMeans {
    def main (args:Array[String]) {
        if (args.length < 4) {
            System.err.println(
              "Usage: ParquetDFKMeans <INPUT_HDFS> <OUTPUT_HDFS> <K> <NUM_ITERATIONS>"
            )
            System.exit(1)
        }
        val spark = SparkSession.builder.appName("ParquetDFKMeans").getOrCreate()
        import spark.implicits._
        val begin1 = System.currentTimeMillis()
        //below reads from the parquet file we generated by using KMeansGenData
        val data = spark.read.load(args(0)).persist
        //data.printSchema
        //data.show
        val begin = System.currentTimeMillis()
        val kmeans = new KMeans().setInitMode("random").setK(args(2).toInt).setMaxIter(args(3).toInt)
        val model = kmeans.fit(data)

        //val WSSSE = model.computeCost(data)
        //println(s"Within Set Sum of Squared Errors = $WSSSE")

        // Shows the result.
        println("Cluster Centers: ")
        model.clusterCenters.foreach(println)

        val end = System.currentTimeMillis()
        //due to pipelining optimization, below output maybe incorrect
        println("LoadTime: %f".format((begin-begin1)/1e3))

        //due to pipelining optimization, below output maybe incorrect
        println("ProcessTime: %f".format((end-begin)/1e3))
        spark.stop
    }

}
