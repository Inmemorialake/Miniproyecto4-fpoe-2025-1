package org.example.eiscuno.model.card;

// Imports
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.Serializable;

/**
 * Represents a card in the Uno game.
 */
public class Card implements Serializable {
    private String url;
    private String value;
    private String color;
    private transient Image image;
    private transient ImageView cardImageView;

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
        card.setFitHeight(170);
        card.setFitWidth(110);
        return card;
    }

    public ImageView getCard() {
        return cardImageView;
    }


    public Image getImage() {
        return image;
    }

    public String getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }

    /**
     * Checks if the card can be played on top of the given card.
     *
     * @param topCard the card on top of which this card is being played
     * @return true if this card can be played on top of the given card, false otherwise
     */
    public boolean canBePlayedOn(Card topCard) {
        try {
            // Wild card and +4 can always be played
            if (this.isWildCard() || this.isPlusFour()) {
                return true;
            }
            // +2 can be played if color matches or top card is +2
            if (this.isPlusTwo()) {
                return this.color != null && topCard.getColor() != null && (this.color.equalsIgnoreCase(topCard.getColor()) || topCard.isPlusTwo());
            }
            // Skip: can be placed if the color or the type matches
            if (this.isSkip()) {
                return this.color != null && topCard.getColor() != null && (this.color.equalsIgnoreCase(topCard.getColor()) || topCard.isSkip());
            }
            // Reverse: can be placed if the color or the type matches
            if (this.isReverse()){
                return this.color != null && topCard.getColor() != null && (this.color.equalsIgnoreCase(topCard.getColor()) || topCard.isReverse());
            }
            // Number cards: can be played if color or value matches
            if (this.value != null && this.value.matches("[0-9]+")) {
                return this.color != null && topCard.getColor() != null && (this.color.equalsIgnoreCase(topCard.getColor())
                        || (topCard.getValue() != null && this.value.equalsIgnoreCase(topCard.getValue())));
            }
            // Default: only color match
            return this.color != null && topCard.getColor() != null && this.color.equalsIgnoreCase(topCard.getColor());
        } catch (Exception e) {
            // If any error, move is not valid
            return false;
        }
    }

    /**
     * Checks if the card is a reverse card.
     */
    public boolean isReverse() {
        String urlLower = this.url.toLowerCase();
        return urlLower.contains("reverse");
    }

    /**
     * Checks if the card is a skip card.
     */
    public boolean isSkip(){
        String urlLower = this.url.toLowerCase();
        return urlLower.contains("skip");
    }

    /**
     * Checks if the card is a skip or reverse card.
     * This method returns true if the card is either a skip or reverse card.
     */
    public boolean isSkipOrReverse(){
        return (this.isSkip() || this.isReverse());
    }

    /**
     * Checks if the card is a wild card.
     * A wild card is a special card that can be played at any time.
     *
     * @return true if the card is a wild card, false otherwise
     */
    public boolean isWildCard() {
        return this.url.endsWith("wild.png");
    }

    /**
     * Checks if the card is a wild card that can change the color.
     * This includes both the classic wild card and the +4 wild card.
     *
     * @return true if the card is a wild card that can change the color, false otherwise
     */
    public boolean isPlusFour() {
        return this.url.contains("4_wild_draw");
    }

    /**
     * Checks if the card is a +2 wild card.
     * This includes wild cards that draw two cards.
     *
     * @return true if the card is a +2 wild card, false otherwise
     */
    public boolean isPlusTwo() {
        return this.url.contains("2_wild_draw");
    }

    /**
     * Checks if the card is a colored wild card.
     * This includes wild cards that are colored (e.g., wild_draw_blue, wild_draw_red, etc.)
     * but not the classic +4 wild card or the classic wild card.
     */
    public boolean isColoredWild() {
        return this.isPlusTwo() && this.color != null;
    }

    /**
     * Checks if the card is a change color card.
     * This method returns true if the card is a wild card that can change the color.
     * This includes both the classic wild card and the +4 wild card.
     * @return true if the card is a change color card, false otherwise
     */
    public boolean isChangeColor() {
        return isWildCard();
    }

    /**
     * Checks if the card is a special card.
     * Special cards include wild cards, +2, +4, skip, and reverse cards.
     *
     * @return true if the card is a special card, false otherwise
     */
    public boolean isSpecial() {
        if ("BLACK".equalsIgnoreCase(this.color)) {
            return true;
        }

        if ("2".equals(this.value) && this.url.contains("wild_draw")) {
            return true;
        }
        if ("4".equals(this.value) && this.url.contains("wild_draw")) {
            return true;
        }

        if (this.url.contains("wild")) {
            return true;
        }

        String urlLower = this.url.toLowerCase();
        if (urlLower.contains("skip") || urlLower.contains("reverse")) {
            return true;
        }
        return false;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Restores the visuals of the card.
     * This method is used to recreate the Image and ImageView after serialization.
     */
    public void restoreVisuals() {
        this.image = new Image(String.valueOf(getClass().getResource(url)));
        this.cardImageView = createCardImageView();
    }

    /**
     * Sets the color of the card.
     * If the provided color is null or empty, it defaults to "BLACK" for wild cards.
     *
     * @param orDefault the color to set, or null/empty to use default
     */
    public void setColor(String orDefault) {
        if (orDefault == null || orDefault.isEmpty()) {
            this.color = "BLACK"; // Default color for wild cards
        } else {
            this.color = orDefault;
        }
        restoreVisuals(); // Update visuals after setting color
    }
}
