package datadog.trace.civisibility.source.index;

import datadog.trace.civisibility.ipc.RepoIndexRequest;
import datadog.trace.civisibility.ipc.RepoIndexResponse;
import datadog.trace.civisibility.ipc.SignalClient;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoIndexFetcher implements RepoIndexProvider {

  private static final Logger log = LoggerFactory.getLogger(RepoIndexFetcher.class);

  private final Supplier<SignalClient> signalClientFactory;
  private final Object indexInitializationLock = new Object();
  private volatile RepoIndex index;

  public RepoIndexFetcher(Supplier<SignalClient> signalClientFactory) {
    this.signalClientFactory = signalClientFactory;
  }

  @Override
  public RepoIndex getIndex() {
    if (index == null) {
      synchronized (indexInitializationLock) {
        if (index == null) {
          index = doGetIndex();
        }
      }
    }
    return index;
  }

  private RepoIndex doGetIndex() {
    try (SignalClient signalClient = signalClientFactory.get()) {
      RepoIndexResponse response = (RepoIndexResponse) signalClient.send(RepoIndexRequest.INSTANCE);
      return response.getIndex();
    } catch (Exception e) {
      log.error("Could not fetch repo index", e);
      return RepoIndex.EMPTY;
    }
  }
}
