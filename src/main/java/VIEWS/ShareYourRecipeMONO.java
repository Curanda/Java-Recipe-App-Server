package VIEWS;

import MODELS.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.websocket.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@ClientEndpoint
public class ShareYourRecipeMONO extends JFrame implements Runnable, ActionListener {

    // runnable, bo otwieramy osobne wątki dla każdej isntancji.
    private String userName;
    private Session session;
    private ArrayList<Recipe> recipes = new ArrayList<>();
    private int currentLine = 0;
    private int selectedRecipeIndex = 0;
    private Recipe selectedRecipe;
    private JTextArea mainScreen;
    private JButton addRecipe, scrollUp, scrollDown, checkRecipe;
    private JDialog addRecipeDialog, checkRecipeDialog;
    private Timer refreshTimer;
    private static final int refresh = 10000;

    // Wiem że nie powininen podawać tutaj klucza, proszę mi nie zhakować komputera.
    private static final String SUPABASE_URL = "";
    private static final String SUPABASE_API_KEY = "";
    private static final String SUPABASE_TABLE = "Recipes";
    private static final HttpClient client = HttpClient.newHttpClient();

    public ShareYourRecipeMONO(String userName) throws Exception {
        this.userName = userName;
        setTitle("Share Your Recipe - " + userName);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        renderLayout();
        addElements();
        addListeners();

        refreshMainScreen();
        autoRefreshMainScreen();
        highlightLine(currentLine);

        pack();
        setVisible(true);
    }

    private void renderLayout() {
        mainScreen = new JTextArea(" ", 18, 40);
        mainScreen.setEditable(false);
        mainScreen.setBorder(new CompoundBorder(new EmptyBorder(5, 10, 5, 10), new LineBorder(Color.BLACK, 1)));

        addRecipe = new JButton("Add Recipe");
        scrollUp = new JButton("Up");
        scrollDown = new JButton("Down");
        checkRecipe = new JButton("Check Recipe");

        addRecipeDialog = new JDialog(this, "Add a recipe", true);
        checkRecipeDialog = new JDialog(this, "Check a recipe", true);
    }

    private void addElements() {
        setLayout(new BorderLayout());
        add(new JScrollPane(mainScreen), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(addRecipe);
        buttonPanel.add(checkRecipe);

        JPanel scrollPanel = new JPanel(new GridLayout(1, 2));
        scrollPanel.add(scrollUp);
        scrollPanel.add(scrollDown);

        add(scrollPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        addRecipe.addActionListener(this);
        checkRecipe.addActionListener(this);
        scrollDown.addActionListener(e -> moveHighlight(1));
        scrollUp.addActionListener(e -> moveHighlight(-1));
    }

    // listener do otwierania modali.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addRecipe) {
            showAddRecipeDialog();
        } else if (e.getSource() == checkRecipe) {
            showCheckRecipeDialog();
        }
    }

    // modal do dodawania przepisu.
    private void showAddRecipeDialog() {
        JTextArea recipeSummary = new JTextArea(" ", 5, 30);
        recipeSummary.setEditable(false);
        recipeSummary.setLineWrap(true);
        recipeSummary.setWrapStyleWord(true);
        JTextField recipeName = new JTextField(20);
        JButton addRecipeName = new JButton("Add");
        JTextField recipeDescription = new JTextField(20);
        JButton addRecipeDescription = new JButton("Add");
        JTextField ingredient = new JTextField(20);
        JButton addIngredient = new JButton("+");
        JTextField step = new JTextField(20);
        JButton addStep = new JButton("+");
        JButton publishRecipe = new JButton("Publish");

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        inputPanel.add(newPanel("Recipe Name:", recipeName, addRecipeName));
        inputPanel.add(newPanel("Description:", recipeDescription, addRecipeDescription));
        inputPanel.add(newPanel("Ingredient:", ingredient, addIngredient));
        inputPanel.add(newPanel("Step:", step, addStep));

        mainPanel.add(new JScrollPane(recipeSummary), BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(publishRecipe, BorderLayout.SOUTH);

        // zmienne do podawania do klasy recipe
        // nie pamiętam już dlaczego, ale nie mogłem po prostu zmodyfikować zwykłych stringów
        // musiałem zrobić takie tabele. Coś tam podkreślało na czerwono jak były zwykłe stringi.
        final String[] recipeNameTarget = {""};
        final String[] recipeDescriptionTarget = {""};
        ArrayList<String> ingredients = new ArrayList<>();
        ArrayList<String> steps = new ArrayList<>();

        // listenery do dodawania docelowych argumentów dla klasy recipe.
        addRecipeName.addActionListener(e -> {
            recipeNameTarget[0] = recipeName.getText();
            recipeSummary.append("Recipe name: " + recipeName.getText() + "\n");
            recipeName.setText("");
        });

        addRecipeDescription.addActionListener(e -> {
            recipeDescriptionTarget[0] = recipeDescription.getText();
            recipeSummary.append("Description: " + recipeDescription.getText() + "\n");
            recipeDescription.setText("");
        });

        addIngredient.addActionListener(e -> {
            ingredients.add(ingredient.getText());
            recipeSummary.append("Ingredient added: " + ingredient.getText() + "\n");
            ingredient.setText("");
        });

        addStep.addActionListener(e -> {
            steps.add(step.getText());
            recipeSummary.append("Step added: " + step.getText() + "\n");
            step.setText("");
        });

        // listener dla tworzenia klasy recipe i wysyłania do bazy.
        publishRecipe.addActionListener(e -> {
            Recipe recipe = new Recipe(recipeNameTarget[0], userName, ingredients, steps, recipeDescriptionTarget[0]);
            try {
                // tutaj metoda obsługująca wysyłanie do bazy.
                insertRecipe(recipe);
                System.out.println("Recipe published");
                // usuwamy modala
                addRecipeDialog.dispose();
                // odświeżamy ekran główny żeby pobrać uzupełnioną listę z bazy.
                refreshMainScreen();
                // resetujemy podświetlenie.
                resetHighlight();
            } catch (Exception ex) {
                System.out.println("Error publishing recipe: " + ex.getMessage());
            }
        });

        addRecipeDialog.setContentPane(mainPanel);
        addRecipeDialog.pack();
        addRecipeDialog.setLocationRelativeTo(this);
        addRecipeDialog.setVisible(true);
    }

    private JPanel newPanel(String labelText, JComponent component, JComponent... additionalComponents) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(labelText));
        panel.add(component);
        for (JComponent additional : additionalComponents) {
            panel.add(additional);
        }
        return panel;
    }

    // inicjacja drugiego modala do sprawdzania szczxegółów przepisów z ekranu głównego oraz do nadawania ocen.
    private void showCheckRecipeDialog() {
        JTextArea recipeSummary = new JTextArea(" ", 15, 50);
        JComboBox<Integer> ratingDropdown = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5});
        JButton submitRating = new JButton("Submit");

        recipeSummary.setEditable(false);
        recipeSummary.setLineWrap(true);
        recipeSummary.setWrapStyleWord(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JScrollPane(recipeSummary), BorderLayout.CENTER);

        JPanel ratingPanel = new JPanel(new GridLayout(1, 3));
        ratingPanel.add(new JLabel("Rate this recipe:"));
        ratingPanel.add(ratingDropdown);
        ratingPanel.add(submitRating);
        mainPanel.add(ratingPanel, BorderLayout.SOUTH);

        // w sumie to można usunąć bo rekordy w bazie już są od początku, ale załóżmy że może być błąd połączenia
        // z bazą. właściwie ten check można przeniść do góry żeby w ogóle nie można otworzyć tego modala jeśli ekran
        // jest pusty.

        if (selectedRecipe != null) {
            updateRecipeSummary(recipeSummary);
        } else {
            recipeSummary.append("No recipe selected");
        }

        // listener do ustawiania oceny.
        final int[] selectedRating = {0};
        ratingDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                selectedRating[0] = (int) ratingDropdown.getSelectedItem();
            }
        });

        // listener do submitu. sprawdzamy czy oceniajacy user nie jest przypadkiem autorem przepisu.
        submitRating.addActionListener(e -> {
            if (selectedRecipe != null && !selectedRecipe.getUserName().equals(userName)) {
                // ustawiamy nową ocenę
                selectedRecipe.rateRecipe(selectedRating[0]);
                try {
                    // podajemy do metody bazodanowej index i zmodyfikowany obiekt/
                    updateRecipeRating(selectedRecipeIndex, selectedRecipe);
                    String notification = "Your recipe for " + selectedRecipe.getRecipeName() + " has been given " + selectedRating[0] + " stars by " + userName;
                    // metoda serwerowa: wysyłamy notyfikację do autora przepisu o tym kto i ile gwiazdek dał jego przepisowi.
                    sendNotification(notification, selectedRecipe.getUserName());
                    // odświeżamy szczegóły przepisu na ekranie modalu.
                    updateRecipeSummary(recipeSummary);
                    // odświeżamy ekran główny żeby pojawiły się nowe oceny.
                    refreshMainScreen();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error updating rating: " + ex.getMessage());
                }
            } else {
                recipeSummary.setText("You can't rate your own recipe");
            }
        });

        checkRecipeDialog.setContentPane(mainPanel);
        checkRecipeDialog.pack();
        checkRecipeDialog.setLocationRelativeTo(this);
        checkRecipeDialog.setVisible(true);
    }

    // odświeżanie przepisu po modyfikacji dla ekranu modala.
    private void updateRecipeSummary(JTextArea recipeSummary) {
        recipeSummary.setText("");
        recipeSummary.append("Recipe for " + selectedRecipe.getRecipeName() + "\n");
        recipeSummary.append("added by " + (selectedRecipe.getUserName().equals(userName) ? "You" : selectedRecipe.getUserName()) + "\n");
        recipeSummary.append("Description: " + selectedRecipe.getDescription() + "\n");
        recipeSummary.append("Ingredients: " + selectedRecipe.getIngredients().toString() + "\n");
        recipeSummary.append("Steps: " + selectedRecipe.getSteps().toString() + "\n");
        recipeSummary.append("Rating: " + selectedRecipe.getRating() + "\n");
        recipeSummary.append("Number of reviews: " + selectedRecipe.getNumberOfReviews() + "\n");
    }

    // odświeżanie ekranu.
    public void refreshMainScreen() throws Exception {
        Gson gson = new Gson();
        String dbResponse = getRecipes();
        mainScreen.setText("");
        recipes = gson.fromJson(dbResponse, new TypeToken<ArrayList<Recipe>>() {
        }.getType());
        for (Recipe recipe : recipes) {
            if (recipe.getNumberOfReviews() == 0) {
                mainScreen.append(recipe.getRecipeName() + " by " + recipe.getUserName() + ", no reviews\n");
            } else {
                mainScreen.append(recipe.getRecipeName() + " by " + recipe.getUserName() + ", rated as: " +
                        recipe.getRating() + " by " + recipe.getNumberOfReviews() + " users\n");
            }
        }
    }

    // odświeżanie ekranu głównego automatycznie, ustawiłem co 10 sekund.
    private void autoRefreshMainScreen() {
        ActionListener refreshScreen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    refreshMainScreen();
                    // czasami po dodaniu oceny highlight przenosi sie poniżej dostępnej ilości rzędów.
                    // mam trochę za mało czasu żeby inwestygować, więc przy refreshu sprawdzamy gdzie on jest.
                } catch (Exception ex) {
                    System.err.println("Error refreshing main screen: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        };
        refreshTimer = new Timer(refresh, refreshScreen);
    }

    // starter odświerzacza do klepnięcia podczas wywoływania run.
    public void startRefresh() {
        refreshTimer.start();
    }

    // metoda do podświetlania przepisów naekranie głównym.
    // większość tej metody i pozostałych do przesuwania podświetlenia skopiowałem żywcem ze stacka szczerze mówiąc.
    private void highlightLine(int line) {
        try {
            Highlighter highlighter = mainScreen.getHighlighter();
            highlighter.removeAllHighlights();
            int startOffset = mainScreen.getLineStartOffset(line);
            int endOffset = mainScreen.getLineEndOffset(line);
            highlighter.addHighlight(startOffset, endOffset, new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
            mainScreen.setCaretPosition(startOffset);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void resetHighlight() {
        highlightLine(0);
    }

    // metoda dla listenerów, do przemieszczania podświetlenia w górę i wdół listy ekranu głównego.
    private void moveHighlight(int direction) {
        int newLine = currentLine + direction;
        if (newLine >= 0 && newLine < mainScreen.getLineCount()) {
            currentLine = newLine;
            selectedRecipeIndex = currentLine;
            highlightLine(currentLine);
            selectedRecipe = recipes.get(selectedRecipeIndex);
        }
    }

    // przyznam
    public void initializeWebSocket() {
        try {
            // container łączy z serwerem websocketowym
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            // ws:// dedykowany adres dla websockoetów.
            String wsUrl = "ws://localhost:2002/RecipeServer/ratings/" + userName;
            // przypisujemy naszej sesji endpoint serwera.
            this.session = container.connectToServer(this, URI.create(wsUrl));
            System.out.println(userName + " connected to server");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // metoda do wysyłania notyfikacji a propos ocen do autora przepisu.
    public void sendNotification(String notification, String targetUser) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(notification + ":" + targetUser);
                System.out.println(userName + " is sending notification to user: " + targetUser);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("ShareYourRecipe user: " + userName + " has closed connection to server.");
        }
    }

    // na przyjęciu wiadomości od serwera wywołujemy okienko z treścią.
    @OnMessage
    public void onMessage(String incomingNotification) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, incomingNotification);
        });
    }

    // metoda do startowania połączenia z serwerem i autoodświeżania ekranu.
    @Override
    public void run() {
        initializeWebSocket();
        startRefresh();
        System.out.println("ShareYourRecipe user: " + userName + " is running");
    }

    // obsługa wysyłania nowych przepisów do bazy.
    public static void insertRecipe(Recipe recipe) throws Exception {
        // jsonujemy nasz przepis. Dziwna sprawa, supabase bierze tego jsona i bezbłędnie do parsuje do tabeli.
        Gson gson = new Gson();
        String json = gson.toJson(recipe);
        // budujemy polączenie http z metodą .POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(SUPABASE_URL + "/rest/v1/" + SUPABASE_TABLE))
                .header("Content-Type", "application/json")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // wysyłamy pakiet do bazy.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new RuntimeException("Failed to insert recipe: " + response.body());
        }
    }

    // SELECT
    public static String getRecipes() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(SUPABASE_URL + "/rest/v1/" + SUPABASE_TABLE))
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get recipes: " + response.body());
        }

        return response.body();
    }

    // nie updateujemy konkretnych pól w tabeli, tylko wysyłamy zmieniony obiekt na ten sam indeks co stary.
    public static void updateRecipeRating(int selectedRecipeIndex, Recipe recipe) throws Exception {
        Gson gson = new Gson();
        // nie wiem czy to jest bulletproof, ale tj na pewno najprostsza odpowiedź na pytanie jak znaleźć rekord
        // w bazie o konkretnym indeksie jeżeli w obiekcie recipe nie ma pola indeks.
        int id = selectedRecipeIndex + 1;
        String json = gson.toJson(recipe);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(SUPABASE_URL + "/rest/v1/" + SUPABASE_TABLE + "?id=eq." + id))
                .header("Content-Type", "application/json")
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new RuntimeException("Failed to update recipe: " + response.body());
        }
    }

    // gettery
    private String getUserName() {
        return userName;
    }

    private Session getSession() {
        return session;
    }

    private Recipe getSelectedRecipe() {
        if (recipes.isEmpty()) {
            return null;
        }
        selectedRecipe = recipes.get(selectedRecipeIndex);
        return selectedRecipe;
    }

    private int getSelectedRecipeIndex() {
        return selectedRecipeIndex;
    }

    // KONIEC!!
}