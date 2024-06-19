package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class CheckRecipeDialog extends JDialog {
    private Recipe selectedRecipe;
    private static int[] rating = {0,1, 2, 3, 4, 5};
    private static int selectedRating = 0;

    public CheckRecipeDialog(java.awt.Frame parent, boolean modal) {
        super(parent, "Check recipe", modal);
        this.selectedRecipe = ShareYourRecipe.getSelectedRecipe();
        JTextArea recipeSummary = new JTextArea(" ", 15, 50);
        JComboBox ratingDropdown = new JComboBox();
        JButton submitRating = new JButton("Submit");

        for (int i : rating) {
            ratingDropdown.addItem(i);
        }
        recipeSummary.setEditable(false);
        recipeSummary.setFont(new Font("Monospaced", Font.PLAIN, 14));
        recipeSummary.setLineWrap(true);
        recipeSummary.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(recipeSummary);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        JPanel ratingPanel = new JPanel( new GridLayout( 1, 3 ) );
        ratingPanel.add(new JLabel("Rate this recipe:"));
        ratingPanel.add(ratingDropdown);
        ratingPanel.add(submitRating);
        getContentPane().add(ratingPanel, BorderLayout.SOUTH);

        if (selectedRecipe != null) {
            recipeSummary.append("Recipe for "+selectedRecipe.getRecipeName()+"\n");
            recipeSummary.append("added by "+selectedRecipe.getUserName()+"\n");
            recipeSummary.append("Description: "+selectedRecipe.getDescription()+"\n");
            recipeSummary.append("Ingredients: "+selectedRecipe.getIngredients().toString()+"\n");
            recipeSummary.append("Steps: " +selectedRecipe.getSteps().toString()+"\n");
            recipeSummary.append("Rating: "+selectedRecipe.getRating()+"\n");
            recipeSummary.append("Number of reviews: "+selectedRecipe.getNumberOfReviews()+"\n");
        } else {
            recipeSummary.append("No recipe selected");
        }

        ratingDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                selectedRating = (int) ratingDropdown.getSelectedItem();
            }
        });

        submitRating.addActionListener(e -> {
            if (selectedRecipe != null) {
                ShareYourRecipe.getSelectedRecipe().rateRecipe(selectedRating);
                recipeSummary.setText("");
                recipeSummary.append("Recipe for "+selectedRecipe.getRecipeName()+"\n");
                recipeSummary.append("added by "+selectedRecipe.getUserName()+"\n");
                recipeSummary.append("Description: "+selectedRecipe.getDescription()+"\n");
                recipeSummary.append("Ingredients: "+selectedRecipe.getIngredients().toString()+"\n");
                recipeSummary.append("Steps: " +selectedRecipe.getSteps().toString()+"\n");
                recipeSummary.append("Rating: "+ShareYourRecipe.getSelectedRecipe().getRating()+"\n");
                recipeSummary.append("Number of reviews: "+ShareYourRecipe.getSelectedRecipe().getNumberOfReviews()+"\n");
                ShareYourRecipe.refreshMainScreen();
            }
        });


        pack();
        setLocationRelativeTo(getOwner());
    }




}
