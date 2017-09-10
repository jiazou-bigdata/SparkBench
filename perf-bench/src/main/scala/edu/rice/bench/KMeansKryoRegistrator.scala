
/**
 * Created by Jia on Aug 27, 2017
 */


package edu.rice.bench

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.clustering
import org.apache.spark.serializer.KryoRegistrator
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import scala.collection.mutable.ArrayBuffer


class KMeansKryoRegistrator extends KryoRegistrator{
  override def registerClasses(kryo: Kryo){
    kryo.register(classOf[Vector])
  }
}
