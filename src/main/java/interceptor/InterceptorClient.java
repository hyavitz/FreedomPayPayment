package interceptor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.NonNull;
import network.TransactionLog;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * InterceptorClient handles talking back and forth with the QuickPoint servers hosted on all of the
 * SoftPoint Node and Primary Server. This also handles basic upkeep of itself and should basically
 * be the only one of itself. If a disconnect happens this class is in charge of reconnecting itself
 * as soon as possible without overloading or sending too many requests.
 * <p>
 * This class does all of its primary actions on its own thread. This includes the call of the
 * listeners. The listeners themselves are Weakly referenced only so a reference to each active one
 * must be kept or else it is likely that it will be collected and the listener no longer return
 * data.
 *
 * @author Joshua Monson - 11/25/2019
 */

public class InterceptorClient implements Client {

    private static InterceptorClient INSTANCE;
    private static final int port = 25534;
    private static final Gson GSON = new Gson();

    public static NetRegisterDevice register;

    private boolean shutdown = false;
    private long lastPing = System.currentTimeMillis();

    private volatile boolean external;

    public static final List<String> canceledList = new ArrayList<>(27);
    public static final List<String> receivedList = new ArrayList<>();

    private static final Map<CommandListener, Boolean> listenerList = Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile String currentWebhook = "";

    private ThreadPoolExecutor SLAVE_POOL = new ThreadPoolExecutor(1, 25, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private Thread thread;

    private static ResponseMessage responseMessage;

    @Deprecated
    public static InterceptorClient getInstance() {
        System.out.println("<6><>Creating InterceptorClient instance");
        if (INSTANCE == null) {
            System.out.println("<6.1><null>Creating InterceptorClient instance");
            synchronized (InterceptorClient.class) {
                if (INSTANCE == null) {
                    System.out.println("<6.2><null>Creating InterceptorClient instance");
                    INSTANCE = new InterceptorClient();
                }
            }
        }
        return INSTANCE;
    }

    @Deprecated
    public static void purge() {
        if (INSTANCE != null) {
            synchronized (InterceptorClient.class) {
                if (INSTANCE != null) {
                    INSTANCE.close();
                    INSTANCE = null;
                }
            }
        }
    }

    private InterceptorClient() {
    }

    public void start() {
        if (!isAlive()) {
            synchronized (InterceptorClient.class) {
                if (!isAlive()) {
                    shutdown = false;
                    thread = new Thread(this);
                    thread.start();
                } else {
                    thread.interrupt();
                }
            }
        }
    }

    private void checkAlive() {
        if (shutdown) {
            return;
        }
        if (!isAlive()) {
            start();
        } else if (lastPing < (System.currentTimeMillis() - 45000)) {
            close();
            start();
            return;
        } else if (lastPing < (System.currentTimeMillis() - 30000)) {
            thread.interrupt();
        }

        if (listenerList.size() < 1) {
            close();
        }

        try {
            Thread.sleep(30000);
            SLAVE_POOL.submit(this::checkAlive);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isAlive() {
        return thread != null && thread.isAlive();
    }

    @Override
    public InterceptorClient addCommandHandler(@NonNull CommandListener listener) {
        System.out.println("<7><>addCommandHandler with listener[" + listener + "]");
        if (!listenerList.containsKey(listener)) {
            System.out.println("<7.1><>adding as new listener");
            if (external) {
                setExternal(false);
            }
            listenerList.put(listener, true);
            System.out.println("<7.2><>canceledList.contains(currentWebhook) ?");
            if (canceledList.contains(currentWebhook)) {
                NetCommand cancel = new NetCommand().setCommand(NetCommand.Command.CANCEL);
                listener.handle(cancel);
            }
        }
        return this;
    }

    @Override
    public InterceptorClient removeCommandHandler(@NonNull CommandListener listener) {
        if (listenerList.size() > 1) {
            listenerList.remove(listener);
        }
        return this;
    }

    public synchronized void setExternal(boolean check) {
        external = check;
    }

    @Override
    public void run() {

        while (!shutdown) {
            try (Socket socket = connect()) {
                try {
                    assert socket != null;

                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));

                    for (CommandListener listener : listenerList.keySet()) {
                        listener.handle(new NetCommand().setCommand(NetCommand.Command.READY));
                    }

                    SLAVE_POOL.submit(() -> {
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        checkAlive();
                    });

                    boolean attemptingPing = false;

                    while (!shutdown) {

                        String data = getMessageFromServer(input);
                        NetCommand command = null;

                        if (canceledList.contains(currentWebhook)
                                && currentWebhook != null
                                && !currentWebhook.equals("")
                                && !currentWebhook.isEmpty()) {
                            receivedList.remove(currentWebhook);
                        }
                        currentWebhook = "";

                        try {

                            if (data != null) {

                                JSONObject jsonObject = new JSONObject(data);
                                System.out.println(data);

                                if (jsonObject.optString("command", "").equalsIgnoreCase("webhook")) {

                                    command = new NetCommand().setCommand(NetCommand.Command.SALE);
                                    command.setResponseCode(jsonObject.optInt("ResponseCode", 0));
                                    command.setResponseMessage(jsonObject.optJSONObject("ResponseMessage").toString());

                                    try {
                                        responseMessage = GSON.fromJson(command.getResponseMessage(), ResponseMessage.class);
                                    } catch (Exception e) {
                                    }

                                    // If our canceled list does NOT yet contain this non-null webhook, we need to receive it -->
                                    if (!canceledList.contains(String.valueOf(responseMessage.getUnique_webhook_id()))
                                            && !(String.valueOf(responseMessage.getUnique_webhook_id()).equals(""))
                                            && !(String.valueOf(responseMessage.getUnique_webhook_id()).isEmpty())
                                            && (String.valueOf(responseMessage.getUnique_webhook_id()) != null)) {

                                        // This webhook should have already been received -->
                                        System.out.println("Old webhook: " + currentWebhook + " is being replaced.");
                                        currentWebhook = String.valueOf(responseMessage.getUnique_webhook_id());
                                        System.out.println("Current webhook: " + currentWebhook);

                                        // First time around, these should be empty -->
                                        System.out.println("Total received: " + receivedList);
                                        System.out.println("Total canceled: " + canceledList);

                                    } else {
                                        command = null;
                                    }

                                    // If our received list does NOT yet contain this non-null webhook, we need to add it
                                    if (!receivedList.contains(currentWebhook)
                                            && currentWebhook != null
                                            && !currentWebhook.equals("")
                                            && !currentWebhook.isEmpty()) {
                                        System.out.println("Receiving...");
                                        sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(currentWebhook));
                                        receivedList.add(currentWebhook);
                                        System.out.println("Receiving webhook: " + currentWebhook);
                                        System.out.println("Total received: " + receivedList);
                                    } else {
                                        System.out.println("Waiting...");
                                        sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));
                                        System.out.println("Receiving webhook: " + currentWebhook);
                                        System.out.println("Total received: " + receivedList);
                                        command = new NetCommand().setCommand(NetCommand.Command.PING);
                                        command.setResponseCode(jsonObject.optInt("ResponseCode", 0));
                                        command.setResponseMessage(jsonObject.optJSONObject("ResponseMessage").toString());
                                    }


                                } else if (jsonObject.optString("ResponseMessage", "").equalsIgnoreCase("New Connection")) {
                                    return;

                                } else {
                                    System.out.println("Waiting...");
                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));

                                    try {
                                        System.out.println("Pinging...");
                                        command = GSON.fromJson(data, NetCommand.class);
                                        command.setCommand(NetCommand.Command.PING);

                                    } catch (Exception e) {
                                        System.out.println("Catching...");
                                    }
                                }
                            }

                        } catch (Exception e) {
                            System.out.println("Catching...");
                        }

                        if (command != null) {
                            if (attemptingPing) {
                                attemptingPing = false;
                            }

                            switch (command.getCommand()) {

                                case WAITING:
                                case FAILED:
                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.FAILED));
                                    break;

                                case PING:
                                    lastPing = System.currentTimeMillis();
                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.PONG));

                                    NetCommand ping = new NetCommand().setCommand(NetCommand.Command.PING);
                                    for (CommandListener listener : listenerList.keySet()) {
                                        listener.handle(ping);
                                    }
                                    break;

                                case PONG:
                                    lastPing = System.currentTimeMillis();
                                    break;

                                case CANCEL:
                                    System.out.println("Canceling...");

                                    NetCommand cancel = new NetCommand().setCommand(NetCommand.Command.CANCEL);

                                    if (!cancel.getWebhookId().isEmpty()) {
                                        canceledList.add(cancel.getWebhookId());
                                    } else {
                                        canceledList.add(currentWebhook);
                                    }

                                    if (canceledList.size() > 25) {
                                        canceledList.remove(0);
                                    }

                                    for (CommandListener listener : listenerList.keySet()) {
                                        listener.handle(cancel);
                                    }
                                    break;

                                case KILL:
                                case SYNC:
                                    for (CommandListener listener : listenerList.keySet()) {
                                        listener.handle(command);
                                    }
                                    break;

                                case RECEIPT:
                                    try {
                                        System.out.println("CASE RECEIPT");
                                        sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(command.getUniqueId()));
                                        System.out.println("Receiving " + command.getUniqueId());
                                        for (CommandListener listener : listenerList.keySet()) {
                                            listener.handle(command);
                                        }

                                    } catch (Exception e) {
                                        System.out.println("Catching...");
                                    }
                                    break;

                                case TOKEN:
                                case PRE_AUTH:
                                case POST_AUTH:
                                case VOID:
                                case REFUND:
                                case SALE:
                                    System.out.println("IC TRANSACTION START?");
                                    TransactionLog.generateLog("Start Transaction", "Interceptor Client");

                                default:
//                                    System.out.println("Receiving...");
//                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(currentWebhook));
                                    System.out.println("Waiting...");
                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));

                                    for (CommandListener listener : listenerList.keySet()) {
                                        NetCommand finalCommand = command;
                                        new Thread(() -> listener.handle(finalCommand)).start();
                                    }

                                    try {
                                        JSONObject temp = new JSONObject(command.getResponseMessage());

                                        NetCommand clean = new NetCommand().setCommand(NetCommand.Command.CANCEL);
                                        for (CommandListener listener : listenerList.keySet()) {
                                            listener.handle(clean);
                                        }

                                        if (!canceledList.contains(temp.optString("unique_webhook_id", ""))) {
                                            System.out.println("IC TRANSACTION FINISH?");
                                            TransactionLog.generateLog("Finish Transaction", "Interceptor Client");
//                                            System.out.println("Receiving...");
//                                            sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED)
//                                                    .setUniqueId(temp.optString("unique_webhook_id", "")));
                                            System.out.println("Assigning new value to currentWebhook...");
                                            currentWebhook = temp.optString("unique_webhook_id", "");
                                            System.out.println("Current webhook: " + currentWebhook);
                                            System.out.println("Adding webhook to canceled: " + currentWebhook);
                                            canceledList.add(currentWebhook);
                                            System.out.println("Canceled list contains: " + canceledList);
                                        }

                                    } catch (Exception e) {
                                        System.out.println("Catching...");
                                    }
                                    break;
                            }

                            try {
                                Thread.sleep(2000);

                            } catch (Exception ignore) {
                                System.out.println("Catching...");
                            }

                        } else {

                            if (!attemptingPing) {
                                attemptingPing = true;
                                socket.setSoTimeout(5000);
                                sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.PING));
                                socket.setSoTimeout(45000);

                            } else {
                                System.out.println("Elsing...");
                                break;
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Catching...");
                }

            } catch (Exception e) {
                System.out.println("Catching...");
            }

            try {
                Thread.sleep(3000);

            } catch (Exception e) {
                System.out.println("Catching...");
            }
        }
    }

    @Override
    public void close() {
        shutdown = true;
        SLAVE_POOL.shutdownNow();

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }

        SLAVE_POOL = new ThreadPoolExecutor(1, 25, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * Attempt to connect from iteration of available servers
     *
     * @return The opened socket if successful, else null
     */
    private Socket connect() {

        // TODO: Supply live servers
        List<String> servers = new ArrayList<>();
        servers.add("softpointdev.com");
        servers.add("softpointdev.com");
        servers.add("softpointdev.com");

        for (String host : servers) {
            Socket socket = connectServer(host);
            if (socket != null) {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    if (registerDevice(socket, out, in)) {
                        return socket;
                    }

                } catch (Exception e) {
                    return null;
                }
            }
        }
        throw new NullPointerException("\nSocket Connection Failed");
    }

    /**
     * Attempts to open a socket connection to the given server on the given port. The timeout for
     * this is 5 seconds.
     *
     * @param server The server address to attempt to connect to
     * @return The opened socket if successful, else null
     */
    private Socket connectServer(String server) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(server, InterceptorClient.port), 5000);
            return socket;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Attempts to register a device with the server we have connected to. If this fails the connect
     * method moves onto the next server and tries again. This is part of the connect process due to
     * this also being the main check to determine if the server is actually running correctly and
     * only being needed once when the device connects.
     *
     * @param out The output for sending message
     * @param in  The input for getting messages
     * @return True iff we manged to successfully register the device on the server, else false
     */
    boolean registerDevice(Socket socket, PrintWriter out, BufferedReader in) throws SocketException {

        socket.setSoTimeout(5000);
        sendMessageToServer(out, register);
        BasicResponse data = getMessageFromServer(in, BasicResponse.class);
        socket.setSoTimeout(45000);

        if (data != null) {
            lastPing = System.currentTimeMillis();
            return data.getResponseCode() == 1;
        }
        return false;
    }

    /**
     * Converts an object to a JSON string and forwards that string to #sendMessageToServer(String)
     *
     * @param out  The PrintWriter to write the message to
     * @param message The object to send
     */
    private void sendMessageToServer(@NonNull PrintWriter out, @NonNull Object message) {
        sendMessageToServer(out, GSON.toJson(message));
    }

    /**
     * Sends a given message to the given PrintWriter. This method mainly is used after an Object is
     * convert into JSON instead of having conversion code all over.
     *
     * @param out  The PrintWriter to write the message to
     * @param message The object to send
     */
    private void sendMessageToServer(@NonNull PrintWriter out, @NonNull String message) {
        out.println(message);
    }

    /**
     * Get message from server and cast it to requested object
     *
     * @param in The Reader to read from
     * @param klass The class we are attempting to parse the data to
     * @param <E>   Return type of the class we are parsing to
     * @return An object of the requested type if successful or null
     */
    private <E> E getMessageFromServer(@NonNull BufferedReader in, @NonNull Class<E> klass) {
        try {
            return GSON.fromJson(getMessageFromServer(in), klass);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * Get message from server as string
     *
     * @param in The Reader to read from
     * @return The String returned from the server
     */
    private String getMessageFromServer(@NonNull BufferedReader in) {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}