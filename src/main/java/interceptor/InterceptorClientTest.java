//package interceptor;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;
//import lombok.NonNull;
//import network.TransactionLog;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.net.SocketException;
//import java.util.*;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
///**
// * InterceptorClient handles talking back and forth with the QuickPoint servers hosted on all of the
// * SoftPoint Node and Primary Server. This also handles basic upkeep of itself and should basically
// * be the only one of itself. If a disconnect happens this class is in charge of reconnecting itself
// * as soon as possible without overloading or sending too many requests.
// * <p>
// * This class does all of its primary actions on its own thread. This includes the call of the
// * listeners. The listeners themselves are Weakly referenced only so a reference to each active one
// * must be kept or else it is likely that it will be collected and the listener no longer return
// * data.
// *
// * @author Joshua Monson - 11/25/2019
// */
//
//public class InterceptorClient implements Client {
//
//    private static InterceptorClient INSTANCE;
//    private static final int port = 25534;
//    private static final Gson GSON = new Gson();
//
//    public static NetRegisterDevice register;
//
//    private boolean shutdown = false;
//    private long lastPing = System.currentTimeMillis();
//
//    public static final List<String> canceledList = new ArrayList<>(27);
//    public static final List<String> receivedList = new ArrayList<>();
//
//    private static final Map<CommandListener, Boolean> listenerList = Collections.synchronizedMap(new WeakHashMap<>());
//    private static volatile String currentWebhook = "";
//
//    private ThreadPoolExecutor SLAVE_POOL = new ThreadPoolExecutor(1, 25, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
//    private Thread thread;
//
//    private static ResponseMessage responseMessage;
//
//    @Deprecated
//    public static InterceptorClient getInstance() {
//        if (INSTANCE == null) {
//            synchronized (InterceptorClient.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = new InterceptorClient();
//                }
//            }
//        }
//        return INSTANCE;
//    }
//
////    @Deprecated
////    public static void purge() {
////        if (INSTANCE != null) {
////            synchronized (InterceptorClient.class) {
////                if (INSTANCE != null) {
////                    INSTANCE.close();
////                    INSTANCE = null;
////                }
////            }
////        }
////    }
//
//    private InterceptorClient() {}
//
//    public void start() {
//        if (!isAlive()) {
//            synchronized (InterceptorClient.class) {
//                if (!isAlive()) {
//                    System.out.println("We called start!");
//                    shutdown = false;
//                    thread = new Thread(this);
//                    thread.start();
//                } else {
//                    System.out.println("Already started?");
//                    thread.interrupt();
//                }
//            }
//        }
//    }
//
//    private void checkAlive() {
//        if (shutdown) {
//            return;
//        }
//        if (!isAlive()) {
//            start();
//        } else if (lastPing < (System.currentTimeMillis() - 45000)) {
//            close();
//            start();
//            return;
//        } else if (lastPing < (System.currentTimeMillis() - 30000)) {
//            thread.interrupt();
//        }
//
//        if (listenerList.size() < 1) {
//            close();
//        }
//
//        try {
//            Thread.sleep(30000); // TODO: Find out why this keeps getting interrupted
//            SLAVE_POOL.submit(this::checkAlive);
//        } catch (InterruptedException e) {
//            System.out.println("<><>" + e.getMessage() + "<><>");
//        }
//    }
//
//    public boolean isAlive() {
//        return thread != null && thread.isAlive();
//    }
//
//    @Override
//    public InterceptorClient addCommandHandler(@NonNull CommandListener listener) {
//        if (!listenerList.containsKey(listener)) {
//            listenerList.put(listener, true);
//            if (canceledList.contains(currentWebhook)) {
//                NetCommand cancel = new NetCommand().setCommand(NetCommand.Command.CANCEL);
//                listener.handle(cancel);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public InterceptorClient removeCommandHandler(@NonNull CommandListener listener) {
//        if (listenerList.size() > 1) {
//            listenerList.remove(listener);
//        }
//        return this;
//    }
//
//    @Override
//    public void run() {
//
//        while (!shutdown) {
//            try (Socket socket = connect()) {
//                System.out.println("socket connect -> " + socket);
//                try {
//                    assert socket != null;
//                    System.out.println("socket is not null");
//
//                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
//                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                    System.out.println("PrintWriter and BufferedReader: " + output + ":" + input);
//
//                    socket.setSoTimeout(5000);
//                    System.out.println("START to send message to server - WAITING");
//                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));
//                    String response = getMessageFromServer(input);
//                    System.out.println("And the answer is: " + response);
//                    socket.setSoTimeout(45000);
//                    System.out.println("FINISH send message to server - WAITING");
//
//                    for (CommandListener listener : listenerList.keySet()) {
//                        System.out.println("handling new command READY");
//                        listener.handle(new NetCommand().setCommand(NetCommand.Command.READY));
//                    }
//
//                    // Every 60 seconds -> checkAlive()
//                    SLAVE_POOL.submit(() -> {
//                        System.out.println("Submit 60 sleep");
//                        try {
//                            System.out.println("Call 60 sleep");
//                            Thread.sleep(60000);
//                        } catch (InterruptedException e) {
//                            System.out.println("Interrupt 60 sleep");
//                        }
//                        System.out.println("Finish 60 sleep");
//                        checkAlive();
//                    });
//
//                    boolean attemptingPing = false;
//
//                    while (!shutdown) {
//
//                        System.out.println("Hey server, you got anything?");
//                        String data = getMessageFromServer(input);
//                        System.out.println("got some data here, it's: " + data);
//                        NetCommand command = null;
//
//                        if (canceledList.contains(currentWebhook)
//                                && currentWebhook != null
//                                && !currentWebhook.equals("")
//                                && !currentWebhook.isEmpty()) {
//                            receivedList.remove(currentWebhook);
//                        }
//                        currentWebhook = "";
//
//                        try {
//
//                            if (data != null) {
//
//                                JSONObject jsonObject = new JSONObject(data);
//                                System.out.println(data);
//
//                                if (jsonObject.optString("command", "").equalsIgnoreCase("webhook")) {
//
//                                    command = new NetCommand().setCommand(NetCommand.Command.SALE);
//                                    command.setResponseCode(jsonObject.optInt("ResponseCode", 0));
//                                    command.setResponseMessage(jsonObject.optJSONObject("ResponseMessage").toString());
//
//                                    try {
//                                        responseMessage = GSON.fromJson(command.getResponseMessage(), ResponseMessage.class);
//                                    } catch (Exception e) {
//                                    }
//
//                                    // If our canceled list does NOT yet contain this non-null webhook, we need to receive it -->
//                                    if (!canceledList.contains(String.valueOf(responseMessage.getUnique_webhook_id()))
//                                            && !(String.valueOf(responseMessage.getUnique_webhook_id()).equals(""))
//                                            && !(String.valueOf(responseMessage.getUnique_webhook_id()).isEmpty())
//                                            && (String.valueOf(responseMessage.getUnique_webhook_id()) != null)) {
//
//                                        // This webhook should have already been received -->
//                                        System.out.println("Old webhook: " + currentWebhook + " is being replaced.");
//                                        currentWebhook = String.valueOf(responseMessage.getUnique_webhook_id());
//                                        System.out.println("Current webhook: " + currentWebhook);
//
//                                        // First time around, these should be empty -->
//                                        System.out.println("Total received: " + receivedList);
//                                        System.out.println("Total canceled: " + canceledList);
//
//                                    } else {
//                                        command = null;
//                                    }
//
//                                    // If our received list does NOT yet contain this non-null webhook, we need to add it
//                                    if (!receivedList.contains(currentWebhook)
//                                            && currentWebhook != null
//                                            && !currentWebhook.equals("")
//                                            && !currentWebhook.isEmpty()) {
//                                        socket.setSoTimeout(5000);
//                                        System.out.println("About to send message to server - RECEIVING...");
//                                        sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(currentWebhook));
//                                        String response1 = getMessageFromServer(input);
//                                        System.out.println("And the answer is: " + response1);
//                                        socket.setSoTimeout(45000);
//                                        receivedList.add(currentWebhook);
//                                        System.out.println("Receiving webhook: " + currentWebhook);
//                                        System.out.println("Total received: " + receivedList);
//                                    } else {
//                                        socket.setSoTimeout(5000);
//                                        System.out.println("About to send message to server - WAITING...");
//                                        sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));
//                                        String response2 = getMessageFromServer(input);
//                                        System.out.println("And the answer is: " + response2);
//                                        socket.setSoTimeout(45000);
//                                        System.out.println("Receiving webhook: " + currentWebhook);
//                                        System.out.println("Total received: " + receivedList);
//                                        command = new NetCommand().setCommand(NetCommand.Command.PING);
//                                        command.setResponseCode(jsonObject.optInt("ResponseCode", 0));
//                                        command.setResponseMessage(jsonObject.optJSONObject("ResponseMessage").toString());
//                                    }
//
//
//                                } else if (jsonObject.optString("ResponseMessage", "").equalsIgnoreCase("New Connection")) {
//
//                                    return;
//
//                                } else {
//                                    socket.setSoTimeout(5000);
//                                    System.out.println("About to send message to server - WAITING...");
//                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));
//                                    String response3 = getMessageFromServer(input);
//                                    System.out.println("And the answer is: " + response3);
//                                    socket.setSoTimeout(45000);
//                                    try {
//                                        System.out.println("Pinging...");
//                                        command = GSON.fromJson(data, NetCommand.class);
//                                        command.setCommand(NetCommand.Command.PING);
//
//                                    } catch (Exception e) {
//                                        System.out.println("Catching...275");
//                                    }
//                                }
//                            }
//
//                        } catch (Exception e) {
//                            System.out.println("Catching...281");
//                        }
//
//                        if (command != null) {
//                            System.out.println("Command is not null: " + command);
//                            if (attemptingPing) {
//                                attemptingPing = false;
//                            }
//
//                            switch (command.getCommand()) {
//
//                                case WAITING:
//                                case FAILED:
//                                    socket.setSoTimeout(5000);
//                                    System.out.println("About to send message to server - FAILING...");
//                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.FAILED));
//                                    String response4 = getMessageFromServer(input);
//                                    System.out.println("And the answer is: " + response4);
//                                    socket.setSoTimeout(45000);
//                                    break;
//
//                                case PING:
//                                    lastPing = System.currentTimeMillis();
//                                    socket.setSoTimeout(5000);
//                                    System.out.println("About to send message to server - PINGING...");
//                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.PONG));
//                                    String response5 = getMessageFromServer(input);
//                                    System.out.println("And the answer is: " + response5);
//                                    socket.setSoTimeout(45000);
//                                    NetCommand ping = new NetCommand().setCommand(NetCommand.Command.PING);
//                                    for (CommandListener listener : listenerList.keySet()) {
//                                        System.out.println("CommandListener will handle PING");
//                                        listener.handle(ping);
//                                    }
//                                    break;
//
//                                case PONG:
//                                    lastPing = System.currentTimeMillis();
//                                    break;
//
//                                case CANCEL:
//                                    System.out.println("Canceling...");
//
//                                    NetCommand cancel = new NetCommand().setCommand(NetCommand.Command.CANCEL);
//
//                                    if (!cancel.getWebhookId().isEmpty()) {
//                                        canceledList.add(cancel.getWebhookId());
//                                    } else {
//                                        canceledList.add(currentWebhook);
//                                    }
//
//                                    if (canceledList.size() > 25) {
//                                        canceledList.remove(0);
//                                    }
//
//                                    for (CommandListener listener : listenerList.keySet()) {
//                                        listener.handle(cancel);
//                                    }
//                                    break;
//
//                                case KILL:
//                                case SYNC:
//                                    for (CommandListener listener : listenerList.keySet()) {
//                                        listener.handle(command);
//                                    }
//                                    break;
//
//                                case RECEIPT:
//                                    try {
//                                        System.out.println("CASE RECEIPT");
//
//                                        socket.setSoTimeout(5000);
//                                        System.out.println("About to send message to server - RECEIPTING...");
//                                        sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(command.getUniqueId()));
//                                        String response6 = getMessageFromServer(input);
//                                        System.out.println("And the answer is: " + response6);
//                                        socket.setSoTimeout(45000);
//                                        System.out.println("Receiving " + command.getUniqueId());
//                                        for (CommandListener listener : listenerList.keySet()) {
//                                            listener.handle(command);
//                                        }
//
//                                    } catch (Exception e) {
//                                        System.out.println("Catching...347");
//                                    }
//                                    break;
//
//                                case TOKEN:
//                                case PRE_AUTH:
//                                case POST_AUTH:
//                                case VOID:
//                                case REFUND:
//                                case SALE:
//                                    System.out.println("IC TRANSACTION START?");
//                                    //TransactionLog.generateLog("Start Transaction", "Interceptor Client");
//
//                                default:
////                                    System.out.println("Receiving...");
////                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED).setUniqueId(currentWebhook));
//                                    socket.setSoTimeout(5000);
//                                    System.out.println("About to send message to server - WAITING...");
//                                    sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.WAITING));
//                                    String response7 = getMessageFromServer(input);
//                                    System.out.println("And the answer is: " + response7);
//                                    socket.setSoTimeout(45000);
//
//
//                                    System.out.println("Should start iterating command listeners...");
//                                    for (CommandListener listener : listenerList.keySet()) {
//                                        System.out.println("handling command listener - " + listener);
//                                        NetCommand finalCommand = command;
//                                        new Thread(() -> listener.handle(finalCommand)).start();
//                                    }
//                                    System.out.println("Any command listeners...");
//
//                                    try {
//                                        JSONObject temp = new JSONObject(command.getResponseMessage());
//
//                                        NetCommand clean = new NetCommand().setCommand(NetCommand.Command.CANCEL);
//                                        for (CommandListener listener : listenerList.keySet()) {
//                                            listener.handle(clean);
//                                        }
//
//                                        if (!canceledList.contains(temp.optString("unique_webhook_id", ""))) {
//                                            System.out.println("IC TRANSACTION FINISH?");
//                                            TransactionLog.generateLog("Finish Transaction", "Interceptor Client");
////                                            System.out.println("Receiving...");
////                                            sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.RECEIVED)
////                                                    .setUniqueId(temp.optString("unique_webhook_id", "")));
//                                            System.out.println("Assigning new value to currentWebhook...");
//                                            currentWebhook = temp.optString("unique_webhook_id", "");
//                                            System.out.println("Current webhook: " + currentWebhook);
//                                            System.out.println("Adding webhook to canceled: " + currentWebhook);
//                                            canceledList.add(currentWebhook);
//                                            System.out.println("Canceled list contains: " + canceledList);
//                                        }
//
//                                    } catch (Exception e) {
//                                        System.out.println("Catching...394");
//                                    }
//                                    break;
//                            }
//
//                            System.out.println("<<About to try to sleep>>");
//                            try {
//                                System.out.print("try to sleep 2000...");
//                                Thread.sleep(2000);
//                                System.out.println("...sleep over");
//                            } catch (Exception e) {
//                                System.out.println("Catching...403: " + e.getMessage());
//                            }
//                            System.out.println("<<Finished trying to sleep>>");
//
//                        } else {
//
//                            System.out.println("Command was null");
//                            if (!attemptingPing) {
//                                System.out.println("Attempting ping");
//                                attemptingPing = true;
//                                socket.setSoTimeout(5000);
//                                System.out.println("About to send message to server - PINGING...");
//                                sendMessageToServer(output, new NetCommand().setCommand(NetCommand.Command.PING));
//                                String response8 = getMessageFromServer(input);
//                                System.out.println("and the answer is: " + response8);
//                                socket.setSoTimeout(45000);
//                            } else {
//                                System.out.println("<<Else>>");
//                                break;
//                            }
//                            System.out.println("We're all done here.");
//                        }
//                        shutdown = false;
//                        System.out.println("shutdown: " + shutdown);
//                    }
//
//                } catch (Exception e) {
//                    System.out.println("Something else failed in the entire block");
//                }
//
//            } catch (Exception e) {
//                System.out.println("Socket connection failed");
//            }
//
//            System.out.println("<<SLEEP>>");
//            try {
//                System.out.println("<sleep 5000 start>");
//                Thread.sleep(5000);
//                System.out.println("<sleep 5000 over>");
//            } catch (Exception e) {
//                System.out.println("433");
//            }
//            System.out.println("<<SLEEP>>");
//
//        }
//    }
//
//    @Override
//    public void close() {
//        System.out.println("close() -> START");
//        shutdown = true;
//        SLAVE_POOL.shutdownNow();
//
//        System.out.println("Checking thread>");
//        if (thread != null) {
//            System.out.println("Thread is not null>");
//            thread.interrupt();
//            System.out.println("Interrupting>");
//            thread = null;
//        }
//        System.out.println("new threadpool");
//        SLAVE_POOL = new ThreadPoolExecutor(1, 25, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
//        System.out.println("close() -> END");
//    }
//
//    /**
//     * Attempt to connect from iteration of available servers
//     *
//     * @return The opened socket if successful, else null
//     */
//    private Socket connect() {
//
//        // TODO: Supply live servers
//        List<String> servers = new ArrayList<>();
//        servers.add("softpointdev.com");
//        servers.add("softpointdev.com");
//        servers.add("softpointdev.com");
//
//
//        for (String host : servers) {
//            Socket socket = connectServer(host);
//            if (socket != null) {
//                try {
//                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                    if (registerDevice(socket, out, in)) {
//                        System.out.println("Asserting socket: " + socket);
//                        System.out.println("Asserting printwriter: " + out);
//                        System.out.println("Asserting bufferedreader: " + in);
//                        return socket;
//                    }
//
//                } catch (Exception e) {
//                    return null;
//                }
//            }
//        }
//        throw new NullPointerException("\nSocket Connection Failed");
//    }
//
//    /**
//     * Attempts to open a socket connection to the given server on the given port. The timeout for
//     * this is 5 seconds.
//     *
//     * @param server The server address to attempt to connect to
//     * @return The opened socket if successful, else null
//     */
//    private Socket connectServer(String server) {
//        try {
//            Socket socket = new Socket();
//            socket.connect(new InetSocketAddress(server, InterceptorClient.port), 5000);
//            return socket;
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    /**
//     * Attempts to register a device with the server we have connected to. If this fails the connect
//     * method moves onto the next server and tries again. This is part of the connect process due to
//     * this also being the main check to determine if the server is actually running correctly and
//     * only being needed once when the device connects.
//     *
//     * @param out The output for sending message
//     * @param in  The input for getting messages
//     * @return True iff we manged to successfully register the device on the server, else false
//     */
//    boolean registerDevice(Socket socket, PrintWriter out, BufferedReader in) throws SocketException {
//
//        socket.setSoTimeout(5000);
//        sendMessageToServer(out, register);
//
//        BasicResponse data = getMessageFromServer(in, BasicResponse.class);
//
//        assert data != null;
//        System.out.println(data);
//        socket.setSoTimeout(45000);
//
//        lastPing = System.currentTimeMillis();
//
//        return data.getResponseCode() == 1;
//    }
//
//    /**
//     * Converts an object to a JSON string and forwards that string to #sendMessageToServer(String)
//     *
//     * @param out  The PrintWriter to write the message to
//     * @param message The object to send
//     */
//    private void sendMessageToServer(@NonNull PrintWriter out, @NonNull Object message) {
//        System.out.println("##sending message << " + message);
//        sendMessageToServer(out, GSON.toJson(message));
//    }
//
//    /**
//     * Sends a given message to the given PrintWriter. This method mainly is used after an Object is
//     * convert into JSON instead of having conversion code all over.
//     *
//     * @param out  The PrintWriter to write the message to
//     * @param message The object to send
//     */
//    private void sendMessageToServer(@NonNull PrintWriter out, @NonNull String message) {
//        System.out.println("#forwarding message << " + message);
//        out.println(message);
//    }
//
//    /**
//     * Get message from server and cast it to requested object
//     *
//     * @param in The Reader to read from
//     * @param klass The class we are attempting to parse the data to
//     * @param <E>   Return type of the class we are parsing to
//     * @return An object of the requested type if successful or null
//     */
//    private <E> E getMessageFromServer(@NonNull BufferedReader in, @NonNull Class<E> klass) {
//        try {
//            System.out.println("##getting message>>");
//            return GSON.fromJson(getMessageFromServer(in), klass);
//        } catch (JsonSyntaxException e) {
//            System.out.println("##snap > " + e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * Get message from server as string
//     *
//     * @param in The Reader to read from
//     * @return The String returned from the server
//     */
//    private String getMessageFromServer(@NonNull BufferedReader in) {
//        try {
////            System.out.println("#getting message>>");
////            String message = in.readLine();
////            System.out.println("#forwarding message > " + message);
//            return in.readLine();
//        } catch (IOException e) {
//            System.out.println("#snap > " + e.getMessage());
//            return null;
//        }
//    }
//}