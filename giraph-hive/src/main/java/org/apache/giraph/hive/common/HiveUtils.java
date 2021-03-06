/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.giraph.hive.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;

import com.facebook.hiveio.input.HiveApiInputFormat;
import com.facebook.hiveio.input.HiveInputDescription;
import com.facebook.hiveio.output.HiveApiOutputFormat;
import com.facebook.hiveio.output.HiveOutputDescription;
import com.facebook.hiveio.schema.HiveTableSchemas;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Utility methods for Hive IO
 */
public class HiveUtils {
  /** Do not instantiate */
  private HiveUtils() {
  }

  /**
   * Initialize hive input, prepare Configuration parameters
   *
   * @param hiveInputFormat HiveApiInputFormat
   * @param inputDescription HiveInputDescription
   * @param profileId profile ID
   * @param conf Configuration
   */
  public static void initializeHiveInput(HiveApiInputFormat hiveInputFormat,
      HiveInputDescription inputDescription, String profileId,
      Configuration conf) {
    hiveInputFormat.setMyProfileId(profileId);
    HiveApiInputFormat.setProfileInputDesc(conf, inputDescription, profileId);
    HiveTableSchemas.put(conf, profileId, inputDescription.hiveTableName());
  }

  /**
   * Initialize hive output, prepare Configuration parameters
   *
   * @param hiveOutputFormat HiveApiOutputFormat
   * @param profileId Profile id
   * @param dbName Database name
   * @param tableName Table name
   * @param partition Partition
   * @param conf Configuration
   */
  public static void initializeHiveOutput(HiveApiOutputFormat hiveOutputFormat,
      String profileId, String dbName, String tableName, String partition,
      Configuration conf) {
    hiveOutputFormat.setMyProfileId(profileId);
    HiveOutputDescription outputDescription = new HiveOutputDescription();
    outputDescription.setDbName(dbName);
    outputDescription.setTableName(tableName);
    outputDescription.setPartitionValues(parsePartitionValues(partition));
    try {
      HiveApiOutputFormat.initProfile(conf, outputDescription, profileId);
    } catch (TException e) {
      throw new IllegalStateException(
          "initializeHiveOutput: TException occurred", e);
    }
    HiveTableSchemas.put(conf, profileId, outputDescription.hiveTableName());
  }

  /**
   * @param outputTablePartitionString table partition string
   * @return Map
   */
  public static Map<String, String> parsePartitionValues(
      String outputTablePartitionString) {
    if (outputTablePartitionString == null) {
      return null;
    }
    Splitter commaSplitter = Splitter.on(',').omitEmptyStrings().trimResults();
    Splitter equalSplitter = Splitter.on('=').omitEmptyStrings().trimResults();
    Map<String, String> partitionValues = Maps.newHashMap();
    for (String keyValStr : commaSplitter.split(outputTablePartitionString)) {
      List<String> keyVal = Lists.newArrayList(equalSplitter.split(keyValStr));
      if (keyVal.size() != 2) {
        throw new IllegalArgumentException(
            "Unrecognized partition value format: " +
                outputTablePartitionString);
      }
      partitionValues.put(keyVal.get(0), keyVal.get(1));
    }
    return partitionValues;
  }
}
