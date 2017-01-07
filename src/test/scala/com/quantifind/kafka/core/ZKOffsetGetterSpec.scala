package com.quantifind.kafka.core

import com.quantifind.utils.ZkUtilsWrapper
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.{Matchers => MockitoMatchers, Mockito}
import org.scalatest._

class ZKOffsetGetterSpec extends FlatSpec with ShouldMatchers {

  trait Fixture {

    val mockedZkUtil =  Mockito.mock(classOf[ZkUtilsWrapper])

    val offsetGetter = new ZKOffsetGetter(mockedZkUtil)
  }

  "ZKOffsetGetter" should "be able to fetch topic list" in new Fixture {

    val testGroup = "testgroup"
    val testTopic1 = "testtopic1"
    val testTopic2 = "testtopic2"

    when(mockedZkUtil.getChildren(anyString)).thenReturn(Seq(testTopic1, testTopic2))

    val topics = offsetGetter.getTopicList(testGroup)
    
    topics.size shouldBe 2
    topics(0) shouldBe testTopic1
    topics(1) shouldBe testTopic2
  }

  "ZKOffsetGetter" should "be able to fetch consumer group list" in new Fixture {

    val testGroup1 = "testgroup1"
    val testGroup2 = "testgroup2"

    when(mockedZkUtil.getChildren(anyString)).thenReturn(Seq(testGroup1, testGroup2))

    val groups = offsetGetter.getGroups

    groups.size shouldBe 2
    groups(0) shouldBe testGroup1
    groups(1) shouldBe testGroup2
  }
}
