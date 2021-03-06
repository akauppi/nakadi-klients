package de.zalando.nakadi.client;

import akka.actor.ActorSystem;
import akka.cluster.ClusterReadView;
import akka.cluster.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import de.zalando.nakadi.client.domain.Cursor;
import de.zalando.nakadi.client.domain.Event;
import de.zalando.nakadi.client.domain.SimpleStreamEvent;
import de.zalando.scoop.Scoop;
import de.zalando.scoop.ScoopClient;
import de.zalando.scoop.ScoopListener;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;


public class ScoopAwareNakadiClientImpl extends NakadiClientImpl
        implements ScoopListener, EventListener {

    private final ObjectMapper objectMapper;
    private final ActorSystem scoopSystem;
    private final ScoopClient scoopClient;
    private final String scoopTopic;
    private ClusterReadView clusterReadView;

    private volatile boolean mayIProcessEvents;

    public static final String UNREACHABLE_MEMBER_EVENT_TYPE = "/scoop-system/unreachable-member";
    private static final String UNREACHABLE_MEMBER_EVENT_BODY_KEY = "unreachable_member";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoopAwareNakadiClientImpl.class);

    ScoopAwareNakadiClientImpl(final URI endpoint,
                               final OAuth2TokenProvider tokenProvider,
                               final ObjectMapper objectMapper,
                               final Scoop scoop,
                               final String scoopTopic) {
        super(endpoint, tokenProvider, objectMapper);

        checkNotNull(scoop, "Scoop instance must not be null");
        checkArgument(!isNullOrEmpty(scoopTopic), "Scoop topic must not be null or empty");

        this.objectMapper = objectMapper;
        this.scoopSystem = scoop.withListener(this).build();
        this.scoopClient = scoop.defaultClient();
        this.scoopTopic = scoopTopic;
        this.mayIProcessEvents = true;

        subscribeToTopic(this.scoopTopic, this);
    }


    @Override
    public void listenForEvents(final String topic,
                                final String partitionId,
                                final String startOffset,
                                final int batchLimit,
                                final int batchFlushTimeoutInSeconds,
                                final int streamLimit,
                                final EventListener listener) {
        checkArgument(!isNullOrEmpty(topic), "topic must not be null or empty");
        checkArgument(!isNullOrEmpty(partitionId), "partition id must not be null or empty");
        checkArgument(! isNullOrEmpty(startOffset), "start offset must not be null or empty");
        checkArgument(batchLimit > 0, "batch limit must be > 0. Given: %s", batchLimit);
        checkArgument(batchFlushTimeoutInSeconds > -1, "batch flush timeout must be > -1. Given: %s", batchFlushTimeoutInSeconds);
        checkArgument(streamLimit > -1, "stream limit must be > -1. Given: %s", streamLimit);

        final String uri = String.format(URI_EVENT_LISTENING, topic, partitionId, startOffset, batchLimit, batchFlushTimeoutInSeconds, streamLimit);
        final HttpRequestBase request = setUpRequest(HttpMethod.GET, uri);
        final CloseableHttpResponse response = performRequest(request);


        try {
            final InputStream contentInputStream = response.getEntity().getContent();
            final ByteArrayOutputStream bout = new ByteArrayOutputStream(INITIAL_RECEIVE_BUFFER_SIZE);

            SimpleStreamEvent streamingEvent;
            List<Event> events;
            int byteItem;
            while ((byteItem = contentInputStream.read()) != -1) {
                bout.write(byteItem);

                if(byteItem == EOL) {
                    final byte[] receiveBuffer = bout.toByteArray();
                    if(hasNonWhiteCharacters(receiveBuffer)){
                        streamingEvent = objectMapper.readValue(bout.toByteArray(), SimpleStreamEvent.class);
                        LOGGER.debug("received [streamingEvent={}]", streamingEvent);
                        final Cursor cursor = streamingEvent.getCursor();
                        events = streamingEvent.getEvents();
                        if(events == null || events.isEmpty()) {
                            LOGGER.debug("received [streamingEvent={}] does not contain any event ", streamingEvent);
                        }
                        else {
                            streamingEvent.getEvents().forEach( event -> {

                                if (mayIProcessEvents) {
                                    try {
                                        final Map<String, Object> meta = event.getMetadata();
                                        final String id = (String) meta.get("id");

                                        if (id == null) {
                                            LOGGER.warn("meta data 'id' is not set in [event={}] -> consuming event", event);
                                            listener.onReceive(cursor, event);
                                        } else if (scoopClient.isHandledByMe(id)) {
                                            LOGGER.debug("scoop: [event={}] IS handled by me", event);
                                            listener.onReceive(cursor, event);
                                        } else if (Objects.equals(event.getEventType(), UNREACHABLE_MEMBER_EVENT_TYPE)) {
                                            LOGGER.debug("[event={}] is handled because it has scoop internal [eventType={}]",
                                                    event, UNREACHABLE_MEMBER_EVENT_TYPE);
                                            listener.onReceive(cursor, event);
                                        } else {
                                            LOGGER.debug("scoop: [event={}] is NOT handled by me", event);
                                        }
                                    } catch (final Exception e) {
                                        LOGGER.warn("a problem occurred while passing [cursor={}, event={}] to [listener={}] -> continuing with next events",
                                                cursor, event, listener, e);
                                    }
                                }
                                else {
                                    LOGGER.info("received [event={}] but I must not process it as I am not reachable by " +
                                            "my cluster -> ignored", event);
                                }
                            });
                        }
                    }
                    else {
                        LOGGER.debug("receive buffer consists of one EOL character only -> skipped");
                    }

                    bout.reset();
                }
            }
            LOGGER.info("stream from [response={}] retrieved from [URI={}] was closed", response, uri);
        } catch (final IOException e) {
            throw new ClientException(e);
        }
    }

    @Override
    public void init(final ClusterReadView clusterReadView) {
        this.clusterReadView = clusterReadView;
    }


    /**
     * If this instance is the cluster leader, it has to notify all other members about unreachable member
     * because this allows the unreachable member to get known about the fact it is not reachable by the cluster.
     * As a consequence, the unreachable member can stop processing events as it is not considered by the cluster
     * anymore.
     */
    @Override
    public void onMemberUnreachable(final Member member) {
        if(clusterReadView.isLeader()){
            LOGGER.info("I AM the LEADER and I am notifying other Scoop aware clients about [unreachableMember={}]", member);

            final Event event = new Event();
            event.setEventType(UNREACHABLE_MEMBER_EVENT_TYPE);
            event.setOrderingKey("scoop-system"); // TODO better ordering key?

            final HashMap<String, Object> metadata = Maps.newHashMap();
            metadata.put("id", UUID.randomUUID().toString());

            final Map<String, String> bodyMap = Maps.newHashMap();
            bodyMap.put(UNREACHABLE_MEMBER_EVENT_BODY_KEY, member.address().toString());
            event.setBody(bodyMap);

            postEvent(scoopTopic, event);
        }
        else {
            LOGGER.debug("received event about [unreachableMember={}] but I am NOT the LEADER -> ignored", member);
        }
    }


    @Override
    public void onReceive(final Cursor cursor, final Event event) {

        final String eventType = event.getEventType();
        if(Objects.equals(eventType, UNREACHABLE_MEMBER_EVENT_TYPE)){
            LOGGER.info("received event of [eventType={}] -> [event={}]", UNREACHABLE_MEMBER_EVENT_TYPE, event);

            final Map<String, String> bodyMap = (Map<String, String>) event.getBody();
            final String unreachableMemberAsString = bodyMap.get(UNREACHABLE_MEMBER_EVENT_BODY_KEY);
            if(isNullOrEmpty(unreachableMemberAsString)){
                LOGGER.warn("[event={}] does not contain [bodyKey={}] -> ignored", event, UNREACHABLE_MEMBER_EVENT_BODY_KEY);
            }
            else {
                final String meAsMemberString = myAddressAsString();
                if(Objects.equals(meAsMemberString, unreachableMemberAsString)){
                    LOGGER.info("I [address={}] am NOT reachable by the rest of my cluster :-( -> disconnecting" +
                                " (you might consider restarting this instance)", meAsMemberString);
                    mayIProcessEvents = false;
                }
            }
        }
    }


    private String myAddressAsString() {
        return clusterReadView.self().address().toString();
    }


    @Override
    public void onMemberUp(final Member member) {
        if(! mayIProcessEvents){

            if(isItMe(member)){
                mayIProcessEvents = true;
                LOGGER.info("I am allowed to process events as my cluster can see me again :-)");
            }
        }
    }


    private boolean isItMe(final Member member){
        final String meAsMemberString = myAddressAsString();
        final String upMemberAddressAsString = member.address().toString();
        return Objects.equals(meAsMemberString, upMemberAddressAsString);
    }


    @Override
    public void onMemberRemoved(final Member member) {
        if(mayIProcessEvents && isItMe(member)){
            mayIProcessEvents = false;
            LOGGER.info("I have been removed from the cluster and therefore I refuse to process any events from now on" +
                        " -> you should consider restarting the instance");
        }
    }

    @Override
    public void onRebalanced(int partitionId, int numberOfPartitions) {
        // do nothing
    }
}
