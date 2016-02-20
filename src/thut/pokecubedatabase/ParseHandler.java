package thut.pokecubedatabase;

import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseHandler implements ActionListener
{

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        try
        {
            parseInput();
        }
        catch (ParserConfigurationException | IOException | SAXException e)
        {
            e.printStackTrace();
        }
    }

    void parseInput() throws ParserConfigurationException, IOException, SAXException
    {
        if (Main.instance.input.getText().trim().isEmpty()) { return; }

        Element node = null;

        String text = Main.instance.name.getText();
        node = Main.instance.getEntry(text);

        NodeList list;

        String type = Main.instance.moves ? "MOVES" : "STATS";
        Choice choice = Main.instance.moves ? Main.instance.moveNodeOptions : Main.instance.statNodeOptions;
        list = node.getElementsByTagName(type);
        Element selectedNode;
        if (list.getLength() == 0)
        {
            selectedNode = Main.instance.doc.createElement(type);
            node.appendChild(selectedNode);
        }
        else
        {
            selectedNode = (Element) list.item(0);
        }
        Element subNode;
        list = selectedNode.getElementsByTagName(choice.getSelectedItem());
        if (list.getLength() == 0)
        {
            subNode = Main.instance.doc.createElement(choice.getSelectedItem());
            selectedNode.appendChild(subNode);
        }
        else
        {
            subNode = (Element) list.item(0);
        }

        if (Main.instance.moves)
        {
            parseMoves(subNode);
        }
        else
        {
            parseStats(subNode);
        }
        Main.cleanUpEmpty(Main.instance.doc);
        Main.instance.updateBoxes(Main.instance.name);
    }

    private void parseStats(Element subNode)
    {
        if (subNode.getNodeName().equals("BASESTATS") || subNode.getNodeName().equals("EVYIELD"))
        {
            String[] attribs = Main.statAttribs.get("BASESTATS").split(",");
            String[] stats = Main.instance.input.getText().replace(",", " ").split(" ");

            if (stats.length != attribs.length)
            {
                Main.instance.info.setText(
                        "ERROR INVALID NUMBER OF " + (subNode.getNodeName().equals("BASESTATS") ? "STATS" : "EVS"));
                return;
            }

            for (int i = 0; i < stats.length; i++)
            {
                String s = stats[i];
                try
                {
                    Integer.parseInt(s.trim());
                }
                catch (NumberFormatException e)
                {
                    Main.instance.info.setText("ERROR, " + attribs[i] + " MUST BE AN INTEGER");
                    return;
                }
            }

            Attr attrib;
            for (int i = 0; i < attribs.length; i++)
            {
                String stat = attribs[i];
                subNode.removeAttribute(stat);
                attrib = Main.instance.doc.createAttribute(stat);
                attrib.setValue(stats[i].trim());
                subNode.setAttributeNode(attrib);
            }
        }

    }

    private void parseMoves(Element subNode)
    {
        if (Main.instance.moveNodeOptions.getSelectedItem().equals("MISC"))
        {
            String toParse = Main.instance.input.getText().trim().replace("\t", ":");

            String[] lines = toParse.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++)
            {
                lines[i] = convertName(lines[i]);
            }

            ArrayList<String> test = new ArrayList<>();
            outer:
            for (String s : lines)
            {
                for (String s1 : test)
                {
                    if (s.equals(s1)) continue outer;
                }
                test.add(s);
            }
            lines = test.toArray(new String[0]);

            String line = Arrays.toString(lines).replace("[", "").replace("]", "");
            subNode.removeAttribute("moves");

            Attr attrib;
            attrib = Main.instance.doc.createAttribute("moves");
            attrib.setValue(line);
            subNode.setAttributeNode(attrib);
        }
        else if (Main.instance.moveNodeOptions.getSelectedItem().equals("LVLUP"))
        {
            HashSet<Attr> toRemove = new HashSet<>();
            for (int i = 0; i < subNode.getAttributes().getLength(); i++)
            {
                toRemove.add((Attr) subNode.getAttributes().item(i));
            }
            for (Attr attrib : toRemove)
                subNode.removeAttributeNode(attrib);

            String toParse = Main.instance.input.getText().trim().replace("\t", ":");

            String[] lines = toParse.split("\\r?\\n");

            HashMap<String, String> levelmoves = new HashMap<>();

            for (String s : lines)
            {
                String[] args = s.split(":");
                String key = "lvl_" + args[0].trim();
                String val = "";
                if (levelmoves.containsKey(key))
                {
                    val = levelmoves.get(key) + ", ";
                }
                val += convertName(args[1]);
                levelmoves.put(key, val);
            }

            Attr attrib;
            for (String key : levelmoves.keySet())
            {
                attrib = Main.instance.doc.createAttribute(key);
                attrib.setValue(levelmoves.get(key));
                subNode.setAttributeNode(attrib);
            }
        }
    }

    private String convertName(String string)
    {
        String ret = "";
        String name = string.trim().toLowerCase().replaceAll("[^\\w\\s ]", "");
        String[] args = name.split(" ");
        for (int i = 0; i < args.length; i++)
        {
            ret += args[i];
        }
        return ret.toUpperCase();
    }

}
