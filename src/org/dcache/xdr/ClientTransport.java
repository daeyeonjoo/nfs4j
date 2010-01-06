/*
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.dcache.xdr;

import com.sun.grizzly.ConnectorHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ClientTransport implements XdrTransport {

    private final ConnectorHandler _connectorHandler;
    private final ReplyQueue<Integer, RpcReply> _replyQueue;

    public ClientTransport(ConnectorHandler connectorHandler ,
            ReplyQueue<Integer, RpcReply> replyQueue ) {
        _replyQueue = replyQueue;
        _connectorHandler = connectorHandler;
    }

    public void send(ByteBuffer data) throws IOException {
        _connectorHandler.write(data, true);
    }

    public InetSocketAddress getLocalSocketAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InetSocketAddress getRemoteSocketAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ReplyQueue<Integer, RpcReply> getReplyQueue() {
        return _replyQueue;
    }

}