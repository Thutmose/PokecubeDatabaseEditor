package thut.pokecubedatabase.pokedex;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import thut.pokecubedatabase.Main;
import thut.pokecubedatabase.pokedex.XMLEntries.XMLPokedexEntry;

public class AddHandler implements ActionListener
{

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (Main.instance.add.getActionCommand().equals("add new"))
        {
            Main.instance.add.setActionCommand("edit");
            Main.instance.add.setLabel("confirm");
        }
        else
        {
            Main.instance.add.setActionCommand("add new");
            Main.instance.add.setLabel("add new");

            String newname = Main.instance.name.getText();
            int newNum = Integer.parseInt(Main.instance.number.getText());
            try
            {
                if (Main.instance.hasEntry(newname, newNum))
                {
                    Main.instance.setStatus("ERROR, THAT ENTRY ALREADY EXISTS");
                    return;
                }
                else
                {
                    appendPokemon(newNum, newname);
                    Main.instance.setStatus("ADDED NEW ENTRY: " + newname);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    boolean appendPokemon(int number, String name) throws Exception
    {
        XMLPokedexEntry entry = new XMLPokedexEntry();
        entry.name = name;
        entry.number = number;
        XMLEntries.getDatabase(Main.pokedexfile).pokemon.add(entry);
        XMLEntries.getDatabase(Main.pokedexfile).pokemon.sort(XMLEntries.ENTRYSORTER);
        XMLEntries.getDatabase(Main.pokedexfile).init();
        Main.instance.writeXML(Main.pokedexfile);
        return entry != null;
    }

}
