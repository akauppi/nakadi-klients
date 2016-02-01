/*
* Run Kafka as a Docker container that we'll launch (and take down) as part of test execution.
*/
package test.docker

import com.whisk.docker.{DockerReadyChecker, DockerContainer, DockerKit}

// Based on 'DockerKafkaService' by 'docker-it-scala' project (but with Nakadi-Klient specific settings)
//
trait MyKafkaDocker extends DockerKit {

  def kafkaAdvertisedPort = 9092
  val zookeeperDefaultPort = 2181

  // tbd. Should cater for these (from Benjamin's yaml):
  /*
  environment:
    KAFKA_ADVERTISED_HOST_NAME: "kafka"
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock
   */
  val kafkaContainer = DockerContainer("wurstmeister/kafka:0.8.2.0")    // image name (tbd. why not 0.9.0?)
    .withPorts( kafkaAdvertisedPort -> Some(kafkaAdvertisedPort), zookeeperDefaultPort -> None )
    .withReadyChecker(
      DockerReadyChecker.LogLineContains("Kafka entered RUNNING state")
    )

  abstract override def dockerContainers: List[DockerContainer] =
    kafkaContainer :: super.dockerContainers
}

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
