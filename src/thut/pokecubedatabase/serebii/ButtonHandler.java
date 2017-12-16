package thut.pokecubedatabase.serebii;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import thut.pokecubedatabase.Main;
import thut.pokecubedatabase.pokedex.XMLEntries;
import thut.pokecubedatabase.pokedex.XMLEntries.XMLPokedexEntry;

public class ButtonHandler implements ActionListener
{
    SerebiiChecker serebii;

    public ButtonHandler(SerebiiChecker checker)
    {
        this.serebii = checker;
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == serebii.update)
        {
            PokedexChecker.pokedex = "pokedex-sm";
            if (serebii.mode.getActionCommand().equals("All"))
            {
                for (int i = 1; i <= SerebiiChecker.MAXNUM; i++)
                    serebii.updatePokedexEntryFromSerebii(i);
            }
            else
            {
                int num = Integer.parseInt(Main.instance.number.getText());
                String name = Main.instance.name.getText();
                XMLPokedexEntry old = XMLEntries.getDatabase(Main.pokedexfile).getEntry(null, num, false, -1);
                if (old != null && !old.name.equals(name)) serebii.updatePokedexEntryFromSerebii(name);
                else serebii.updatePokedexEntryFromSerebii(num);
            }
            return;
        }
        else if (evt.getSource() == serebii.mode)
        {
            if (serebii.mode.getActionCommand().equals("Selected"))
            {
                serebii.mode.setActionCommand("All");
                serebii.mode.setLabel("All");
            }
            else
            {
                serebii.mode.setActionCommand("Selected");
                serebii.mode.setLabel("Selected");
            }
        }
        else if (evt.getSource() == serebii.mergeAnims)
        {
            try
            {
                JsonMoves.merge(new File("./animations.json"), Main.movesFile);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (evt.getSource() == serebii.updateMoves)
        {
            try
            {
                serebii.moves.checkAttack("absorb", 10000, false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (evt.getSource() == serebii.outputLang)
        {
            File mobsLang = new File("./en_US_mobs.lang");
            File movesLang = new File("./en_US_moves.lang");

            MovesJson validMoves = JsonMoves.getMoves(Main.movesFile);
            try
            {
                FileWriter writer = new FileWriter(movesLang);
                PrintWriter out = new PrintWriter(writer);
                out.println("#Pokecube translation");
                for (MoveJsonEntry move : validMoves.moves)
                {
                    out.println("pokemob.move." + move.name + "=" + move.readableName);
                }
                writer.close();
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

}
