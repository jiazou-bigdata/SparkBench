
/**
 * Created by Jia on Aug 27, 2017
 */


package edu.rice.bench

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.serializer.KryoRegistrator


class KMeansKryoRegistrator extends KryoRegistrator{
  override def registerClasses(kryo: Kryo){
    kryo.register(classOf[Vector])
  }
}
