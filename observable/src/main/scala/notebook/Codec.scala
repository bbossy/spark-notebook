/*
 * Copyright (c) 2013  Bridgewater Associates, LP
 *
 * Distributed under the terms of the Modified BSD License.  The full license is in
 * the file COPYING, distributed as part of this software.
 */
package notebook

import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.slf4j.LoggerFactory

import notebook.util._

/**
 * Author: Ken
 */


trait Codec[A,B] {
  def encode(x:A):B
  def decode(x:B):A
}

object JsonCodec {
  val log = LoggerFactory.getLogger(getClass())
  implicit val formats = DefaultFormats

//  implicit val ints = new Codec[JValue, Int] {
//    def decode(t: Int) = JInt(t)
//    def encode(v: JValue):Int = v.extract[Int]
//  }
  implicit val ints = new Codec[JDouble, Int] {
    def decode(t: Int) = JDouble(t.toDouble)
    def encode(v: JDouble):Int = v.extract[Double].toInt
  }
  implicit val doubles = new Codec[JValue, Double] {
    def decode(t: Double) = JDouble(t)
    def encode(v: JValue):Double = v.extract[Double]
  }
  implicit val strings = new Codec[JValue, String] {
    def decode(t: String):JValue = JString(t)
    def encode(v: JValue) = v.extract[String]
  }

  implicit val pair = new Codec[JValue, (Double,Double)] {
    def decode(t: (Double,Double)): JValue = Seq(t._1, t._2)
    def encode(v: JValue) = {
      val JArray(Seq(JDouble(x),JDouble(y))) = v
      (x, y)
    }
  }

  implicit def tMap[T](implicit codec:Codec[JValue, T]):Codec[JValue, Map[String, T]] = new Codec[JValue, Map[String, T]] {
    def encode(vs: JValue):Map[String, T] = {
      val JObject(xs) = vs
      xs.map { case JField(name, value) =>
        name -> codec.encode(value)
      }.toMap
    }
    def decode(ts: Map[String, T]): JValue = JObject( ts.map{ case (n, t) => JField(n, codec.decode(t))}.toList )
  }

  implicit def tSeq[T](implicit codec:Codec[JValue, T]):Codec[JValue, Seq[T]] = new Codec[JValue, Seq[T]] {
    def encode(vs: JValue) = for (JArray(Seq(t)) <- vs) yield codec.encode(t)
    def decode(ts: Seq[T]): JValue = ts.map(t => codec.decode(t))
  }

  implicit def idCodec[T]:Codec[T, T] = new Codec[T, T] {
    def encode(x: T): T = x
    def decode(x: T): T = x
  }


  implicit def jdouble2jvalueCodec[T](codec:Codec[JDouble, T]):Codec[JValue, T] = new Codec[JValue, T] {
    def encode(x:JValue):T = codec.encode(x)
    def decode(x:T):JValue = codec.decode(x)
  }

}
