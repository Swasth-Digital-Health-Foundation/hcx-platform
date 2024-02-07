package org.swasth.dp.core.util

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.environment.{CheckpointConfig, StreamExecutionEnvironment}
import org.swasth.dp.core.job.BaseJobConfig

object FlinkUtil {

  def getExecutionContext(config: BaseJobConfig): StreamExecutionEnvironment = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setUseSnapshotCompression(config.enableCompressedCheckpointing)
    env.enableCheckpointing(config.checkpointingInterval)

    /**
      * Use Blob storage as distributed state backend if enabled
      */
    config.enableDistributedCheckpointing match {
      case Some(true) => {
        env.setStateBackend(new HashMapStateBackend())
        val checkpointConfig: CheckpointConfig = env.getCheckpointConfig
        checkpointConfig.setExternalizedCheckpointCleanup(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
        checkpointConfig.setMinPauseBetweenCheckpoints(config.checkpointingPauseSeconds)
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
        checkpointConfig.setCheckpointStorage(s"${config.checkpointingBaseUrl.getOrElse("")}/${config.jobName}")
      }
      case _ => // Do nothing
    }

    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(config.restartAttempts, config.delayBetweenAttempts))
    env
  }
}