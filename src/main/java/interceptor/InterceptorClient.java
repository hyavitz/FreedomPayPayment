package interceptor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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

import lombok.NonNull;

/**
 * InterceptorClient handles talking back and forth with the QuickPoint servers hosted on all of the
 * SoftPoint Node and Primary Server. This also handles basic upkeep of itself and should basically
 * be the only one of itself. If a disconnect happens this class is in charge of reconnecting itself
 * as soon as possible without overloading or sending to many requests.
 *
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

    public static NetRegisterDevice netRegisterDevice;

    private boolean shutdown = false;
    private long lastPing = System.currentTimeMillis();

    private volatile boolean external;

    private List<String> canceledList = new ArrayList<>(27);

    private Thread thread;
    private ThreadPoolExecutor SLAVE_POOL = new ThreadPoolExecutor(1, 25, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static volatile Map<CommandListener, Boolean> listenerList = Collections.synchronizedMap(new HashMap<>());
    private static volatile String currentWebhook = "";

    private InterceptorClient() {}

    @Deprecated
    public static InterceptorClient getInstance() {
        if (INSTANCE == null) {
            synchronized (InterceptorClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InterceptorClient();
                }
            }
        }
        return INSTANCE;
    }

    public void start() {
        System.out.println("starting ic");
        if(!isAlive()) {
            synchronized (InterceptorClient.class) {
                if (!isAlive()) {
                    shutdown = false;
                    thread = new Thread(this);
                    thread.start();
                } else {
                }
            }
        }
    }

    private void checkAlive() {
        if(shutdown) {
            return;
        }

        if(!isAlive()) { // If the thread isn't alive we need to start it up again
            start();
        } else if(lastPing < (System.currentTimeMillis() - 45000)) { // If we have not gotten a ping in 45 seconds we close down and restart, its not worth trying to ping as it needs a restart more then likely
            close();
            start();
            return;
        } else if(lastPing < (System.currentTimeMillis() - 30000)) { // If we have not gotten a ping in some time we need to ping, this should force it
            thread.interrupt();
        }

        try {
            Thread.sleep(30000);
            SLAVE_POOL.submit(this::checkAlive);
        } catch (InterruptedException e) {
            // If we are interrupted we assume we are finished and should not try and start ourselves again..
            e.printStackTrace();
        }
    }

    // TODO: check if we have gotten a response in the last 45 seconds...
    public boolean isAlive(){
        return thread != null && thread.isAlive();
    }

    @Override
    public InterceptorClient addCommandHandler(@NonNull CommandListener listener) {
        System.out.println("adding command listener to ic");
        if(!listenerList.containsKey(listener)) {
            if(external) {
                setExternal(false);
            }
            listenerList.put(listener, true);

            // We send this automatically if we are in a cancel state.
            if(canceledList.contains(currentWebhook)) {
                NetCommand cancel = new NetCommand().setCommand(NetCommand.Command.CANCEL);
                listener.handle(cancel);
            }
        }
        System.out.println("returning");
        return this;
    }

    @Override
    public InterceptorClient removeCommandHandler(@NonNull CommandListener listener) {

        // only allow removal when there is more than one listener
        if(listenerList.size() > 1) {
            if (listenerList.remove(listener) == null) {
            } else {
            }
        }
        return this;
    }

    public synchronized void setExternal(boolean check) {
        external = check;
    }

    @Override
    public void run() {
        System.out.println("running");

        while(!shutdown) {

            System.out.println("not shut down");
            try(Socket socket = connect()) {

                System.out.println("connected socket");
                try {

                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));

                    for (CommandListener listener: listenerList.keySet()) {
                        listener.handle(new NetCommand().setCommand(NetCommand.Command.READY));
                    }

                    SLAVE_POOL.submit(() -> {
                        try {
                            Thread.sleep(45000);

                        } catch (InterruptedException e) {
                            // If we are interrupted we assume we are finished and should not try and start ourselves again..
                            e.printStackTrace();
                        }
                        checkAlive();
                    });

                    boolean attemptingPing = false;
                    while (!shutdown) {
                        System.out.println("data");

                        String data = getMessageFromServer(input);
                        NetCommand command = null;

                        try {
                            System.out.println("START");
                            if(data != null) {
                                JSONObject json = new JSONObject(data);
                                System.out.println("\tHere's some data from the server > " + data + " <");
                                System.out.println("\tHere's our current webhook: > " + currentWebhook + " <" );
                                System.out.println("\tHere's our canceled list: " + canceledList);
                                if (json.optString("command", "").equalsIgnoreCase("webhook") || json.optString("command", "").equalsIgnoreCase("pre_auth")) {
                                    command = new NetCommand().setCommand(NetCommand.Command.SALE);
                                    System.out.println("GOT ME A WEBHOOK!");
                                    if(json.optString("command", "").equalsIgnoreCase("pre_auth")) {
                                        command = new NetCommand().setCommand(NetCommand.Command.PRE_AUTH);
                                    }
                                    command.setResponseCode(json.optInt("ResponseCode", 0));
                                    command.setResponseMessage(json.optJSONObject("ResponseMessage").toString());
                                } else if (json.optString("ResponseMessage", "").equalsIgnoreCase("New Connection")) {
                                    return;
                                } else {
                                    command = GSON.fromJson(data, NetCommand.class);
                                }
                            }

                        } catch (Exception e) {
                            // TODO: review the below instead of doing this in a cathc\
                            //  https://stackoverflow.com/questions/31758872/how-to-handle-different-data-types-with-same-attribute-name-with-gson

                            /*JSONObject json = new JSONObject(data);

                            command = new NetCommand().setCommand(NetCommand.Command.SALE);

                            command.setResponseCode(json.optInt("ResponseCode", 0));
                            command.setResponseMessage(json.optJSONObject("ResponseMessage").toString());*/
                        }
                        if(command != null) {
                            if(attemptingPing) {
                                attemptingPing = false;
                            }

                            switch (command.getCommand()) {

                                // These are current transaction commands that are currently marked as failed
                                case WAITING:
                                    System.out.println("WAITING");
                                case FAILED:
                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.FAILED));
                                    break;

                                // A message from the server to make sure that the device is still active and getting the requests
                                case PING:
                                    System.out.println("PING");
                                    lastPing = System.currentTimeMillis();
                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.PONG));

                                    NetCommand ping = new NetCommand().setCommand(NetCommand.Command.PING);
                                    for (CommandListener listener : listenerList.keySet()) {
                                        listener.handle(ping);
                                    }

                                    break;

                                // We can mostly ignore pong messages as we do not need to respond to these messages
                                case PONG:
                                    System.out.println("PONG");
                                    lastPing = System.currentTimeMillis();
                                    break;

                                case CANCEL:

                                    NetCommand cancel = new NetCommand().setCommand(NetCommand.Command.CANCEL);
                                    if(!cancel.getWebhookId().isEmpty()) {
                                        canceledList.add(cancel.getWebhookId());
                                        if(canceledList.size() > 25) {
                                            canceledList.remove(0);
                                        }
                                    } else {
                                        canceledList.add(currentWebhook);
                                        if(canceledList.size() > 25) {
                                            canceledList.remove(0);
                                        }
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
                                        sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(command.getUniqueId()));

                                        for (CommandListener listener : listenerList.keySet()) {
                                            listener.handle(command);
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    break;

                                case PRE_AUTH:

                                    try {
                                        NetCommand sendCommand = command;
                                        command.setCommand(NetCommand.Command.PRE_AUTH);
                                        //final NetCommand sendCommand = new NetCommand().setCommand(NetCommand.Command.PRE_AUTH);

                                        try {
                                            JSONObject temp = new JSONObject(sendCommand.getResponseMessage());

                                            // The webhook is already running, so we can ignore this one.
                                            if(!currentWebhook.isEmpty() && currentWebhook.equalsIgnoreCase(temp.optString("unique_webhook_id", ""))) {
                                                break;
                                            }

                                            NetCommand clean = new NetCommand().setCommand(NetCommand.Command.CANCEL);
                                            for (CommandListener listener : listenerList.keySet()) {
                                                listener.handle(clean);
                                            }

//                                            boolean found = false;

//                                            for(int i = 0; i < 10; i++) {
//                                                if(containsListener(listenerList, IdleActivity.class)
//                                                        || containsDevicePaymentListener(listenerList) && (getSessionManager().getDevice() == DeviceType.EXADIGM && i == 9)) {
//                                                    found = true;
//                                                    break;
//                                                }
//
//                                                try {
//                                                    Thread.sleep(1000);
//                                                } catch (Exception ignore) {
//
//                                                }
//                                            }

//                                            if(!found) {
//                                                // device is busy so fuck off...
//                                                return;
//                                            }

                                            if(!canceledList.contains(temp.optString("unique_webhook_id", ""))) {
                                                sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(temp.optString("unique_webhook_id", "")));

                                                currentWebhook = temp.optString("unique_webhook_id", "");
                                                for (CommandListener listener : listenerList.keySet()) {
                                                    listener.handle(sendCommand);
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        //});

                                    } catch (Exception e) {
                                    }
                                    break;

                                case TOKEN:
                                case POST_AUTH:
                                case VOID:
                                case REFUND:
                                case SALE:
                                    System.out.println("SALE-ING!");


                                default:
                                    try {

                                        final NetCommand sendCommand = command;
                                        System.out.println("This command should be: > " + command.getCommand() + " <");

                                        //MASTER_POOL.submit(() -> {

                                        try {
                                            JSONObject temp = new JSONObject(sendCommand.getResponseMessage());

                                            // The webhook is already running, so we can ignore this one.
                                            if(!currentWebhook.isEmpty() && currentWebhook.equalsIgnoreCase(temp.optString("unique_webhook_id", ""))) {
                                                System.out.println("currentWebhook is ignored: > " + currentWebhook + " <");
                                                break;
                                            }
                                            System.out.println("currentWebhook not ignored: > " + currentWebhook + " <");

                                            NetCommand clean = new NetCommand().setCommand(NetCommand.Command.CANCEL);
                                            System.out.println("ListenerList is: >" + listenerList + " <");
                                            for (CommandListener listener : listenerList.keySet()) {
                                                System.out.println("Cleaning the listener");
                                                listener.handle(clean);
                                            }

//                                            boolean found = false;
//                                            for(int i = 0; i < 10; i++) {
//                                                if(containsListener(listenerList, IdleActivity.class)
//                                                        || containsDevicePaymentListener(listenerList) && (getSessionManager().getDevice() == DeviceType.EXADIGM && i == 9)) {
//                                                    found = true;
//                                                    break;
//                                                }
//
//                                                try {
//                                                    Thread.sleep(1000);
//                                                } catch (Exception ignore) {
//
//                                                }
//                                            }
//
//                                            if(!found) {
//                                                // device is busy so fuck off...
//                                                return;
//                                            }

                                            if(!canceledList.contains(temp.optString("unique_webhook_id", ""))) {
                                                sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(temp.optString("unique_webhook_id", "")));

                                                currentWebhook = temp.optString("unique_webhook_id", "");
                                                System.out.println("About to handle our tish");
                                                for (CommandListener listener : listenerList.keySet()) {
                                                    System.out.println("handling...");
                                                    listener.handle(sendCommand);
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        //});

                                    } catch (Exception e) {
                                    }
                                    break;
                            }

                            // Delay 1.5 seconds
                            try {
                                Thread.sleep(1500);
                            } catch (Exception ignore) {

                            }
                            // Lets just assume we are waiting...
                            sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));

                        } else {

                            if (!attemptingPing) {
                                attemptingPing = true;
                                socket.setSoTimeout(5000);
                                sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.PING));
                                socket.setSoTimeout(45000);
                            } else {
                                break;
                            }
                        }
                        System.out.println("END");
                        System.out.println("\tHere's some data from the server > " + data + " <");
                        System.out.println("\tHere's our current webhook: > " + currentWebhook + " <" );
                        System.out.println("\tHere's our canceled list: " + canceledList);
                    }
                } catch (Exception e) {
                }
            } catch (Exception e) {
            }

            // Wait for 2.5 seconds before going again.
            try {
                Thread.sleep(2500);
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public void close() {
        shutdown = true;
        SLAVE_POOL.shutdownNow();

        if(thread != null) {
            thread.interrupt();
            thread = null;
        }

        SLAVE_POOL = new ThreadPoolExecutor(1, 25, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * Loop through the different servers and attempt to connect
     *
     * @return The opened socket if successful, else null
     */
    @NonNull
    private Socket connect() {


        System.out.println("connecting socket");
        List<String> quickpointServers = new ArrayList<>();
        quickpointServers.add("softpointdev.com");

//        if (!getSessionManager().getPrimaryServer().contains("lb.softpointcloud.com") && !getSessionManager().getPrimaryServer().contains("lbdev.softpointcloud.com")) {
//            quickpointServers.add(getSessionManager().getPrimaryServer());
//            quickpointServers.add(getSessionManager().getPrimaryServer());
//        }
//
//        if(!TextUtils.isEmpty(BuildConfig.PRIMARY_LB_SERVER_HOST)) {
//            quickpointServers.add(BuildConfig.PRIMARY_LB_SERVER_HOST);
//            quickpointServers.add(BuildConfig.PRIMARY_LB_SERVER_HOST);
//        }
//
//        Collections.addAll(quickpointServers, BuildConfig.SERVER_HOST);

        for(String host: quickpointServers) {
            Socket socket = connectServer(host, port);
            System.out.println("is socket null");
            if(socket != null) {
                System.out.println("nope");
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    if (registerDevice(socket, out, input)) {
                        System.out.println("returning socket");
                        return socket;
                    }
                } catch (IOException e) {
                }
            }
        }

        throw new NullPointerException("Socket unable to connect; socket null");
    }

    /**
     * Attempts to open a socket connection to the given server on the given port. The timeout for
     * this is 5 seconds.
     *
     * @param server The server address to attempt to connect to
     * @param port The port to attempt to connect to
     *
     * @return The opened socket if successful, else null
     */
    private Socket connectServer(String server, int port) {
//        if(getStaticContext().haveNetworkConnection()) {
//            Timber.v("Attempting Server Connection: %s:%d", server, port);
//
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(server, port), 5000);

            return socket;
        } catch (IOException e) {
        }
//        }
        return null;
    }

    /**
     * Attempts to register a device with the server we have connected to. If this fails the connect
     * method moves onto the next server and tries again. This is part of the connect process due to
     * this also being the main check to determine if the server is actually running correctly and
     * only being needed once when the device connects.
     *
     * @param output The output for sending message
     * @param input Thie input for getting messages
     *
     * @return True iff we manged to successfully register the device on the server, else false
     */
    private boolean registerDevice(Socket socket, PrintWriter output, BufferedReader input) throws SocketException {

//        netRegisterDevice = new NetRegisterDevice()
//                .setLocationId("10009")
//                .setDevice("PAX")
//                .setSerialNumber("53096522")
//                .setApplication("Focus")
//                .setVersion(1)
//                .setVersionName("1")
//                .setServerVersion(2L);

        socket.setSoTimeout(5000);
        sendMessageToServer(output, netRegisterDevice);
        BasicResponse data = getMessageFromServer(input, BasicResponse.class);
        socket.setSoTimeout(45000);

        if(data != null) {
            lastPing = System.currentTimeMillis();
            return data.getResponseCode() == 1;
        }

        return false;
    }

    /**
     * Converts an object to a JSON string and forwards that string to #sendMessageToServer(String)
     *
     * @param output The PrintWriter to write the message to
     * @param message The object to send
     */
    private void sendMessageToServer(@NonNull PrintWriter output, @NonNull Object message) {
        System.out.println("sending message to server 1");
        sendMessageToServer(output, GSON.toJson(message));
    }

    /**
     * Sends a given message to the given PrintWriter. This method mainly is used after an Object is
     * convert into JSON instead of having conversion code all over.
     *
     * @param output The PrintWriter to write the message to
     * @param message The object to send
     */
    private void sendMessageToServer(@NonNull PrintWriter output, @NonNull String message) {

        System.out.println("sending message to server 2");
        output.println(message);
    }

    /**
     * Gets a message from the Server and parses it to be the requested object.
     *
     * @param input The Reader to read from
     * @param klass The class we are attempting to parse the data to
     * @param <E> Return type of the class we are parsing to
     *
     * @return An object of the requested type if successful or null
     */
    private <E> E getMessageFromServer(@NonNull BufferedReader input, @NonNull Class<E> klass) {
        System.out.println("getting message from server 1");
        try {
            return GSON.fromJson(getMessageFromServer(input), klass);
        } catch (JsonSyntaxException e) {
        }
        return null;
    }

    /**
     * Gets a message from the Server as a string.
     *
     * @param input The Reader to read from
     *
     * @return The String returned from the server
     */
    private String getMessageFromServer(@NonNull BufferedReader input) {
        System.out.println("getting message from server 2");
        try {
            String message = input.readLine();
            return message;
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Checks if the list of listeners contains a specific listener class
     *
     * @return true if the listener is contained within the class
     */
    private boolean containsListener(Map<CommandListener, Boolean> commandList, Class<?> klass) {
        for(CommandListener liste: commandList.keySet()) {
            if(liste.getClass() == klass) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the list of listeners contains instance of Device Payment Activity
     *
     * @return true if the listener is contained within the class
     */
    private boolean containsDevicePaymentListener(Map<CommandListener, Boolean> commandList){
        for(CommandListener list: commandList.keySet()) {
//            if(list.getClass() == AMPDevicePaymentActivity.class
//                    || list.getClass() == BPPOSDevicePaymentActivity.class
//                    || list.getClass() == CloverDevicePaymentActivity.class
//                    || list.getClass() == ExaDigmDevicePaymentActivity.class
//                    || list.getClass() == PAXDevicePaymentActivity.class
//                    || list.getClass() == PaxPayAnywhereDevicePaymentActivity.class) {
//                return true;
//            }
        }
        return false;
    }
}