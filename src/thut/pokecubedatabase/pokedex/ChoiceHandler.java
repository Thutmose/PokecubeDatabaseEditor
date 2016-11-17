package thut.pokecubedatabase.pokedex;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;

import javax.xml.namespace.QName;

import thut.pokecubedatabase.Main;
import thut.pokecubedatabase.pokedex.XMLEntries.Moves;
import thut.pokecubedatabase.pokedex.XMLEntries.XMLPokedexEntry;
import thut.pokecubedatabase.pokedex.XMLEntries.Moves.LvlUp;

public class ChoiceHandler implements ItemListener, FocusListener, ComponentListener, MouseListener, MouseMotionListener
{
    final Main main;

    public ChoiceHandler(Main main)
    {
        this.main = main;
    }

    @Override
    public void focusGained(FocusEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void focusLost(FocusEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void itemStateChanged(ItemEvent arg0)
    {
        if (arg0 == null || arg0.getSource() == main.statNodeOptions || arg0.getSource() == main.moveNodeOptions)
        {
            main.attribChoice.removeAll();
            String selectedItem = main.moves ? main.moveNodeOptions.getSelectedItem()
                    : main.statNodeOptions.getSelectedItem();
            XMLPokedexEntry entry = Main.currentEntry;

            if (entry == null)
            {
                String text = main.name.getText();
                entry = main.getEntry(text);
            }
            if (entry == null)
            {
                int num = Integer.parseInt(main.number.getText());
                entry = main.getEntry(num);
            }

            if (!main.moves)
            {
                if (Main.validAttribs.containsKey(selectedItem))
                {
                    for (String s : Main.validAttribs.get(selectedItem))
                    {
                        main.attribChoice.add(s);
                    }
                }
            }
            else
            {
                Field selected = null;
                try
                {
                    selected = Moves.class.getDeclaredField(selectedItem);
                    Object value = selected.get(entry.moves);
                    if (value instanceof LvlUp)
                    {
                        LvlUp lvl = (LvlUp) value;

                        ArrayList<String> levels = new ArrayList<>();
                        for (QName n : lvl.values.keySet())
                        {
                            levels.add(n.toString());
                        }
                        levels.sort(new Comparator<String>()
                        {
                            @Override
                            public int compare(String o1, String o2)
                            {
                                int num1 = Integer.parseInt(o1.replace("lvl_", ""));
                                int num2 = Integer.parseInt(o2.replace("lvl_", ""));
                                return num1 - num2;
                            }
                        });
                        for (String s : levels)
                        {
                            main.attribChoice.add(s);
                        }
                    }
                }
                catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
        updateBoxes();
    }

    @Override
    public void componentHidden(ComponentEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void componentMoved(ComponentEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void componentResized(ComponentEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void componentShown(ComponentEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        // updateBoxes();
    }

    private void updateBoxes()
    {
        main.inputLabel.setText(main.moves ? Main.movesNodes.get(main.moveNodeOptions.getSelectedItem())
                : Main.statsNodes.get(main.statNodeOptions.getSelectedItem()));
        main.updateBoxes(main.name);
    }

}
