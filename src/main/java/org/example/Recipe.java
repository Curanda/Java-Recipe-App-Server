package org.example;

import java.util.ArrayList;

public class Recipe {
    public String recipeName;
    public String userName;
    public ArrayList<String> ingredients = new ArrayList<>();
    public ArrayList<String> steps = new ArrayList<>();
    public String description;
    public double rating = 0.0;
    public int numberOfReviews = 0;

    public Recipe(String recipeName, String userName, ArrayList<String> ingredients, ArrayList<String> steps, String description) {
        this.recipeName = recipeName;
        this.userName = userName;
        this.ingredients = ingredients;
        this.steps = steps;
        this.description = description;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getUserName() {
        return userName;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void addIngredients(String ingredient) {
        ingredients.add(ingredient);
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    public void setSteps(String step) {
        steps.add(step);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public int getNumberOfReviews() {
        return numberOfReviews;
    }

    public void rateRecipe(int rate) {
        if (this.rating == 0.0) {
            this.rating = rate;
            this.numberOfReviews++;
        } else {
            this.numberOfReviews++;
            this.rating = (this.rating + rate) / this.numberOfReviews;
        }
    }

}
