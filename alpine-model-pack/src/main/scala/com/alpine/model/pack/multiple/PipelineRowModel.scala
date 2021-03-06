/*
 * Copyright (c) 2015 Alpine Data Labs
 * All rights reserved.
 */
package com.alpine.model.pack.multiple

import com.alpine.model._
import com.alpine.model.export.pfa.modelconverters.PipelinePFAConverter
import com.alpine.model.export.pfa.{PFAConverter, PFAConvertible}
import com.alpine.model.pack.multiple.sql.{PipelineClassificationSQLTransformer, PipelineClusteringSQLTransformer, PipelineRegressionSQLTransformer, PipelineSQLTransformer}
import com.alpine.plugin.core.io.ColumnDef
import com.alpine.sql.SQLGenerator
import com.alpine.transformer.sql.RegressionSQLTransformer

/**
 * Used to combine models in sequence.
 * e.g.
 *  the output of one model is the input to the next.
 */
case class PipelineRowModel(transformers: Seq[RowModel], override val identifier: String = "")
  extends RowModel with PFAConvertible {

  override def transformer = new PipelineTransformer(transformers.map(t => t.transformer).toList, transformers)

  @transient lazy val outputFeatures: Seq[ColumnDef] = transformers.last.transformationSchema.outputFeatures

  @transient lazy val inputFeatures: Seq[ColumnDef] = transformers.head.transformationSchema.inputFeatures

  @transient override lazy val sqlOutputFeatures: Seq[ColumnDef] = transformers.last.sqlOutputFeatures

  override def classesForLoading = {
    super.classesForLoading ++ transformers.flatMap(t => t.classesForLoading).toSet
  }

  override def sqlTransformer(sqlGenerator: SQLGenerator) = {
    PipelineSQLTransformer.make(this, sqlGenerator)
  }

  override def getPFAConverter: PFAConverter = new PipelinePFAConverter(transformers)
}

/**
  * Used for combining a Regression model (e.g. Linear Regression) with preprocessors (e.g. One Hot Encoding).
  */
case class PipelineRegressionModel(preProcessors: Seq[RowModel], finalModel: RegressionRowModel, override val identifier: String = "")
  extends RegressionRowModel with PFAConvertible {

  override def transformer = {
    new PipelineRegressionTransformer(preProcessors.map(t => t.transformer).toList, finalModel.transformer, preProcessors ++ List(finalModel))
  }

  override def sqlTransformer(sqlGenerator: SQLGenerator): Option[RegressionSQLTransformer] = {
    PipelineRegressionSQLTransformer.make(this, sqlGenerator)
  }

  override def dependentFeature = finalModel.dependentFeature

  override def outputFeatures: Seq[ColumnDef] = finalModel.outputFeatures

  @transient lazy val inputFeatures: Seq[ColumnDef] = preProcessors.head.transformationSchema.inputFeatures

  @transient override lazy val sqlOutputFeatures: Seq[ColumnDef] = finalModel.sqlOutputFeatures

  override def classesForLoading = {
    super.classesForLoading ++ preProcessors.flatMap(t => t.classesForLoading).toSet ++ finalModel.classesForLoading
  }

  override def getPFAConverter: PFAConverter = new PipelinePFAConverter(preProcessors ++ Seq(finalModel))

}
/**
  * Used for combining a Clustering model (e.g. K-Means) with preprocessors (e.g. One Hot Encoding).
  */
case class PipelineClusteringModel(preProcessors: Seq[RowModel], finalModel: ClusteringRowModel, override val identifier: String = "")
  extends ClusteringRowModel with PFAConvertible {

  override def transformer = {
    PipelineClusteringTransformer(preProcessors.map(t => t.transformer).toList, finalModel.transformer, preProcessors ++ List(finalModel))
  }

  override def sqlTransformer(sqlGenerator: SQLGenerator) = {
    PipelineClusteringSQLTransformer.make(this, sqlGenerator)
  }

  override def classLabels = finalModel.classLabels

  @transient lazy val inputFeatures: Seq[ColumnDef] = preProcessors.head.transformationSchema.inputFeatures

  @transient override lazy val sqlOutputFeatures: Seq[ColumnDef] = finalModel.sqlOutputFeatures

  override def outputFeatures = finalModel.outputFeatures

  override def classesForLoading = {
    super.classesForLoading ++ preProcessors.flatMap(t => t.classesForLoading).toSet ++ finalModel.classesForLoading
  }

  override def getPFAConverter: PFAConverter = new PipelinePFAConverter(preProcessors ++ Seq(finalModel))

}

/**
  * Used for combining a Classification model (e.g. Logistic Regression) with preprocessors (e.g. One Hot Encoding).
  */
case class PipelineClassificationModel(preProcessors: Seq[RowModel], finalModel: ClassificationRowModel, override val identifier: String = "")
  extends ClassificationRowModel with PFAConvertible {

  override def transformer = {
    PipelineClassificationTransformer(preProcessors.map(t => t.transformer).toList, finalModel.transformer, preProcessors ++ List(finalModel))
  }

  override def sqlTransformer(sqlGenerator: SQLGenerator) = {
    PipelineClassificationSQLTransformer.make(this, sqlGenerator)
  }

  override def classLabels = finalModel.classLabels

  @transient lazy val inputFeatures: Seq[ColumnDef] = preProcessors.head.transformationSchema.inputFeatures

  @transient override lazy val sqlOutputFeatures: Seq[ColumnDef] = finalModel.sqlOutputFeatures

  override def outputFeatures = finalModel.outputFeatures

  override def classesForLoading = {
    super.classesForLoading ++ preProcessors.flatMap(t => t.classesForLoading).toSet ++ finalModel.classesForLoading
  }

  // Used when we are doing model quality evaluation e.g. Confusion Matrix,
  override def dependentFeature = finalModel.dependentFeature

  override def getPFAConverter: PFAConverter = new PipelinePFAConverter(preProcessors ++ Seq(finalModel))

}

/**
  * Never used by Alpine.
  * Does not have sqlTransformer implemented.
  * Use PipelineClusteringModel or PipelineClassificationModel instead.
  */
@Deprecated
case class PipelineCategoricalModel(preProcessors: Seq[RowModel], finalModel: CategoricalRowModel, override val identifier: String = "")
  extends CategoricalRowModel with PFAConvertible {

  override def transformer = {
    new PipelineCategoricalTransformer(preProcessors.map(t => t.transformer).toList, finalModel.transformer, preProcessors ++ List(finalModel))
  }
  override def classLabels = finalModel.classLabels

  @transient lazy val inputFeatures: Seq[ColumnDef] = preProcessors.head.transformationSchema.inputFeatures

  @transient override lazy val sqlOutputFeatures: Seq[ColumnDef] = finalModel.sqlOutputFeatures

  override def outputFeatures = finalModel.outputFeatures

  override def classesForLoading = {
    super.classesForLoading ++ preProcessors.flatMap(t => t.classesForLoading).toSet ++ finalModel.classesForLoading
  }

  override def getPFAConverter: PFAConverter = new PipelinePFAConverter(preProcessors ++ Seq(finalModel))
}
