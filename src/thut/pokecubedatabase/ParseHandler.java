package thut.pokecubedatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import javax.xml.namespace.QName;

import thut.pokecubedatabase.XMLEntries.StatsNode;
import thut.pokecubedatabase.XMLEntries.StatsNode.Stats;
import thut.pokecubedatabase.XMLEntries.XMLPokedexEntry;

public class ParseHandler implements ActionListener
{

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        parseInput(evt.getSource() == Main.instance.parse);
    }

    void parseInput(boolean parse)
    {
        if (!parse)
        {
            Main.instance.input.setText("");
            return;
        }
        if (Main.instance.moves) parseMoves();
        else parseStats();
        Main.instance.updateBoxes(null);
    }

    private void parseStats()
    {
        try
        {
            XMLPokedexEntry entry = Main.currentEntry;

            if (entry == null)
            {
                String text = Main.instance.name.getText();
                entry = Main.instance.getEntry(text);
            }
            if (entry == null)
            {
                int num = Integer.parseInt(Main.instance.number.getText());
                entry = Main.instance.getEntry(num);
            }

            String selectedItem = Main.instance.statNodeOptions.getSelectedItem();
            String selectedAtrib = Main.instance.attribChoice.getSelectedItem();
            Field selected = StatsNode.class.getDeclaredField(selectedItem);
            Object value = selected.get(entry.stats);
            String input = Main.instance.input.getText();
            if (input.isEmpty()) input = null;

            boolean simple = ((selected.getType() == Double.TYPE) || (selected.getType() == Double.class))
                    || ((selected.getType() == Integer.TYPE) || (selected.getType() == Integer.class))
                    || ((selected.getType() == Float.TYPE) || (selected.getType() == Float.class))
                    || ((selected.getType() == Boolean.TYPE) || (selected.getType() == Boolean.class))
                    || ((value instanceof String) || (selected.getType() == String.class));

            if (selectedAtrib == null)
            {
                if (simple)
                {
                    Object val = input;
                    if ((selected.getType() == Double.TYPE) || (selected.getType() == Double.class))
                        val = Double.parseDouble(input);
                    if ((selected.getType() == Integer.TYPE) || (selected.getType() == Integer.class))
                        val = Integer.parseInt(input);
                    if ((selected.getType() == Float.TYPE) || (selected.getType() == Float.class))
                        val = Float.parseFloat(input);
                    if ((selected.getType() == Boolean.TYPE) || (selected.getType() == Boolean.class))
                        val = Boolean.parseBoolean(input);
                    selected.set(entry.stats, val);
                }
            }
            else if (value instanceof Stats)
            {
                Stats stat = (Stats) value;
                if (input != null) stat.values.put(new QName(selectedAtrib), input);
                else stat.values.remove(new QName(selectedAtrib));
            }
            if (input != null) Main.instance.info.setText(input);
            else Main.instance.info.setText("");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void parseMoves()
    {

    }

    public static String convertName(String string)
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
