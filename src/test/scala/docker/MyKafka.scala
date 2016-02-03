/*
* Run Kafka as a Docker container
*/
package test.docker

import com.whisk.docker.{DockerReadyChecker, DockerContainer, DockerKit}

// Based on 'DockerKafkaService' by 'docker-it-scala' project (but with Nakadi-Klient specific settings)
//
// See -> https://github.com/wurstmeister/kafka-docker
//
trait MyKafka extends DockerKit {
  import MyKafka._

  // tbd. Should cater for these (from Benjamin's yaml):
  /*
  environment:
    KAFKA_ADVERTISED_HOST_NAME: "kafka"
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock
   */
  private
  val env: Seq[String] = Map(
    "KAFKA_ADVERTISED_HOST_NAME" -> "kafka",
    "KAFKA_CREATE_TOPICS" -> "Topic1:1:1,Topic2:1:1"    // tbd (do we want to create fixed topics?) AKa030216
  ).map( t => s"$t._1=$t._2" ).toSeq

  // Note: Benjamin told (030216) that a standard Kafka image (maybe also 0.9) should work.
  //
  //val image = "wurstmeister/kafka:0.8.2.0"    // as in yaml
  private
  val image = "wurstmeister/kafka:0.9.0.0"

  val kafkaContainer = DockerContainer(image)
    .withPorts( advertisedPort -> Some(advertisedPort), zookeeperDefaultPort -> None )
    .withEnv(env:_*)
    .withReadyChecker( DockerReadyChecker.LogLineContains("Kafka entered RUNNING state") )

  abstract override def dockerContainers: List[DockerContainer] =
    kafkaContainer :: super.dockerContainers
}

object MyKafka {
  val advertisedPort = 9092
  val zookeeperDefaultPort = 2181
}

/*** NOT NEEDED? AKa030216
// tbd. Explain what's different with "fixed" Kafka? Why do we use two in tests? AKa010216
//
trait MyKafkaFixedDocker extends DockerKit {

  def kafkaAdvertisedPort = 9092
  val zookeeperDefaultPort = 2181

  // tbd. Should cater for these (from Benjamin's yaml):
  /*
  environment:
    KAFKA_ADVERTISED_HOST_NAME: "kafka-fixed"
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock
   */
  val kafkaFixedContainer = DockerContainer("wurstmeister/kafka:0.8.2.0")    // image name (tbd. why not 0.9.0?)
    .withPorts( kafkaAdvertisedPort -> Some(kafkaAdvertisedPort), zookeeperDefaultPort -> None )
    .withReadyChecker(
      DockerReadyChecker.LogLineContains("Kafka entered RUNNING state")
    )

  abstract override def dockerContainers: List[DockerContainer] =
    kafkaFixedContainer :: super.dockerContainers
}
***/