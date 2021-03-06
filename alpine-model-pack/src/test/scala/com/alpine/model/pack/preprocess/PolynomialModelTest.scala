/*
 * Copyright (c) 2015 Alpine Data Labs
 * All rights reserved.
 */
package com.alpine.model.pack.preprocess

import com.alpine.json.JsonTestUtil
import com.alpine.plugin.core.io.{ColumnDef, ColumnType}
import com.alpine.transformer.sql.{ColumnName, ColumnarSQLExpression, LayeredSQLExpressions}
import com.alpine.util.SimpleSQLGenerator
import org.scalatest.FunSuite

/**
 * Tests serialization of PolynomialModel
 * and application of PolynomialTransformer.
 */
class PolynomialModelTest extends FunSuite {

  val exponents = Seq(Seq[java.lang.Double](1.0,2.0,0.0), Seq[java.lang.Double](0.5,3.0,2.0))
  val inputFeatures = {
    Seq(new ColumnDef("x1", ColumnType.Double), new ColumnDef("x2", ColumnType.Double), new ColumnDef("x3", ColumnType.Double))
  }

  val t = new PolynomialModel(exponents, inputFeatures)

  test("Should serialize correctly") {
    JsonTestUtil.testJsonization(t)
  }

  test("Should score correctly") {
    assert(Seq(1d, 1d) === t.transformer.apply(Seq(1,1,1)))
    assert(Seq(1d, 0d) === t.transformer.apply(Seq(1,1,0)))
    assert(Seq(1 * 4d, 1 * 8 * 9d) === t.transformer.apply(Seq(1,2,3)))
    assert(Seq(4 * 1d, 2 * 1 * 2.25) === t.transformer.apply(Seq(4,1,1.5)))
  }

  test("Should generate correct SQL") {
    val sqlTransformer = t.sqlTransformer(new SimpleSQLGenerator).get
    val sql = sqlTransformer.getSQL
    val expected = LayeredSQLExpressions(Seq(Seq(
      (ColumnarSQLExpression("\"x1\" * POWER(\"x2\", 2.0)"), ColumnName("y_0")),
      (ColumnarSQLExpression("POWER(\"x1\", 0.5) * POWER(\"x2\", 3.0) * POWER(\"x3\", 2.0)"), ColumnName("y_1"))
    )))
    assert(expected === sql)
  }

  test("Should return 1 if all exponents are 0") {
    val emptyExponents = Seq(Seq[java.lang.Double](0.0,0.0,0.0), Seq[java.lang.Double](0.0,3.0,2.0))
    val model: PolynomialModel = new PolynomialModel(emptyExponents, inputFeatures)

    val sqlTransformer = model.sqlTransformer(new SimpleSQLGenerator).get
    val sql = sqlTransformer.getSQL
    val expected = LayeredSQLExpressions(Seq(Seq(
      (ColumnarSQLExpression("1"), ColumnName("y_0")),
      (ColumnarSQLExpression("POWER(\"x2\", 3.0) * POWER(\"x3\", 2.0)"), ColumnName("y_1"))
    )))
    assert(expected === sql)

    assert(Seq(1d, 1d) === model.transformer.apply(Seq(1,1,1)))
    assert(Seq(1d, 0d) === model.transformer.apply(Seq(1,1,0)))
    assert(Seq(1, 8 * 9d) === model.transformer.apply(Seq(1,2,3)))
    assert(Seq(1, 1 * 2.25) === model.transformer.apply(Seq(4,1,1.5)))
  }

}
