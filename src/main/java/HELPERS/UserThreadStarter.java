package HELPERS;

import VIEWS.ShareYourRecipeMONO;

import javax.swing.*;

public class UserThreadStarter {
    public static void StartUser(String userName) {

        // invokelater dla bezpiecznego rerenderowania layoutu podczas używania wątków.
        SwingUtilities.invokeLater(() -> {
            try {
                // tworzymy instancję aplikacji i startujemy ją w nowym wątku.
                ShareYourRecipeMONO shareYourRecipe = new ShareYourRecipeMONO(userName);
                new Thread(shareYourRecipe).start();
                System.out.println("Started ShareYourRecipe instance for " + userName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}