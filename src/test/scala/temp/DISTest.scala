/*
* Testing the docker-it-scala usage
*/
package test.temp

import com.whisk.docker.scalatest.DockerTestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{GivenWhenThen, BeforeAndAfterAll, Matchers, FlatSpec}

import test.docker._

object DISTest extends FlatSpec with Matchers with BeforeAndAfterAll with GivenWhenThen with ScalaFutures
  with MyZookeeper
  with MyKafka
  with MyNakadi
  with DockerTestKit {    // Note: 'DockerTestKit' probably needs to be last

  // Make this a 'main', as long as it's being prototyped.
  //
  // Use: 'sbt test:run'
  //
  def main(args: Seq[String]): Unit = {

    //implicit val patience = PatienceConfig(Span(20, Seconds), Span(1, Second))

    "all containers" should "be ready at the same time" in {
      dockerContainers.foreach(x => info(x.image))
      dockerContainers.forall(_.isReady().futureValue) shouldBe true
    }
  }
}
