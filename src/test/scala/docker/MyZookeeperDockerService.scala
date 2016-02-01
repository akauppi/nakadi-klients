/*
* Run Zookeeper as a Docker container that we'll launch (and take down) as part of test execution.
*/
package test.docker

import com.whisk.docker.{DockerReadyChecker, DockerContainer, DockerKit}

// Based on 'DockerZookeeperService' by 'docker-it-scala' project (but with Nakadi-Klient specific settings)
//
trait MyZookeeperDocker extends DockerKit {

  val zookeeperContainer = DockerContainer("wurstmeister/zookeeper")    // image name
    .withPorts(2181 -> None)
    .withReadyChecker(DockerReadyChecker.LogLineContains("binding to port"))

  abstract override def dockerContainers: List[DockerContainer] =
    zookeeperContainer :: super.dockerContainers
}
