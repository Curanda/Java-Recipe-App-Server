package MAIN;

import HELPERS.UserThreadStarter;
import WORKERS.ServerSocketMulti;

// Kilka słów wtsępu. Nie mogłem się kompletnie ogarnąć na podstawie tego przykładu, który Pan podał. Szczerze mówiąc
// po przeczytaniu pdfa nie mogłem się ogarnąć z tym serwerem i handlerami. Może jakbym miał code review
// i możliwość zadania konkretnych pytań to byłoby banalne. Na stacku i jakichś stronkach generalnie
// użycie tych websocketów zawsze zawiera dekoratory @OnMessage itp. W związku tym zmieniłem podejście i
// zaimplementowałem coś na wzór przykładów które znalazłem na necie, korzystając z rekomendowanych bibliotek.
// Wszystkie zmienne i nazwy po angielsku bo tak się napociłęm, że wrzucam na githuba do portfolio.
// Zdecydowałem się cały front i logikę użytkownika umieścić w jednej klasie. Wiem, że dostanę naganę za to, ale
// jest konkretny powód. Rozbiwszy wszystko na osobne klasy zacząłem mieć problemy typu naciśnięcie przycisku na jednej
// isntancji powodowało zmianę na drugiej instancji. Oczywiście mógłbym je pomapować i potem wyciągać konkretne instancje
// a może nawet skorzystać z istniejącej hashmapy sesji i userów która jest w klasie serwera, ALE znacznie prościej i
// szybciej było wszystko władować do jednej klasy. Czytelność cierpi na tym, nijak się to nie ma do SOLID, ale
// czas mnie gonił, więc to zaważyło na mojej decyzji.
// Zdecydowałem się jako bonus na przechowywanie danych w Supabase. Znam dosyć dobrze Supabase z jakiegoś poprzedniego
// projektu i było to dosyć proste.


public class Main {
    public static void main(String[] args) {

        // Startujemy serwer na osobvnym wątku.
        ServerSocketMulti server = new ServerSocketMulti();
        Thread serverThread = new Thread(server::startServer);
        serverThread.start();

        // czekamy na inicjalizację przed tworzeniem obiektów.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // inicjalizujemy helpera do startowania obiektów w osobnych wątkach.
        UserThreadStarter userThreadStarter = new UserThreadStarter();

        // instancje aplikacji
        userThreadStarter.StartUser("MMMKKK");
        userThreadStarter.StartUser("Jonas_Jonsson");

        // Utrzymujemy główny wątek przy życiu do momentu zamknięcia.
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            server.stopServer();
        }
    }
}