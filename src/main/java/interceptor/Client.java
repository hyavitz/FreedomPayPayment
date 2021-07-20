package interceptor;

//package com.softpointdev.quickpoint.network.client;
//import com.softpointdev.quickpoint.network.client.pojo.NetCommand;

import java.io.Closeable;

import lombok.NonNull;

/**
 * Client interface represents the different ways that QuickPoint is able to communicate and get
 * commands on what to do.
 *
 * @author Joshua Monson - 11/25/2019
 */
public interface Client extends Runnable, Closeable {

    /**
     * Adds a listener to the set that will then get commands when they come in or are thrown
     *
     * @param listener The listener to add
     */
    Client addCommandHandler(@NonNull CommandListener listener);

    /**
     * Removes a listener from the list of active listeners
     *
     * @param listener The listener to remove
     */
    Client removeCommandHandler(@NonNull CommandListener listener);

    /**
     * Listens for commands from the client and handles them
     */
    interface CommandListener {

        /**
         * Handle a given command. The Client itself will start waiting for more commands after all
         * these are sent.
         *
         * @param command The command to handle
         */
        void handle(NetCommand command);
    }
}
