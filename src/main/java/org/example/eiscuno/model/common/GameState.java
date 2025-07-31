package org.example.eiscuno.model.common;

import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import java.io.Serializable;

public class GameState implements Serializable {
    private final Player humanPlayer;
    private final Player machinePlayer;
    private final Deck deck;
    private final Table table;
    private final GameUno game;

    // Cositas que no se guardan en esas clases pero que igual no está de mas
    private final boolean iaSaidUno;
    private final boolean isHumanTurn;
    private final boolean humanSaidUno;

    public GameState(Player human, Player machine, Deck deck, Table table, GameUno game, boolean iaSaidUno, boolean isHumanTurn, boolean humanSaidUno) {
        this.humanPlayer = human;
        this.machinePlayer = machine;
        this.deck = deck;
        this.table = table;
        this.game = game;
        this.iaSaidUno = iaSaidUno;
        this.isHumanTurn = isHumanTurn;
        this.humanSaidUno = humanSaidUno;
    }

    public Player getHumanPlayer() { return humanPlayer; }
    public Player getMachinePlayer() { return machinePlayer; }
    public Deck getDeck() { return deck; }
    public Table getTable() { return table; }
    public GameUno getGame() { return game; }
    public boolean getIASaidUno() { return iaSaidUno; }
    public boolean getHumanTurn() { return isHumanTurn; }
    public boolean getHumanSaidUno() { return humanSaidUno; }
}
