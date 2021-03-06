/*
 * COPYRIGHT (C) 2015 Alpine Data Labs Inc. All Rights Reserved.
 */

package com.alpine.common.serialization.json

import com.google.gson.Gson


object JsonTestUtil {

  def gsonsToTest: Seq[Gson] = {
    Seq(
      JsonUtil.simpleGsonBuilder().create(),
      JsonUtil.simpleGsonBuilder().setPrettyPrinting().create()
    )
  }
  def testJsonization(p: Any, printJson: Boolean = false): Unit = {
    gsonsToTest.foreach(g => testJsonization(p, g, printJson))
  }

  def testJsonization(p: Any, gson: Gson, printJson: Boolean): Unit = {
    val json: String = gson.toJson(p)
    if (printJson) {
      println("Json is:")
      println(json)
    }

    val deserialized = gson.fromJson(json, p.getClass)
    assert(p == deserialized)
  }

  def testDeserialization(json: String, expected: Any): Unit = {
    gsonsToTest.foreach(g => {
      val deserialized = g.fromJson(json, expected.getClass)
      assert(expected == deserialized)
    })
  }

}
