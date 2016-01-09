package org.zalando.nakadi.client

import org.zalando.nakadi.client.domain.{Cursor, TopicPartition, Topic, Event}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future


/*
 * Paramters for listening on Nakadi
 *
 * @param startOffset start position in 'queue' from which events are received
 * @param batchLimit  number of events which should be received at once (per poll). Must be > 0
 * @param batchFlushTimeoutInSeconds maximum time in seconds to wait for the flushing of each chunk;
 *                                   if the `batch_limit` is reached before this time is reached the messages are
 *                                   immediately flushed to the client
 * @param streamLimit maximum number of events which can be consumed with this stream (consumption finishes after
 *                    {streamLimit} events). If 0 or undefined, will stream indefinitely. Must be > -1
 */
case class ListenParameters(startOffset: Option[String],
                            batchLimit: Option[Int],
                            batchFlushTimeoutInSeconds: Option[Int],
                            streamLimit: Option[Int])

trait Client {
  /**
   * Gets monitoring metrics.
   * NOTE: metrics format is not defined / fixed
   *
   * @return immutable map of metrics data (value can be another Map again)
   */
  def getMetrics: Future[Map[String, AnyRef]]

  /**
   * Lists all known `Topics` in Event Store.
   *
   * @return immutable list of known topics
   */
  def getTopics: Future[List[Topic]]

  /**
   * Get partition information of a given topic
   *
   * @param topic   target topic
   * @return immutable list of topic's partitions information
   */
  def getPartitions(topic: String): Future[Either[String, List[TopicPartition]]]

  /**
   * Post a single event to the given topic.  Partition selection is done using the defined partition resolution.
   * The partition resolution strategy is defined per topic and is managed by event store (currently resolved from
   * hash over Event.orderingKey).
   * @param topic  target topic
   * @param event  event to be posted
   */
  def postEvent(topic: String, event: Event): Future[Either[String, Unit]]

  /**
   * Get specific partition
   *
   * @param topic  topic where the partition is located
   * @param partitionId  id of the target partition
   * @return partition information
   */
  def getPartition(topic: String, partitionId: String): Future[Either[String, TopicPartition]]

  /**
   * Post event to specific partition.
   * NOTE: not implemented by Nakadi yet
   *
   * @param topic  topic where the partition is located
   * @param partitionId  id of the target partition
   * @param event event to be posted
   */
  def postEventToPartition(topic: String, partitionId: String, event: Event): Future[Either[String, Unit]]

  /**
   * Blocking subscription to events of specified topic and partition.
   * (batchLimit is set to 1, batch flush timeout to 1,  and streamLimit to 0 -> infinite streaming receiving 1 event per poll)
   *
   * @param parameters listen parameters
   * @param listener  listener consuming all received events
   */
  def listenForEvents(topic: String, partitionId: String,parameters: ListenParameters, listener: (Cursor, Event) => Unit, autoReconnect: Boolean = false): Future[Either[String, _]]


  /**
   * Non-blocking subscription to a topic requires a `EventListener` implementation. The event listener must be thread-safe because
   * the listener listens to all partitions of a topic (one thread each).
   *
   * @param parameters listen parameters
   * @param listener  listener consuming all received events
   * @return {Future} instance of listener threads
   */
  def subscribeToTopic(topic: String, partitionId: String, parameters: ListenParameters, listener: (Cursor, Event) => Unit, autoReconnect: Boolean = false): Future[Either[String, _]]
}