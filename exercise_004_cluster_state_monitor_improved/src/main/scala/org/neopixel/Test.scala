package org.neopixel

import java.nio.ByteBuffer
import java.lang.Long

//object Test {
//  def main(args: Array[String]): Unit = {
//    val bb = ByteBuffer.allocate(16)
//    bb.order(java.nio.ByteOrder.nativeOrder())
//    bb.putLong(2773723664L)
//    val ba = bb.array()
//    println(s"ba: ${ba.toList}")
//
//    bb.flip()
//    println(s"bb.getLong: ${bb.getLong}")
//    val bb1 = ByteBuffer.allocate(16)
//    bb1.order(java.nio.ByteOrder.nativeOrder())
////    bb1.put(ba)
//    bb1.put(ba, 8,8)
//    bb1.flip()
//    println(s"bb1: ${bb1.getLong(8)} ")
//  }
//}
