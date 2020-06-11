/*
 * Copyright 2020 Precog Data
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package quasar.physical.blobstore.azure

import slamdata.Predef._
import quasar.blobstore.azure._
import quasar.connector.DataFormat

final case class AzureConfig(
    override val containerName: ContainerName,
    override val credentials: Option[AzureCredentials.SharedKey],
    override val storageUrl: StorageUrl,
    override val maxQueueSize: Option[MaxQueueSize],
    format: DataFormat) extends Config {

  def sanitize: AzureConfig = copy(
    credentials = credentials.map(_ => AzureConfig.RedactedCreds))

  def isSensitive: Boolean = credentials match {
    case None => false
    case Some(cs) => !(cs.accountName.value.isEmpty && cs.accountKey.value.isEmpty)
  }

  def reconfigureNonSensitive(patch: AzureConfig): Either[AzureConfig, AzureConfig] =
    if (patch.isSensitive)
      Left(patch.sanitize)
    else
      Right(copy(
        containerName = patch.containerName,
        storageUrl = patch.storageUrl,
        maxQueueSize = patch.maxQueueSize,
        format = patch.format))
}

object AzureConfig {
  val RedactedCreds =
    AzureCredentials.SharedKey(
      AccountName("<REDACTED>"),
      AccountKey("<REDACTED>"))

}
