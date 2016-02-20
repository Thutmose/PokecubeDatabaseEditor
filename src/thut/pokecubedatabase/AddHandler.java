package thut.pokecubedatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
                    Main.instance.info.setText("ERROR, THAT ENTRY ALREADY EXISTS");
                    return;
                }
                else
                {
                    appendPokemon(newNum, newname);
                    Main.instance.info.setText("ADDED NEW ENTRY: " + newname);
                }
            }
            catch (ParserConfigurationException | SAXException | IOException e)
            {
                e.printStackTrace();
            }

        }
        System.out.println(Main.instance.add.getName() + " " + Main.instance.add.getActionCommand());

    }
    

    Element appendPokemon(int number, String name) throws ParserConfigurationException, IOException, SAXException
    {
        Element first = Main.instance.getEntry(null, -1, false);
        Element next = Main.instance.getEntry(null, number + 1, false);
        Element document = Main.instance.doc.getDocumentElement();
        Element ret = Main.instance.doc.createElement("Pokemon");
        ret.setAttribute("name", name);
        ret.setAttribute("number", "" + number);

        boolean append = first == next;
        if (append)
        {
            document.appendChild(ret);
        }
        else
        {
            document.insertBefore(ret, next);
        }

        Main.instance.writeXML(Main.file);
        return ret;
    }

}
