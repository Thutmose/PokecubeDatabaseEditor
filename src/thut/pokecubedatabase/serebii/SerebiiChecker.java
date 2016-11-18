package thut.pokecubedatabase.serebii;

import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;

import thut.pokecubedatabase.Main;

public class SerebiiChecker
{
    public static int TOTALCOUNT = 801;

    public Panel      panel;
    Button            update;
    Button            mode;
    Button            updateMoves;
    Button            mergeAnims;
    ButtonHandler     handler;
    PokedexChecker    pokedex    = new PokedexChecker();
    MovesChecker      moves      = new MovesChecker();

    public SerebiiChecker(Main main)
    {
        this.panel = new Panel();
        handler = new ButtonHandler(this);
        panel.add(new Label("Update Mode:"));
        panel.add(mode = new Button("Selected"));
        panel.add(update = new Button("Update From Serebii"));
        panel.add(updateMoves = new Button("Update Moves"));
        panel.add(mergeAnims = new Button("Merge Animations"));
        mode.addActionListener(handler);
        update.addActionListener(handler);
        updateMoves.addActionListener(handler);
        mergeAnims.addActionListener(handler);
    }

    public void updatePokedexEntryFromSerebii(int num)
    {
        try
        {
            pokedex.parseForNumber(num);
        }
        catch (Exception e)
        {
            System.out.println(num + " Failed");
            e.printStackTrace();
        }
    }

    public void updateMoveEntryFromSerebii(String move)
    {
        try
        {
            moves.checkAttack(move, 10000, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
