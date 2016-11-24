package thut.pokecubedatabase.serebii;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import pokecube.core.database.moves.json.JsonMoves;
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
            if (serebii.mode.getActionCommand().equals("All"))
            {
                for (int i = 722; i <= SerebiiChecker.TOTALCOUNT; i++)
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
    }

}
