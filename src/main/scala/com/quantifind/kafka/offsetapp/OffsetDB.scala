package com.quantifind.kafka.offsetapp

import java.sql.SQLException

import com.quantifind.kafka.OffsetGetter.OffsetInfo
import com.quantifind.kafka.offsetapp.ConsumerGroupDB.DbConsumerGroupInfo
import com.quantifind.kafka.offsetapp.OffsetDB.{OffsetHistory, DbOffsetInfo, OffsetPoints}
import com.twitter.util.Time

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.JdbcBackend
import scala.slick.jdbc.meta.MTable

/**
 * Tools to store offsets in a DB
 * User: andrews
 * Date: 1/27/14
 */
class OffsetDB(dbfile: String) {

  val database = Database.forURL(s"jdbc:sqlite:$dbfile.db",
    driver = "org.sqlite.JDBC")

  implicit val twitterTimeMap = MappedColumnType.base[Time, Long](
  {
    time => time.inMillis
  }, {
    millis => Time.fromMilliseconds(millis)
  }
  )

  class ConsumerGroup(tag: Tag) extends Table[DbConsumerGroupInfo](tag, "CONSUMER_GROUP") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val group = column[String]("group")
    val topic = column[String]("topic")
    val owner = column[Option[String]]("owner")

    def * = (id.?, group, topic, owner).shaped <>(DbConsumerGroupInfo.parse, DbConsumerGroupInfo.unparse)

    def idx = index("idx_consumer_group__group_topic", (group, topic))

    def uidx = index("idx_consumer_group__unique", (group, topic, owner), unique = true)

  }

  class Offset(tag: Tag) extends Table[DbOffsetInfo](tag, "OFFSETS") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    val consumerGroup = column[Int]("consumerGroup")
    val partition = column[Int]("partition")
    val offset = column[Long]("offset")
    val logSize = column[Long]("log_size")
    val timestamp = column[Long]("timestamp")
    val creation = column[Time]("creation")
    val modified = column[Time]("modified")


    def * = (id.?, timestamp, consumerGroup, partition, offset, logSize, creation, modified).shaped <>(DbOffsetInfo.parse, DbOffsetInfo.unparse)

    def forHistory = (timestamp, partition, offset, logSize) <>(OffsetPoints.tupled, OffsetPoints.unapply)

    def idx = index("idx_search", (consumerGroup))

    def tidx = index("idx_time", (timestamp))

    def uidx = index("idx_unique", (consumerGroup, partition, timestamp), unique = true)

  }

  val offsets = TableQuery[Offset]
  val consumerGroups = TableQuery[ConsumerGroup]

  def insert(timestamp: Long, info: OffsetInfo) {
    database.withSession {
      implicit s =>
        val consumerGroupId = getConsumerGroupIdInsertIfMissing(s, info)
        offsets += createDbOffset(timestamp, consumerGroupId, info)
    }
  }

  def createDbOffset(timestamp: Long, consumerGroupId: Int, info: OffsetInfo): DbOffsetInfo = {
    DbOffsetInfo(timestamp = timestamp, consumerGroup = consumerGroupId, partition = info.partition,
      offset = info.offset, logSize = info.logSize, creation = info.creation, modified = info.modified)
  }

  private def getConsumerGroupId(implicit s: Session, info: OffsetInfo): Option[Int] = {
    consumerGroups.filter(r =>
      r.group === info.group && r.topic === info.topic && r.owner === info.owner
    ).map(_.id).firstOption
  }

  private def getConsumerGroupIdInsertIfMissing(implicit s: Session, info: OffsetInfo): Int = {
    val result = getConsumerGroupId(s, info)
    if (result.isDefined) {
      return result.get
    }

    try {
      consumerGroups returning consumerGroups.map(_.id) +=
        DbConsumerGroupInfo(None, info.group, info.topic, info.owner)
    } catch {
      case ex: SQLException =>
        if (ex.getMessage.contains("CONSTRAINT")) {
          getConsumerGroupId(s, info).get
        } else {
          throw ex
        }
    }
  }

  def insertAll(info: IndexedSeq[OffsetInfo]) {
    val now = System.currentTimeMillis
    database.withTransaction {
      implicit s =>
        offsets ++= info.map { info =>
          val consumerGroupId = getConsumerGroupIdInsertIfMissing(s, info)
          createDbOffset(timestamp = now, consumerGroupId = consumerGroupId, info = info)
        }
    }
  }

  def emptyOld(since: Long) {
    database.withSession {
      implicit s =>
        offsets.filter(_.timestamp < since).delete
    }
  }

  def offsetHistory(group: String, topic: String): OffsetHistory = database.withSession {
    implicit s =>
      val consumerGroupIds = consumerGroups.filter(r =>
        r.group === group && r.topic === topic
      ).map(_.id)
      val offsetPoints = offsets
        .filter(_.consumerGroup in consumerGroupIds)
        .sortBy(_.timestamp)
        .map(_.forHistory)
        .list(implicitly[JdbcBackend#Session])
      OffsetHistory(group, topic, offsetPoints)
  }

  def maybeCreate() {
    database.withSession {
      implicit s =>
        if (MTable.getTables("OFFSETS").list(implicitly[JdbcBackend#Session]).isEmpty) {
          offsets.ddl.create
        }
        if (MTable.getTables("CONSUMER_GROUP").list(implicitly[JdbcBackend#Session]).isEmpty) {
          consumerGroups.ddl.create
        }
    }
  }

}

object OffsetDB {

  case class OffsetPoints(timestamp: Long, partition: Int, offset: Long, logSize: Long)

  case class OffsetHistory(group: String, topic: String, offsets: Seq[OffsetPoints])

  case class DbOffsetInfo(id: Option[Int] = None, timestamp: Long, consumerGroup: Int, partition: Int,
                          offset: Long, logSize: Long, creation: Time, modified: Time)

  object DbOffsetInfo {
    def parse(in: (Option[Int], Long, Int, Int, Long, Long, Time, Time)): DbOffsetInfo = {
      val (id, timestamp, consumerGroup, partition, offset, logSize, creation, modified) = in
      DbOffsetInfo(id, timestamp, consumerGroup, partition, offset, logSize, creation, modified)
    }

    def unparse(in: DbOffsetInfo): Option[(Option[Int], Long, Int, Int, Long, Long, Time, Time)] = Some(
      in.id,
      in.timestamp,
      in.consumerGroup,
      in.partition,
      in.offset,
      in.logSize,
      in.creation,
      in.modified
    )
  }

}

object ConsumerGroupDB {

  case class DbConsumerGroupInfo(id: Option[Int] = None, group: String, topic: String, owner: Option[String])

  object DbConsumerGroupInfo {
    def parse(in: (Option[Int], String, String, Option[String])): DbConsumerGroupInfo = {
      val (id, group, topic, owner) = in
      DbConsumerGroupInfo(id, group, topic, owner)
    }

    def unparse(in: DbConsumerGroupInfo): Option[(Option[Int], String, String, Option[String])] = Some(
      in.id,
      in.group,
      in.topic,
      in.owner
    )
  }

}
