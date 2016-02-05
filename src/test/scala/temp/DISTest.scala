package test.temp

import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.{Matchers, FlatSpec}

import test.docker._

/*
* Trying out the docker-it-scala usage (adopted from docker-it-scala project)
*/
class DISTest extends FlatSpec with Matchers
  with MyZookeeper
  with MyKafka
  with MyNakadi
  with DockerTestKit {    // Note: 'DockerTestKit' probably needs to be last

  sys.env.getOrElse("DOCKER_HOST", {
    val s= """|
        |Please set up DOCKER_HOST first:
        |
        |  $ docker-machine start xxx
        |  $ eval $(docker-machine env xxx)
        |
      """.stripMargin

    // Would like to fail the further tests nicely here, with giving the above error out. Seems hard? AKa050216
    //
    sys.error(s)    // for now (aborts here)
  })

  "all containers" should "be ready at the same time" in {
    dockerContainers.foreach(x => info(x.image))
    dockerContainers.forall(_.isReady().futureValue) shouldBe true
  }
}
