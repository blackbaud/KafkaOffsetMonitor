package com.quantifind.utils

import java.util

import kafka.api.{ApiVersion, LeaderAndIsr}
import kafka.cluster.{EndPoint, BrokerEndPoint, Cluster, Broker}
import kafka.common.TopicAndPartition
import kafka.consumer.ConsumerThreadId
import kafka.controller.{ReassignedPartitionsContext, LeaderIsrAndControllerEpoch}
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkClient
import org.apache.kafka.common.protocol.SecurityProtocol
import org.apache.zookeeper.data.{ACL, Stat}

import scala.collection.mutable

/*
  This class is mainly to help us mock the ZkUtils class. It is really painful to get powermock to work with scalatest,
  so we created this class with a little help from IntelliJ to auto-generate the delegation code
 */
class ZkUtilsWrapper(zkUtils: ZkUtils) {

  val ConsumersPath = ZkUtils.ConsumersPath

  val delegator = zkUtils

  def updatePersistentPath(path: String, data: String, acls: util.List[ACL]): Unit = delegator.updatePersistentPath(path, data, acls)

  def updatePartitionReassignmentData(partitionsToBeReassigned: collection.Map[TopicAndPartition, Seq[Int]]): Unit = delegator.updatePartitionReassignmentData(partitionsToBeReassigned)

  def updateEphemeralPath(path: String, data: String, acls: util.List[ACL]): Unit = delegator.updateEphemeralPath(path, data, acls)

  def setupCommonPaths(): Unit = delegator.setupCommonPaths()

  def replicaAssignmentZkData(map: collection.Map[String, Seq[Int]]): String = delegator.replicaAssignmentZkData(map)

  def registerBrokerInZk(id: Int, host: String, port: Int, advertisedEndpoints: collection.Map[SecurityProtocol, EndPoint], jmxPort: Int, rack: Option[String], apiVersion: ApiVersion): Unit = delegator.registerBrokerInZk(id, host, port, advertisedEndpoints, jmxPort, rack, apiVersion)

  def readDataMaybeNull(path: String): (Option[String], Stat) = delegator.readDataMaybeNull(path)

  def readData(path: String): (String, Stat) = delegator.readData(path)

  def pathExists(path: String): Boolean = delegator.pathExists(path)

  def parseTopicsData(jsonData: String): Seq[String] = delegator.parseTopicsData(jsonData)

  def parsePartitionReassignmentDataWithoutDedup(jsonData: String): Seq[(TopicAndPartition, Seq[Int])] = delegator.parsePartitionReassignmentDataWithoutDedup(jsonData)

  def parsePartitionReassignmentData(jsonData: String): collection.Map[TopicAndPartition, Seq[Int]] = delegator.parsePartitionReassignmentData(jsonData)

  def makeSurePersistentPathExists(path: String, acls: util.List[ACL]): Unit = delegator.makeSurePersistentPathExists(path, acls)

  def leaderAndIsrZkData(leaderAndIsr: LeaderAndIsr, controllerEpoch: Int): String = delegator.leaderAndIsrZkData(leaderAndIsr, controllerEpoch)

  def getTopicsByConsumerGroup(consumerGroup: String): Seq[String] = delegator.getTopicsByConsumerGroup(consumerGroup)

  def getSortedBrokerList(): Seq[Int] = delegator.getSortedBrokerList()

  def getSequenceId(path: String, acls: util.List[ACL]): Int = delegator.getSequenceId(path, acls)

  def getReplicasForPartition(topic: String, partition: Int): Seq[Int] = delegator.getReplicasForPartition(topic, partition)

  def getReplicaAssignmentForTopics(topics: Seq[String]): mutable.Map[TopicAndPartition, Seq[Int]] = delegator.getReplicaAssignmentForTopics(topics)

  def getPartitionsUndergoingPreferredReplicaElection(): collection.Set[TopicAndPartition] = delegator.getPartitionsUndergoingPreferredReplicaElection()

  def getPartitionsForTopics(topics: Seq[String]): mutable.Map[String, Seq[Int]] = delegator.getPartitionsForTopics(topics)

  def getPartitionsBeingReassigned(): collection.Map[TopicAndPartition, ReassignedPartitionsContext] = delegator.getPartitionsBeingReassigned()

  def getPartitionLeaderAndIsrForTopics(zkClient: ZkClient, topicAndPartitions: collection.Set[TopicAndPartition]): mutable.Map[TopicAndPartition, LeaderIsrAndControllerEpoch] = delegator.getPartitionLeaderAndIsrForTopics(zkClient, topicAndPartitions)

  def getPartitionAssignmentForTopics(topics: Seq[String]): mutable.Map[String, collection.Map[Int, Seq[Int]]] = delegator.getPartitionAssignmentForTopics(topics)

  def getLeaderForPartition(topic: String, partition: Int): Option[Int] = delegator.getLeaderForPartition(topic, partition)

  def getLeaderAndIsrForPartition(topic: String, partition: Int): Option[LeaderAndIsr] = delegator.getLeaderAndIsrForPartition(topic, partition)

  def getInSyncReplicasForPartition(topic: String, partition: Int): Seq[Int] = delegator.getInSyncReplicasForPartition(topic, partition)

  def getEpochForPartition(topic: String, partition: Int): Int = delegator.getEpochForPartition(topic, partition)

  def getController(): Int = delegator.getController()

  def getConsumersPerTopic(group: String, excludeInternalTopics: Boolean): mutable.Map[String, List[ConsumerThreadId]] = delegator.getConsumersPerTopic(group, excludeInternalTopics)

  def getConsumersInGroup(group: String): Seq[String] = delegator.getConsumersInGroup(group)

  def getConsumerPartitionOwnerPath(group: String, topic: String, partition: Int): String = delegator.getConsumerPartitionOwnerPath(group, topic, partition)

  def getConsumerGroups(): Seq[String] = delegator.getConsumerGroups()

  def getChildrenParentMayNotExist(path: String): Seq[String] = delegator.getChildrenParentMayNotExist(path)

  def getChildren(path: String): Seq[String] = delegator.getChildren(path)

  def getBrokerSequenceId(MaxReservedBrokerId: Int): Int = delegator.getBrokerSequenceId(MaxReservedBrokerId)

  def getBrokerInfo(brokerId: Int): Option[Broker] = delegator.getBrokerInfo(brokerId)

  def getAllTopics(): Seq[String] = delegator.getAllTopics()

  def getAllPartitions(): collection.Set[TopicAndPartition] = delegator.getAllPartitions()

  def getAllEntitiesWithConfig(entityType: String): Seq[String] = delegator.getAllEntitiesWithConfig(entityType)

  def getAllConsumerGroupsForTopic(topic: String): collection.Set[String] = delegator.getAllConsumerGroupsForTopic(topic)

  def getAllBrokersInCluster(): Seq[Broker] = delegator.getAllBrokersInCluster()

  def getAllBrokerEndPointsForChannel(protocolType: SecurityProtocol): Seq[BrokerEndPoint] = delegator.getAllBrokerEndPointsForChannel(protocolType)

  def formatAsReassignmentJson(partitionsToBeReassigned: collection.Map[TopicAndPartition, Seq[Int]]): String = delegator.formatAsReassignmentJson(partitionsToBeReassigned)

  def deletePathRecursive(path: String): Unit = delegator.deletePathRecursive(path)

  def deletePath(path: String): Boolean = delegator.deletePath(path)

  def deletePartition(brokerId: Int, topic: String): Unit = delegator.deletePartition(brokerId, topic)

  def createSequentialPersistentPath(path: String, data: String, acls: util.List[ACL]): String = delegator.createSequentialPersistentPath(path, data, acls)

  def createPersistentPath(path: String, data: String, acls: util.List[ACL]): Unit = delegator.createPersistentPath(path, data, acls)

  def createEphemeralPathExpectConflict(path: String, data: String, acls: util.List[ACL]): Unit = delegator.createEphemeralPathExpectConflict(path, data, acls)

  def conditionalUpdatePersistentPathIfExists(path: String, data: String, expectVersion: Int): (Boolean, Int) = delegator.conditionalUpdatePersistentPathIfExists(path, data, expectVersion)

  def conditionalUpdatePersistentPath(path: String, data: String, expectVersion: Int, optionalChecker: Option[(ZkUtils, String, String) => (Boolean, Int)]): (Boolean, Int) = delegator.conditionalUpdatePersistentPath(path, data, expectVersion, optionalChecker)

  def conditionalDeletePath(path: String, expectedVersion: Int): Boolean = delegator.conditionalDeletePath(path, expectedVersion)

  def close(): Unit = delegator.close()

}
