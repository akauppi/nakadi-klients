/*
* Zookeeper as a Docker container
*/
package test.docker

import com.whisk.docker.{DockerReadyChecker, DockerContainer, DockerKit}

// Based on 'DockerZookeeperService' by 'docker-it-scala' project (but with Nakadi-Klient specific settings)
// -> https://github.com/whisklabs/docker-it-scala/blob/master/core/src/main/scala/com/whisk/docker/DockerZookeeperService.scala
//
trait MyZookeeper extends DockerKit {
  import MyZookeeper._

  val zookeeperContainer = DockerContainer(image)
    .withPorts(2181 -> None)
    .withEnv(env:_*)
    .withReadyChecker( DockerReadyChecker.LogLineContains("binding to port") )

  abstract override def dockerContainers: List[DockerContainer] =
    zookeeperContainer :: super.dockerContainers
}

object MyZookeeper {
  private
  val env = Seq.empty[String]

  // See -> https://hub.docker.com/r/wurstmeister/zookeeper/tags/
  //
  //val image = "wurstmeister/zookeeper"    // in the yaml
  private
  val image = "wurstmeister/zookeeper:3.4.6"
}