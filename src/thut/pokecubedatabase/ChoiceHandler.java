package thut.pokecubedatabase;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Comparator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
            String nodeName = main.moves ? main.moveNodeOptions.getSelectedItem()
                    : main.statNodeOptions.getSelectedItem();
            String var;
            if (!main.moves && (var = Main.statAttribs.get(nodeName)) != null)
            {
                String[] attrib = var.split(",");
                for (String s : attrib)
                {
                    main.attribChoice.add(s);
                }
            }
            else if(main.moves)
            {
                if (nodeName.equals("MISC"))
                {
                    main.attribChoice.add("moves");
                }
                else
                {
                    NodeList list = main.node.getElementsByTagName("MOVES");
                    Element movesNode;
                    if (list.getLength() == 0)
                    {
                        movesNode = main.doc.createElement("MOVES");
                        main.node.appendChild(movesNode);
                    }
                    else
                    {
                        movesNode = (Element) list.item(0);
                    }

                    Element subNode;

                    list = movesNode.getElementsByTagName(main.moveNodeOptions.getSelectedItem());
                    if (list.getLength() == 0)
                    {
                        subNode = main.doc.createElement(main.moveNodeOptions.getSelectedItem());
                        movesNode.appendChild(subNode);
                    }
                    else
                    {
                        subNode = (Element) list.item(0);
                    }
                    ArrayList<String> levels = new ArrayList<>();
                    for (int i = 0; i < subNode.getAttributes().getLength(); i++)
                    {
                        levels.add(subNode.getAttributes().item(i).getNodeName());
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
                    for(String l: levels)
                    {
                        main.attribChoice.add(l);
                    }

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
