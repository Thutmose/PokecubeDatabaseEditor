package thut.pokecubedatabase.serebii;

import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.io.IOException;

import thut.pokecubedatabase.Main;

public class SerebiiChecker
{
    public static int TOTALCOUNT = 801;

    public Panel      panel;
    Button            update;
    Button            mode;
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
        mode.addActionListener(handler);
        update.addActionListener(handler);
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
            moves.checkAttack(move, 10000);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
