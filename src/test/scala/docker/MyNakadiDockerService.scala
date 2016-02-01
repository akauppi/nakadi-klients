/*
* Run Nakadi as a Docker container that we'll launch (and take down) as part of test execution.
*/
package test.docker

import com.whisk.docker.{DockerReadyChecker, DockerContainer, DockerKit}

trait MyNakadiDocker extends DockerKit {

  // tbd. Should cater for these (from Benjamin's yaml):
  /*
  environment:
    KAFKA_BROKER: "kafka:9092,kafka_fixed:9092"
    ZK_BROKERS: "zk:2181"
   */
  val nakadiContainer = DockerContainer("pierone.stups.zalan.do/laas/aruha-eventstore:0.1-SNAPSHOT")  // image name
    .withPorts( 8080 -> None )
    .withReadyChecker( DockerReadyChecker.LogLineContains("Nakadi running") )

  abstract override def dockerContainers: List[DockerContainer] =
    nakadiContainer :: super.dockerContainers
}
