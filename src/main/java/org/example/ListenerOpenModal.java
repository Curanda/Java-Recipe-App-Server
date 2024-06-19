package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ListenerOpenModal implements ActionListener {
    private JButton addRecipe;
    private JButton checkRecipe;
    private String userName;
    private JFrame recipeApp;

    public ListenerOpenModal(JButton addRecipe, JButton checkRecipe, String userName, JFrame recipeApp) {
        this.addRecipe = addRecipe;
        this.checkRecipe = checkRecipe;
        this.userName = userName;
        this.recipeApp = recipeApp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addRecipe) {
            AddRecipeDialog addRecipeDialog = new AddRecipeDialog(recipeApp, true, userName);
            addRecipeDialog.setTitle("Add a recipe");
            addRecipeDialog.setSize(400, 300);
            addRecipeDialog.setLayout(new BorderLayout());
            addRecipeDialog.setVisible(true);
            addRecipeDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        } else if (e.getSource() == checkRecipe) {
            CheckRecipeDialog checkRecipeDialog = new CheckRecipeDialog(recipeApp,true);
            checkRecipeDialog.setTitle("Check a recipe");
            checkRecipeDialog.setSize(400, 400);
            checkRecipeDialog.setLayout(new BorderLayout());
            checkRecipeDialog.setVisible(true);
            checkRecipeDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
    }
}
