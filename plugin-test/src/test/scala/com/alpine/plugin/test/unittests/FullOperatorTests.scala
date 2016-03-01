package com.alpine.plugin.test.unittests

import com.alpine.plugin.core.datasource.OperatorDataSourceManager
import com.alpine.plugin.core.dialog.{ColumnFilter, OperatorDialog}
import com.alpine.plugin.core.io.OperatorSchemaManager
import com.alpine.plugin.core.spark.templates.{SparkDataFrameGUINode, SparkDataFrameJob}
import com.alpine.plugin.core.spark.utils.SparkRuntimeUtils
import com.alpine.plugin.core.{OperatorListener, OperatorParameters}
import com.alpine.plugin.test.mock.OperatorParametersMock
import com.alpine.plugin.test.utils.{TestSparkContexts, OperatorParameterMockUtil, SimpleAbstractSparkJobSuite}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row}


class FullOperatorTests extends  SimpleAbstractSparkJobSuite {

  import TestSparkContexts._

  class TestGui extends SparkDataFrameGUINode[TestSparkJob] {

    override def onPlacement(operatorDialog: OperatorDialog,
                             operatorDataSourceManager: OperatorDataSourceManager,
                             operatorSchemaManager: OperatorSchemaManager): Unit = {

      operatorDialog.addCheckboxes(
        id = "addCheckboxes",
        label = "addCheckboxes param",
        values = Seq(
          "red fish",
          "blue fish",
          "one fish",
          "two fish"
        ),
        defaultSelections = Seq("blue fish")
      )

      operatorDialog.addTabularDatasetColumnCheckboxes(
        id = "addTabularDatasetColumnCheckboxes",
        label = "addTabularDatasetColumnCheckboxes Param ",
        columnFilter = ColumnFilter.All,
        selectionGroupId = "main"
      )

      operatorDialog.addTabularDatasetColumnDropdownBox(
        id = "addTabularDatasetColumnDropdownBox",
        label = "addTabularDatasetColumnDropdownBox param",
        columnFilter = ColumnFilter.All,
        selectionGroupId = "other"
      )

      operatorDialog.addIntegerBox("addIntegerBox", " addIntegerBox param ", 0, 100, 50)

      operatorDialog.addStringBox("addStringBox", "addStringBox Param", "aaa", "a*", 0, 0)

      operatorDialog.addDoubleBox("addDoubleBox", "addDoubleBox param", 0.0, 1.0,
        inclusiveMin = true, inclusiveMax = true, 0.5)

      super.onPlacement(operatorDialog, operatorDataSourceManager, operatorSchemaManager)
    }
  }

  class TestSparkJob extends SparkDataFrameJob {

    override def transform(operatorParameters: OperatorParameters, dataFrame: DataFrame,
                           sparkUtils: SparkRuntimeUtils, listener: OperatorListener): DataFrame = {
      val col: String = operatorParameters.getTabularDatasetSelectedColumn("addTabularDatasetColumnDropdownBox")._2
      val col2: Array[String] = operatorParameters.getTabularDatasetSelectedColumns("addTabularDatasetColumnCheckboxes")._2

      val checkboxes: Array[String] = operatorParameters.getStringArrayValue("addCheckboxes")
      assert(checkboxes.sameElements(Seq("blue fish")))
      val stringParam = operatorParameters.getStringValue("addStringBox")
      assert(stringParam == "aaa")
      val intParam = operatorParameters.getIntValue("addIntegerBox")
      assert(intParam == 50)
      val doubleParam = operatorParameters.getDoubleValue("addDoubleBox")
      assert(doubleParam == 0.5)
      dataFrame.select(col, col2 : _ *)
    }
  }

  test("Check default values") {
    val uuid = "1"
    val colFilterName = "TestColumnSelector"

    val inputRows = sc.parallelize(List(Row("Masha", 22), Row("Ulia", 21), Row("Nastya", 23)))
    val inputSchema =
      StructType(List(StructField("name", StringType), StructField("age", IntegerType)))
    val dataFrameInput = sqlContext.createDataFrame(inputRows, inputSchema)

    val parameters = new OperatorParametersMock(colFilterName, uuid)
    OperatorParameterMockUtil.addTabularColumn(parameters, "addTabularDatasetColumnDropdownBox", "name")
    OperatorParameterMockUtil.addTabularColumns(parameters, "addTabularDatasetColumnCheckboxes", "name", "age")
    OperatorParameterMockUtil.addHdfsParams(parameters, "ColumnSelector")
    val operatorGUI = new TestGui
    val operatorJob = new TestSparkJob
    val (r, _) = runDataFrameThroughEntireDFTemplate(operatorGUI, operatorJob, parameters, dataFrameInput)
    assert(r.collect().nonEmpty)

  }

}