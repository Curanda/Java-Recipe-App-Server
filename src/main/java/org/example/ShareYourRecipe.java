package org.example;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ShareYourRecipe extends JFrame {

    private String userName;
    public static ArrayList<Recipe> recipes = new ArrayList<>();
    private static int currentLine = 0;
    private static int selectedRecipeIndex = currentLine;
    public static Recipe selectedRecipe;
    static JTextArea mainScreen = new JTextArea(" ",18, 40);
    JButton addRecipe = new JButton("Add Recipe");
    JButton scrollUp = new JButton("Up");
    JButton scrollDown = new JButton("Down");
    JButton checkRecipe = new JButton("Check Recipe");
    JDialog addRecipeDialog = new JDialog();
    JDialog checkRecipeDialog = new JDialog();
    JPanel buttonPanel = new JPanel(new GridLayout(1,2));
    JPanel buttonPanel2 = new JPanel(new GridLayout(1,2));
    EmptyBorder padding = new EmptyBorder(5, 10, 5, 10);
    LineBorder outline = new LineBorder(Color.BLACK, 1);
    CompoundBorder compoundBorder = new CompoundBorder(padding, outline);


    public ShareYourRecipe(String userName) {
        JFrame recipeApp = new JFrame("Share Your Recipe");
        // najprostsze rozwiązanie, bez angażowania loginu, inicjalizujemy klasę z nazwą użytkownika.
        this.userName = userName;

        setSize(700,500);

        // dodawanie elementów do layoutu.
        recipeApp.getContentPane().setLayout(new BorderLayout());
        mainScreen.setEditable(false);
        mainScreen.setBorder(compoundBorder);
        buttonPanel.add(addRecipe);
        buttonPanel.add(checkRecipe);
        buttonPanel2.add(scrollUp);
        buttonPanel2.add(scrollDown);
        recipeApp.getContentPane().add(new JScrollPane(mainScreen), BorderLayout.NORTH);
        recipeApp.getContentPane().add(buttonPanel2, BorderLayout.CENTER);
        recipeApp.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // listenery do otwierania modali.
        ListenerOpenModal listenerOpenModal = new ListenerOpenModal(addRecipe, checkRecipe, userName, recipeApp);
        addRecipe.addActionListener(listenerOpenModal);
        checkRecipe.addActionListener(listenerOpenModal);

        // listener do przewijania przepisów.
        scrollDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveHighlight(1);
            }
        });
        scrollUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveHighlight(-1);
            }
        });

        // zerowanie podświetlenia przy inicjacji klasy.
        highlightLine(currentLine);

        recipeApp.pack();
        recipeApp.setVisible(true);
        recipeApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // publiczna metoda, odświeżania wyświetlacza przepisów do użytku przez klasę dodającą przepisy.
    public static void refreshMainScreen() {
        mainScreen.setText("");
        recipes.forEach(recipe -> {
            if (recipe.getNumberOfReviews() == 0) {
                mainScreen.append(recipe.getRecipeName() +" by "+recipe.getUserName()+", no reviews\n");
            } else {
                mainScreen.append(recipe.getRecipeName() +" by "+recipe.getUserName()+", rated as: " + recipe.getRating() + " by " + recipe.getNumberOfReviews() + " users\n");
            }
        });
    }

    public String getUserName() {
        return userName;
    }

    // metoda do podświetlania linii w wyświetlaczu przepisów.
    private static void highlightLine(int line) {
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

    public static void resetHighlight() {
        highlightLine(0);
    }

    private static void moveHighlight(int direction) {
        int newLine = currentLine + direction;
        if (newLine >= 0 && newLine < mainScreen.getLineCount()) {
            currentLine = newLine;
            selectedRecipeIndex = currentLine;
            highlightLine(currentLine);
        }
    }

    // setter dla przepisu wybranego z ekranu do wyświetlenia w modalu checkRecipe.
    public static Recipe getSelectedRecipe() {
        if (recipes.size() == 0) {
            return null;
        }
        selectedRecipe = recipes.get(selectedRecipeIndex);
        return selectedRecipe;
    }

    public static void main(String[] args) {
        ShareYourRecipe recipeApp = new ShareYourRecipe("MMMKKK");
    }

}
