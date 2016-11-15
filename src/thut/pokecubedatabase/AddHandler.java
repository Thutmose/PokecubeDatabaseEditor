package thut.pokecubedatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Comparator;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import thut.pokecubedatabase.XMLEntries.XMLPokedexEntry;

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
                    Main.instance.status.setText("ERROR, THAT ENTRY ALREADY EXISTS");
                    return;
                }
                else
                {
                    appendPokemon(newNum, newname);
                    Main.instance.status.setText("ADDED NEW ENTRY: " + newname);
                }
            }
            catch (ParserConfigurationException | SAXException | IOException | JAXBException e)
            {
                e.printStackTrace();
            }
        }
    }

    boolean appendPokemon(int number, String name) throws JAXBException
    {
        XMLPokedexEntry entry = new XMLPokedexEntry();
        entry.name = name;
        entry.number = number + "";
        XMLEntries.getDatabase(Main.file).pokemon.add(entry);
        XMLEntries.getDatabase(Main.file).pokemon.sort(new Comparator<XMLPokedexEntry>()
        {
            @Override
            public int compare(XMLPokedexEntry o1, XMLPokedexEntry o2)
            {
                if (o1.number.compareTo(o2.number) != 0) return o1.number.compareTo(o2.number);
                int diff = 0;
                if (Boolean.parseBoolean(o1.base) && !Boolean.parseBoolean(o2.base)) diff = -1;
                else if (Boolean.parseBoolean(o2.base) && !Boolean.parseBoolean(o1.base)) diff = 1;
                if (diff != 0) return diff;
                return o1.name.compareTo(o2.name);
            }
        });
        XMLEntries.getDatabase(Main.file).init();
        Main.instance.writeXML(Main.file);
        return entry != null;
    }

}
