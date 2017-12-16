package thut.pokecubedatabase.serebii;

import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;

import thut.pokecubedatabase.Main;

public class SerebiiChecker
{
    public static int MAXNUM = 807;

    public Panel      panel;
    Button            update;
    Button            mode;
    Button            updateMoves;
    Button            mergeAnims;
    Button            outputLang;
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
        panel.add(outputLang = new Button("Output Lang"));
        mode.addActionListener(handler);
        update.addActionListener(handler);
        updateMoves.addActionListener(handler);
        mergeAnims.addActionListener(handler);
        outputLang.addActionListener(handler);
    }

    public void updatePokedexEntryFromSerebii(String name)
    {
        try
        {
            pokedex.parseForName(name);
        }
        catch (Exception e)
        {
            System.out.println(name + " Failed");
            Main.instance.addToStatus(name + " Failed");
            e.printStackTrace();
        }
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
            Main.instance.addToStatus(num + " Failed");
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
