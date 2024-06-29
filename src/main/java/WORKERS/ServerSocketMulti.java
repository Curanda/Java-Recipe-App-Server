package WORKERS;

import org.glassfish.tyrus.server.Server;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

// piękny dekorator dla wejścia
@ServerEndpoint(value = "/ratings/{userName}")
public class ServerSocketMulti {

    // Hashmapa do key:value pairs, tzn przechowujemy każdego usera z przypisaną mu sesją.
    // ConcurrentHashMap ponoć zmniejsza ryzyko wystąpienia jakichś błędów przy urhucmaniu na osobnych wątkach.
    private static final ConcurrentHashMap<String, Session> users = new ConcurrentHashMap<>();
    private Server server;
    private Thread serverThread;

    // na otwarciu dodajemy userów i ich sesje do hashmapy
    @OnOpen
    public void onOpen(Session session, @PathParam("userName") String userName) {
        users.put(userName, session);
        System.out.println(userName + " connected to the server");
    }

    // na przyjęciu wiadomości wyciągamy ze stringa nazwę odbiorcy. Wiem, że to jest kompletnie podatne na błędy, ale
    // jest piątek wieczorem i nie jestem w stanie już nic dopracować przed jutrzejszą obroną.
    @OnMessage
    public void onMessage(String notification, Session session) throws Exception {
        String[] parts = notification.split(":", 2);
        if (parts.length == 2) {
            String notificationToSend = parts[0];
            String targetUser = parts[1];
            // wyciągamy sesję odbiorcy i sprawdzamy czy istnieje i czy jest otwarta a potem podajemy wiadomość.
            Session targetSession = users.get(targetUser);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(notificationToSend);
                System.out.println("Message sent to " + targetUser + ": " + notificationToSend);
            } else {
                // jeśli sesji nie ma to wysyłamy do nadawcy info.
                session.getBasicRemote().sendText("Server: User " + targetUser + " is not available.");
                System.out.println("Failed to send message to " + targetUser + ": user not available");
            }
        }
    }

    // na zamknięciu wyrzucamy usera z hashmapy.
    @OnClose
    public void onClose(Session session, @PathParam("userName") String userName) {
        users.remove(userName);
        System.out.println("User disconnected: " + userName);
    }

    // startujemy serwer na localhoście. Tutaj pobrałem bibliotekę tyrus.server żeby sobie uprościć.
    public void startServer() {
        server = new Server("localhost", 2002, "/RecipeServer", null, ServerSocketMulti.class);
        // tworzymy osobny wątek dla serwera.
        serverThread = new Thread(() -> {
            try {
                server.start();
                System.out.println("WebSocket server started on thread " + Thread.currentThread().getName());
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // startujemy wątek
        serverThread.start();
    }

    // zatrzymywanie serwera i wątku.
    public void stopServer() {
        if (server != null) {
            server.stop();
            System.out.println("Server stopped");
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }
}