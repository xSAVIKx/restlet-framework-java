/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.util;

import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Modifiable list of server connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ServerList extends WrapperList<Server> {

    /** The context. */
    private volatile Context context;

    /** The target Restlet of added servers. */
    private volatile Restlet target;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param target
     *            The target Restlet of added servers.
     */
    public ServerList(Context context, Restlet target) {
        super(new CopyOnWriteArrayList<Server>());
        this.context = context;
        this.target = target;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol.
     * 
     * @param protocol
     *            The connector protocol.
     * @return The added server.
     */
    public Server add(Protocol protocol) {
        final Server result = new Server(getContext().createChildContext(),
                protocol, null, protocol.getDefaultPort(), getTarget());
        add(result);
        return result;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol on
     * the specified port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param port
     *            The listening port.
     * @return The added server.
     */
    public Server add(Protocol protocol, int port) {
        final Server result = new Server(getContext().createChildContext(),
                protocol, null, port, getTarget());
        add(result);
        return result;
    }

    /**
     * Adds a new server connector in the map supporting the given protocol on
     * the specified IP address and port.
     * 
     * @param protocol
     *            The connector protocol.
     * @param address
     *            The optional listening IP address (useful if multiple IP
     *            addresses available).
     * @param port
     *            The listening port.
     * @return The added server.
     */
    public Server add(Protocol protocol, String address, int port) {
        final Server result = new Server(getContext().createChildContext(),
                protocol, address, port, getTarget());
        add(result);
        return result;
    }

    /**
     * Adds a server at the end of the list.
     * 
     * @return True (as per the general contract of the Collection.add method).
     */
    @Override
    public boolean add(Server server) {
        server.setTarget(getTarget());
        return super.add(server);
    }

    /**
     * Returns the context.
     * 
     * @return The context.
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Returns the target Restlet.
     * 
     * @return The target Restlet.
     */
    public Restlet getTarget() {
        return this.target;
    }

    /**
     * Sets the context.
     * 
     * @param context
     *            The context.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets the target Restlet.
     * 
     * @param target
     *            The target Restlet.
     */
    public void setTarget(Restlet target) {
        this.target = target;
    }

}
