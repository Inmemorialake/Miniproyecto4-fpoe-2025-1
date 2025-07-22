package org.example.eiscuno.model.card;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Represents a card in the Uno game.
 */
public class Card {
    private String url;
    private String value;
    private String color;
    private Image image;
    private ImageView cardImageView;

    /**
     * Constructs a Card with the specified image URL and name.
     *
     * @param url the URL of the card image
     * @param value of the card
     */
    public Card(String url, String value, String color) {
        this.url = url;
        this.value = value;
        this.color = color;
        this.image = new Image(String.valueOf(getClass().getResource(url)));
        this.cardImageView = createCardImageView();
    }

    /**
     * Creates and configures the ImageView for the card.
     *
     * @return the configured ImageView of the card
     */
    private ImageView createCardImageView() {
        ImageView card = new ImageView(this.image);
        card.setY(16);
        card.setFitHeight(90);
        card.setFitWidth(70);
        return card;
    }

    /**
     * Gets the ImageView representation of the card.
     *
     * @return the ImageView of the card
     */
    public ImageView getCard() {
        return cardImageView;
    }

    /**
     * Gets the image of the card.
     *
     * @return the Image of the card
     */
    public Image getImage() {
        return image;
    }

    public String getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }

    public boolean canBePlayedOn(Card topCard) {
        try {
            // Wild card and +4 can always be played
            if (this.isWildCard() || this.isPlusFour()) {
                return true;
            }
            // +2 can be played if color matches or top card is +2
            if (this.isPlusTwo()) {
                return this.color != null && (this.color.equalsIgnoreCase(topCard.getColor()) || topCard.isPlusTwo());
            }
            // Skip or Reverse: can be played if color matches
            if (this.isSkipOrReverse()) {
                return this.color != null && this.color.equalsIgnoreCase(topCard.getColor());
            }
            // Number cards: can be played if color or value matches
            if (this.value != null && this.value.matches("[0-9]+")) {
                return this.color != null && (this.color.equalsIgnoreCase(topCard.getColor())
                        || (this.value != null && this.value.equalsIgnoreCase(topCard.getValue())));
            }
            // Default: only color match
            return this.color != null && this.color.equalsIgnoreCase(topCard.getColor());
        } catch (Exception e) {
            // If any error, move is not valid
            return false;
        }
    }

    public boolean isSkipOrReverse() {
        String urlLower = this.url.toLowerCase();
        return urlLower.contains("skip") || urlLower.contains("reverse");
    }

    // Métodos auxiliares para identificar tipos de carta
    public boolean isWildCard() {
        // Comodín clásico (wild sin color)
        return this.url.endsWith("wild.png");
    }
    public boolean isPlusFour() {
        // +4
        return this.url.contains("4_wild_draw");
    }
    public boolean isPlusTwo() {
        // +2
        return this.url.contains("2_wild_draw");
    }
    public boolean isColoredWild() {
        // Comodines de color (wild_draw_blue, wild_draw_red, etc.) que no sean +4 ni wild clásico
        return this.isPlusTwo() && this.color != null;
    }
    public boolean isChangeColor() {
        // Cambio de color (comodín de color, si existiera otro)
        return isWildCard(); // En este UNO, el comodín es el cambio de color
    }

    public boolean isSpecial() {
        // Consideramos especiales: comodines, +2, +4, cambio de color, ceder turno
        // Comodines: color BLACK o valor null
        if ("BLACK".equalsIgnoreCase(this.color)) {
            return true;
        }
        // +2 y +4
        if ("2".equals(this.value) && this.url.contains("wild_draw")) {
            return true;
        }
        if ("4".equals(this.value) && this.url.contains("wild_draw")) {
            return true;
        }
        // Cambio de color (comodín)
        if (this.url.contains("wild")) {
            return true;
        }
        // Ceder turno (skip o reverse)
        String urlLower = this.url.toLowerCase();
        if (urlLower.contains("skip") || urlLower.contains("reverse")) {
            return true;
        }
        return false;
    }

    public String getUrl() {
        return url;
    }
}
