package dev.streamx.ingestion;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface IngestionConfig {

  String DEFAULT_SOCKET_TIMEOUT = "1000";
  String DEFAULT_CONNECTION_TIMEOUT = "5000";
  String DEFAULT_STREAMX_BATCH_SOURCE_PROVIDER_BATCH_SIZE_IN_BYTES = "3000000";


  String STREAMX_INGESTION_CHANNELS_API = "streamx.ingestion.channels.api";
  String STREAMX_SOCKET_TIMEOUT = "streamx.connection.socket.timeout";
  String STREAMX_CONNECTION_TIMEOUT = "streamx.connection.timeout";
  String STREAMX_BATCH_SOURCE_PROVIDER_BATCH_SIZE_IN_BYTES =
      "streamx.batch-source-provider.batch.size";

  @WithName(STREAMX_INGESTION_CHANNELS_API)
  String ingestionChannelsApi();

  @WithName(STREAMX_SOCKET_TIMEOUT)
  @WithDefault(DEFAULT_SOCKET_TIMEOUT)
  int connectionSocketTimeoutMillis();

  @WithName(STREAMX_CONNECTION_TIMEOUT)
  @WithDefault(DEFAULT_CONNECTION_TIMEOUT)
  int connectionTimeoutMillis();

  @WithName(STREAMX_BATCH_SOURCE_PROVIDER_BATCH_SIZE_IN_BYTES)
  @WithDefault(DEFAULT_STREAMX_BATCH_SOURCE_PROVIDER_BATCH_SIZE_IN_BYTES)
  long batchSourceProviderBatchSizeInBytes();
}
