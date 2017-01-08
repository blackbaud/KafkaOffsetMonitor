package com.quantifind.utils

import java.io.StringReader
import java.util.Properties

import com.blackbaud.configserver.client.ConfigServerClient
import com.quantifind.kafka.offsetapp.{OWArgs, OffsetGetterArgs}
import kafka.utils.Logging
import net.liftweb.json
import net.liftweb.json.{JValue, DefaultFormats}
import net.liftweb.json.JsonAST.{JString, JField, JObject}

import scala.concurrent.duration.{Duration, FiniteDuration}


class OffsetMonitorCloudFoundryConfig extends Logging {

  private implicit val formats = DefaultFormats
  private val vcapServicesJson: JValue = getVcapServicesJson()
  private val configProperties: Properties = getConfigServerProperties(vcapServicesJson)

  private def getVcapServicesJson(): JValue = {
    val vcapServicesJsonString = System.getenv("VCAP_SERVICES")

    json.parse(vcapServicesJsonString)
  }

  private def getConfigServerProperties(vcapServicesJson: JValue): Properties = {
    val configServerUris = for {
      config@JObject(x) <- vcapServicesJson \ "user-provided"
      if x contains new JField("name", new JString("lonxt-config"))
      JField("uri", JString(uri)) <- config \ "credentials"
    } yield uri

    val configServerClient = new ConfigServerClient(configServerUris.head)

    val activeSpringProfiles = System.getenv("SPRING_PROFILES_ACTIVE")
    val propertiesText = configServerClient.getCloudConfigWithValuesEncrypted("kafka-offset-monitor", activeSpringProfiles, "properties")

    val properties = new Properties()
    properties.load(new StringReader(propertiesText))
    properties
  }

  def initArgs(args: OWArgs) {
    initArgs(args.asInstanceOf[OffsetGetterArgs])

    val consumerGroupExcludePatterns = configProperties.getProperty("offsetmanager.consumerGroupExcludePatterns")
    if (consumerGroupExcludePatterns != null) {
      args.consumerGroupExcludePatterns = consumerGroupExcludePatterns
    }

    val refreshRate = configProperties.getProperty("offsetmanager.refreshRate")
    if (refreshRate != null) {
      args.refresh = Duration(refreshRate).asInstanceOf[FiniteDuration]
    }

    val retention = configProperties.getProperty("offsetmanager.retention")
    if (retention != null) {
      args.retain = Duration(retention).asInstanceOf[FiniteDuration]
    }

    val port = configProperties.getProperty("offsetmanager.port")
    if (port != null) {
      args.port = port.toInt
    }

    logger.info("Config, port=" + args.port + ", refresh=" + args.refresh + ", retaion=" + args.retain )
  }

  def initArgs(args: OffsetGetterArgs) {
    val offsetStorage = configProperties.getProperty("offsetmanager.storage")
    if (offsetStorage != null) {
      args.offsetStorage = offsetStorage
    }

    val kafkaOffsetForceFromStart = configProperties.getProperty("offsetmanager.kafka.forceOffsetFromStart")
    if (kafkaOffsetForceFromStart != null) {
      args.kafkaOffsetForceFromStart = kafkaOffsetForceFromStart.toBoolean
    }

    val truststoreLocation = configProperties.getProperty("kafka.sslTruststoreLocation")
    if (truststoreLocation != null) {
      args.kafkaSslTruststoreLocation = truststoreLocation
    }

    val truststorePassword = (vcapServicesJson \ "Kafka Cluster" \ "credentials" \ "kafka" \"ssl_truststore_key" ).extract[String]
    if (truststorePassword != null) {
      args.kafkaSslTruststorePassword = truststorePassword
    }

    val zookeeperNodeIps = (vcapServicesJson \ "Kafka Cluster" \ "credentials" \ "zookeeper" \"node_ips" ).extract[String]
    if (zookeeperNodeIps != null) {
      args.zk = zookeeperNodeIps
    }

    logger.info("Config, offsetStorage=" + args.offsetStorage + ", kafkaOffsetForceFromStart=" + args.kafkaOffsetForceFromStart
      + ", zookeeperUri=" + args.zk + ", kafkaSslTruststoreLocation=" + args.kafkaSslTruststoreLocation)
  }

}

object OffsetMonitorCloudFoundryConfig {

  private val instance = new OffsetMonitorCloudFoundryConfig()

  def getInstance(): OffsetMonitorCloudFoundryConfig = {
    instance
  }

}
