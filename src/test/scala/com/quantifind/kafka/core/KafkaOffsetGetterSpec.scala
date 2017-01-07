package com.quantifind.kafka.core

import com.quantifind.kafka.core.KafkaOffsetGetter.GroupTopicPartition
import com.quantifind.utils.ZkUtilsWrapper
import kafka.api.{OffsetRequest, OffsetResponse, PartitionOffsetsResponse}
import kafka.common.{OffsetAndMetadata, TopicAndPartition}
import kafka.consumer.SimpleConsumer
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.{Matchers => MockitoMatchers, Mockito}
import org.scalatest._

class KafkaOffsetGetterSpec extends FlatSpec with ShouldMatchers {

  trait Fixture {

    val mockedZkUtil =  Mockito.mock(classOf[ZkUtilsWrapper])
    val mockedConsumer = Mockito.mock(classOf[SimpleConsumer])
    val testPartitionLeader = 1

    val offsetGetter = new KafkaOffsetGetter(mockedZkUtil)
    offsetGetter.consumerMap += (testPartitionLeader -> Some(mockedConsumer))
  }

  "KafkaOffsetGetter" should "be able to build offset data for given partition" in new Fixture {

    val testGroup = "testgroup"
    val testTopic = "testtopic"
    val testPartition = 1

    val topicAndPartition = TopicAndPartition(testTopic, testPartition)
    val groupTopicPartition = GroupTopicPartition(testGroup, topicAndPartition)
    val offsetAndMetadata = OffsetAndMetadata(100, "meta", System.currentTimeMillis)
    KafkaOffsetGetter.offsetMap += (groupTopicPartition -> offsetAndMetadata)

    when(mockedZkUtil.getLeaderForPartition(MockitoMatchers.eq(testTopic), MockitoMatchers.eq(testPartition)))
      .thenReturn(Some(testPartitionLeader))

    val partitionErrorAndOffsets = Map(topicAndPartition -> PartitionOffsetsResponse(0,Seq(102)))
    val offsetResponse = OffsetResponse(1, partitionErrorAndOffsets)
    when(mockedConsumer.getOffsetsBefore(any[OffsetRequest])).thenReturn(offsetResponse)

    offsetGetter.processPartition(testGroup, testTopic, testPartition) match {
      case Some(offsetInfo) =>
        offsetInfo.topic shouldBe testTopic
        offsetInfo.group shouldBe testGroup
        offsetInfo.partition shouldBe testPartition
        offsetInfo.offset shouldBe 100
        offsetInfo.logSize shouldBe 102
      case None => fail("Failed to build offset data")
    }
    
  }
}
