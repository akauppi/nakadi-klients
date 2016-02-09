package test

import java.net.URI

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import de.zalando.scoop.Scoop
import org.zalando.nakadi.client.{KlientBuilder}

/*
* ...tbd. What is the purpose of this? AKa050216
*
* Usage:
*   sbt test:run
*/
object Main {
  implicit val system = ActorSystem()   // @Benjamin: better give it a name, at least for logging? AKa050216
  implicit val materializer = ActorMaterializer()

  def main (args: Array[String]) {

    import org.zalando.nakadi.client.{
      Cursor => NakadiCursor,
      Event => NakadiEvent,
      Listener => NakadiListener,
      ListenParameters => NakadiListenParameters
    }

    // @Benjamin: what are these? (i.e. why fixed, what do they mean, should we place them in a config?) AKa050216
    //
    val scoop = new Scoop().withBindHostName("localhost")
                           .withSeed("akka.tcp://scoop-nakadi-client@localhost:25551")

    val klient = KlientBuilder()
      .withEndpoint(new URI("localhost"))       // why not port here as well, via URI AKa050216
      .withPort(8080)
      .withSecuredConnection(false)
      .withTokenProvider(() => "<my token>")    // @Benjamin: Where is this supposed to come? AKa050216
      .withScoop(Some(scoop))
      .withScoopTopic(Some("scoop"))
      .build

    val listener = new NakadiListener {

      override def id = "test"

      override def onReceive(topic: String, partition: String, cursor: NakadiCursor, event: NakadiEvent): Unit = println(s">>>>> [event=$event, partition=$partition]")

      override def onConnectionClosed(topic: String, partition: String, lastCursor: Option[NakadiCursor]): Unit = println(s"connection closed [partition=$partition]")

      override def onConnectionOpened(topic: String, partition: String): Unit = println(s"connection opened [partition=$partition]")

      override def onConnectionFailed(topic: String, partition: String, status: Int, error: String): Unit = println(s"connection failed [topic=$topic, partition=$partition, status=$status, error=$error]")
    }

    klient.subscribeToTopic("test", NakadiListenParameters(Some("0")), listener, true)

    Thread.sleep(Long.MaxValue)
  }
}