/**
 * COPYRIGHT (C) 2015 Alpine Data Labs Inc. All Rights Reserved.
 */

package com.alpine.plugin.core.io.defaults

import com.alpine.plugin.core.io.{HdfsHtmlDataset, OperatorInfo}

/**
  * Abstract implementation of [[HdfsHtmlDataset]].
  * Can be extended by developers who want custom behaviour not provided by [[HdfsHtmlDatasetDefault]].
  * @param path Path of the file in HDFS.
  * @param sourceOperatorInfo Information about the operator that generated this object as output.
  * @param addendum Map containing additional information.
  */
abstract class AbstractHdfsHtmlDataset(val path: String,
                                       val sourceOperatorInfo: Option[OperatorInfo],
                                       val addendum: Map[String, AnyRef])
  extends HdfsHtmlDataset {
  override def displayName: String = path
}

/**
  * Default implementation of [[HdfsHtmlDataset]].
  * @param path Path of the file in HDFS.
  * @param sourceOperatorInfo Information about the operator that generated this object as output.
  * @param addendum Map containing additional information.
  */
case class HdfsHtmlDatasetDefault(override val path: String,
                                  override val sourceOperatorInfo: Option[OperatorInfo],
                                  override val addendum: Map[String, AnyRef] = Map[String, AnyRef]())
  extends AbstractHdfsHtmlDataset(path, sourceOperatorInfo, addendum)
