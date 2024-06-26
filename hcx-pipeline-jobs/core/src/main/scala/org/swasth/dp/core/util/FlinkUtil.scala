package org.swasth.dp.core.util

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.runtime.state.StateBackend
import org.apache.flink.runtime.state.filesystem.FsStateBackend
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
        val stateBackend: StateBackend = new FsStateBackend(s"${config.checkpointingBaseUrl.getOrElse("")}/${config.jobName}", true)
        env.setStateBackend(stateBackend)
        val checkpointConfig: CheckpointConfig = env.getCheckpointConfig
        checkpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
        checkpointConfig.setMinPauseBetweenCheckpoints(config.checkpointingPauseSeconds     )
        checkpointConfig.setExternalizedCheckpointCleanup(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
        checkpointConfig.setMinPauseBetweenCheckpoints(config.checkpointingPauseSeconds)
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
        checkpointConfig.setTolerableCheckpointFailureNumber(3)
        checkpointConfig.setForceUnalignedCheckpoints(true)
        checkpointConfig.setCheckpointStorage(s"${config.checkpointingBaseUrl.getOrElse("")}/${config.jobName}")
      }
      case _ => // Do nothing
    }

    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(config.restartAttempts, config.delayBetweenAttempts))
    env
  }
}