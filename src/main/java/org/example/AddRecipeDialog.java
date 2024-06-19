package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class AddRecipeDialog extends javax.swing.JDialog {
    final String[] recipeNameTarget = {""};
    final String[] recipeDescriptionTarget = {""};
    ArrayList<String> ingredients = new ArrayList<>();
    ArrayList<String> steps = new ArrayList<>();

    public AddRecipeDialog(java.awt.Frame parent, boolean modal, String userName) {
        super(parent, "Add your recipe", modal);
        JTextArea recipeSummary = new JTextArea(" ", 5, 30);
        recipeSummary.setEditable(false);
        recipeSummary.setFont(new Font("Monospaced", Font.PLAIN, 14));
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
        JScrollPane scrollPane = new JScrollPane(recipeSummary);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        JPanel panelRecipeName = new JPanel();
        panelRecipeName.setLayout(new BoxLayout(panelRecipeName, BoxLayout.X_AXIS));
        panelRecipeName.add(new JLabel("Recipe Name:"));
        panelRecipeName.add(recipeName);
        panelRecipeName.add(addRecipeName);

        JPanel panelDescription = new JPanel();
        panelDescription.setLayout(new BoxLayout(panelDescription, BoxLayout.X_AXIS));
        panelDescription.add(new JLabel("Description:"));
        panelDescription.add(recipeDescription);
        panelDescription.add(addRecipeDescription);

        JPanel panelIngredient = new JPanel();
        panelIngredient.setLayout(new BoxLayout(panelIngredient, BoxLayout.X_AXIS));
        panelIngredient.add(new JLabel("Ingredient:"));
        panelIngredient.add(ingredient);
        panelIngredient.add(addIngredient);

        JPanel panelStep = new JPanel();
        panelStep.setLayout(new BoxLayout(panelStep, BoxLayout.X_AXIS));
        panelStep.add(new JLabel("Step:"));
        panelStep.add(step);
        panelStep.add(addStep);

        inputPanel.add(panelRecipeName);
        inputPanel.add(panelDescription);
        inputPanel.add(panelIngredient);
        inputPanel.add(panelStep);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(publishRecipe, BorderLayout.SOUTH);

        addRecipeName.addActionListener(e -> {
            recipeNameTarget[0] = recipeName.getText();
            recipeSummary.append("Recipe name: "+recipeName.getText() + "\n");
            recipeName.setText("");
        });

        addRecipeDescription.addActionListener(e -> {
            recipeDescriptionTarget[0] = recipeDescription.getText();
            recipeSummary.append("Descirption: "+recipeDescription.getText() + "\n");
            recipeDescription.setText("");
        });

        addIngredient.addActionListener(e -> {
            ingredients.add(ingredient.getText());
            recipeSummary.append("Ingredient added: "+ingredient.getText() + "\n");
            ingredient.setText("");
        });

        addStep.addActionListener(e -> {
            steps.add(step.getText());
            recipeSummary.append("Step added: "+step.getText() + "\n");
            step.setText("");
        });

        publishRecipe.addActionListener(e -> {
            Recipe recipe = new Recipe(recipeNameTarget[0], userName, ingredients, steps, recipeDescriptionTarget[0]);
            ShareYourRecipe.recipes.add(recipe);
            ShareYourRecipe.refreshMainScreen();
            ShareYourRecipe.resetHighlight();
            dispose();
        });

        add(mainPanel);
        pack();
        setLocationRelativeTo(getOwner());

    }

}
