/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package net.tomp2p.examples;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.nat.PeerBuilderNAT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for testing with {@link TomP2PTests}
 */
public class SeedNodeForTesting {
    private static final Logger log = LoggerFactory.getLogger(SeedNodeForTesting.class);

    public static void main(String[] args) throws Exception {
        // Define your seed node IP and port
        // "127.0.0.1" for localhost or TomP2PTests.SEED_ID_WAN_1
        new SeedNodeForTesting().startSeedNode("localhost", 5000);
    }

    public Thread startSeedNode(final String seedNodeId, final int seedNodePort) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Peer peer = null;
                try {
                    peer = new PeerBuilder(Number160.createHash(seedNodeId)).ports(seedNodePort).start();
                    peer.objectDataReply(new ObjectDataReply() {
                        @Override
                        public Object reply(PeerAddress sender, Object request) throws Exception {
                            log.trace("received request: ", request.toString());
                            return "pong";
                        }
                    });

                    new PeerBuilderDHT(peer).start();
                    new PeerBuilderNAT(peer).start();

                    log.debug("SeedNode started.");
                    for (; ; ) {
                        for (PeerAddress pa : peer.peerBean().peerMap().all()) {
                            log.debug("peer online:" + pa);
                        }
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    if (peer != null)
                        peer.shutdown().awaitUninterruptibly();
                }
            }
        });
        thread.start();
        return thread;
    }

}
