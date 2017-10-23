
package edu.rice.bench
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkException}
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import breeze.linalg._
//import com.github.fommil.netlib.BLAS.{getInstance => nativeBLAS}
//import com.github.fommil.netlib.{BLAS => NetlibBLAS, F2jBLAS}


object MatrixMult {
   def main (args:Array[String]) {
       if (args.length < 5) {
           System.err.println(
               "Usage: MatrixMult <mode> <num_row_dim1> <num_col_dim1> <num_row_dim2> <num_col_dim2>"
           )
           System.exit(1)
       }
       if (args(0)=="BLAS") {
           val num_row_dim1 = args(1).toInt
           val num_col_dim1 = args(2).toInt           
           val lhs = DenseMatrix.rand[Double](num_row_dim1, num_col_dim1)
           println("the first element is %f".format(lhs.apply(0,0)))
           println("the first element is %f".format(lhs.apply(num_row_dim1-1,num_col_dim1-1)))
           val num_row_dim2 = args(3).toInt
           val num_col_dim2 = args(4).toInt
           val rhs = DenseMatrix.rand[Double](num_row_dim2, num_col_dim2)

           //val product = DenseMatrix.zeros[Double](num_row_dim1, num_col_dim2)
           //nativeBLAS.dgemm("N", "N", num_row_dim1, num_col_dim2, num_col_dim1, 1.0, lhs.t.toArray, num_row_dim1, rhs.t.toArray, num_row_dim2, 0.0, product.t.toArray, num_row_dim2);
           val begin = System.currentTimeMillis()
           val product = lhs * rhs
           println("the first element is %f".format(product.apply(0,0)))
           println("the first element is %f".format(product.apply(num_row_dim1-1,num_col_dim2-1)))
           val end = System.currentTimeMillis()
           println("ProcessTime: %f".format((end-begin)/1e3))

       } else {
           println("Type %s not supported".format(args(0)))
           System.exit(1)
       }
   }
} 
