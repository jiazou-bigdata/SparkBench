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

object ObjectRDDSimpleSupervisorAggregation {

    def groupEmployees = (accum: Seq[SimpleEmployee], rhs: Seq[SimpleEmployee]) => accum ++ rhs
        

    def main (args:Array[String]) {
        if (args.length < 2) {
            System.err.println(
              "Usage: ObjectRDDSimpleSupervisorAggregation <INPUT_HDFS> <OUTPUT_HDFS>"
            )
            System.exit(1)
        }
        val sparkConf = new SparkConf().setAppName("ObjectRDDSimpleSupervisorAggregation")
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        sparkConf.registerKryoClasses(Array(classOf[edu.rice.bench.SimpleEmployee], classOf[edu.rice.bench.SimpleSupervisor]))
        val sc = new SparkContext(sparkConf)
        val begin1 = System.currentTimeMillis()
        val data = sc.objectFile[SimpleSupervisor](args(0))
        data.persist(StorageLevel.MEMORY_AND_DISK)
        val begin = System.currentTimeMillis()
        val employees = data.map(x => (x.department, x.employees)).aggregateByKey(Seq.empty[SimpleEmployee])(groupEmployees, groupEmployees)
        //println(employees.count())
        val end1 = System.currentTimeMillis()
        employees.saveAsObjectFile(args(1))
        val end = System.currentTimeMillis()
        println("LoadTime: %f".format((begin-begin1)/1e3))
        println("PureProcessTime: %f".format((end1-begin)/1e3))
        println("ProcessTime: %f".format((end-begin)/1e3))
        sc.stop()
    }

}
