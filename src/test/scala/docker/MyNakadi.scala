/*
* Run Nakadi as a Docker container that we'll launch (and take down) as part of test execution.
*/
package test.docker

import com.whisk.docker.{DockerReadyChecker, DockerContainer, DockerKit}

trait MyNakadi extends DockerKit {

  // From Benjamin's yaml:
  /*
  environment:
    KAFKA_BROKER: "kafka:9092,kafka_fixed:9092"
    ZK_BROKERS: "zk:2181"
   */

  private
  val env = Seq(
    "KAFKA_BROKER=kafka:9092",
    "ZK_BROKERS=zk:2181"
  )

  // Note: The PierOne Docker image is only visible within Zalando (not open source). Update this to some
  //    publicly available image, once things work. tbd AKa030216
  //
  // Note: Benjamin mentioned (030216) that 0.2-SNAPSHOT is the version to use. AKa030216
  //
  // Note: Benjamin also mentioned just one 'kafka' (no need for 'kafka_fixed') should be enough. AKa030216
  //
  private
  val image = "pierone.stups.zalan.do/laas/aruha-eventstore:0.2-SNAPSHOT"

  val nakadiContainer = DockerContainer(image)
    .withPorts( 8080 -> None )
    .withEnv(env:_*)
    .withReadyChecker( DockerReadyChecker.LogLineContains("Nakadi running") )

  abstract override def dockerContainers: List[DockerContainer] =
    nakadiContainer :: super.dockerContainers
}
