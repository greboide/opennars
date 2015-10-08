package nars.io;

import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.tuple.Tuples;
import hellblazer.gossip.GossipPeer;
import nars.NAR;
import nars.util.event.DefaultTopic;
import nars.util.event.Topic;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import java.io.Serializable;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 10/7/15.
 */
public class StreamOperatorsTest {


    /** a UDP Gossip network interface (NIC) that provides a send and receive stream */
    public static class UDPNetwork<O extends Serializable>  /* implements Network */
        implements Consumer<O> {

        final GossipPeer peer;
        public final Topic<Pair<UUID,O>> in = new DefaultTopic<>();
        public final Topic<O> out = new DefaultTopic();

        public UDPNetwork(int port) throws SocketException {

            peer = new GossipPeer(port) {
                public final void onUpdate(UUID id, Object j) {
                    try {
                        in.emit( Tuples.pair(id, (O) j) );
                    }
                    catch (Exception e) {
                        peer.log(e);
                    }
                }
            };

            out.on(this);

        }

        public void setFrequency(float hz) {
            long ms = (long)(1000f/hz);
            peer.gossip.setInterval((int)ms, TimeUnit.MILLISECONDS);
        }

        @Override
        public final void accept(O o) {
            try {
                peer.put(o);
            }
            catch (Exception e) {
                peer.log(e);
            }
        }


        /** initialize a NAR with operators for this network */
        public void connect(NAR nar) {

        }
    }

    @Test
    public void testUDP() throws SocketException, InterruptedException {
        UDPNetwork<String> a = new UDPNetwork(10001);
        UDPNetwork<String> b = new UDPNetwork(10002);
        b.peer.connect("localhost", 10001);

        System.out.println("Testing UDP pair at 60hz");
        a.setFrequency(60);
        b.setFrequency(60);

        DescriptiveStatistics stat = new DescriptiveStatistics();

        Thread test = Thread.currentThread();

        a.in.on(aIn -> {
            //System.out.println(aIn);
            a.peer.put(aIn.getTwo());
        });
        b.in.on(m -> {
            long start = Long.valueOf(m.getTwo()).longValue();
            long now = System.currentTimeMillis();

            stat.addValue(now-start);

            test.interrupt();
        });


        int bursts = 10;
        int burstSize = 64;

        for (int i = 0; i < bursts; i++) {
            final long now = System.currentTimeMillis();
            for (int j = 0; j < burstSize; j++) {
                b.out.emit(String.valueOf(now));

                try {
                    Thread.sleep((long) ( 1000.0 / 60.0));
                } catch (Exception e) { }
            }

            System.out.println(stat);

        }



        assertTrue(stat.getN() > 0);

        System.out.println("average lag time: " +
                (stat.getMean() - (1000.0/60.0)) + "ms"
        );

        a.peer.stop();
        b.peer.stop();

    }

}
