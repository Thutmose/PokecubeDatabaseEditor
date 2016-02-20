package thut.pokecubedatabase;

// Using AWT container and component classes
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
// Using AWT event classes and listener interfaces
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

// An AWT GUI program inherits from the top-level container java.awt.Frame
public class Main extends Frame implements ActionListener, WindowListener
{
    final static String defaultFile      = "gen1.xml";
    static File         file             = new File("./" + defaultFile);
    /**
     * 
     */
    static final long   serialVersionUID = 1L;
    Label               lblInput;
    Label               nodeLabel;

    // View Buttons
    TextField fileName;

    TextField name;
    TextField number;

    Button next;
    Button prev;

    Button add;

    TextField label;
    TextField info;

    Button toggle;

    // Edit buttons

    Label inputLabel;

    TextArea input;
    Button   parse;

    Choice statNodeOptions;
    Choice moveNodeOptions;
    Choice attribChoice;

    ChoiceHandler choiceHandler = new ChoiceHandler(this);

    boolean moves = false;

    Document doc;
    Element  node = null;

    static HashMap<String, String> statsNodes  = new HashMap<>();
    static HashMap<String, String> movesNodes  = new HashMap<>();
    static HashMap<String, String> statAttribs = new HashMap<>();

    static
    {
        statsNodes.put("EVOLUTIONMODE", "Method/s of evolution");
        statsNodes.put("CAPTURERATE", "The Capture Rate");
        statsNodes.put("EVOLUTIONANIMATION", "related to the colour of the evolution animation");
        statsNodes.put("RIDDENOFFSET", "Offset for position of rider");
        statsNodes.put("PREY", "Species this pokemon eats");
        statsNodes.put("SPECIES", "Species this pokemon is");
        statsNodes.put("MOVEMENTTYPE", "options:normal, floating, flying, water");
        statsNodes.put("SPECIALEGGSPECIESRULES", "special rules for what eggs are produced from varied parents");
        statsNodes.put("MASSKG", "The mass in kg");
        statsNodes.put("EVOLVESTO", "the pokemon's number which this evolves to");
        statsNodes.put("EXPYIELD", "Base EXP from defeating");
        statsNodes.put("FOODDROP", "\"food\" item dropped, a guarenteed drop when wild one is killed.");
        statsNodes.put("COMMONDROP", "list of common drops");
        statsNodes.put("RAREDROP", "list of rare drops");
        statsNodes.put("HELDITEM", "list of items held randomly by wild versions");
        statsNodes.put("FOODMATERIAL", "materials this pokemon can eat (example, watr, light)");
        statsNodes.put("BASEFRIENDSHIP", "Base friendship for the pokemon");
        statsNodes.put("BIOMESALLNEEDED", "spawn biomes, where all are needed to spawn");
        statsNodes.put("BIOMESANYACCEPTABLE", "spawn biomes where any are needed to spawn");
        statsNodes.put("EXCLUDEDBIOMES", "biomes it cannot spawn in");
        statsNodes.put("SPECIALCASES", "special spawn rules");
        statsNodes.put("ABILITY", "the abilities available to the pokemon");
        statsNodes.put("TYPE", "the pokemon's types");
        statsNodes.put("SIZES", "the sizes of the pokemon (l,w,h");
        statsNodes.put("EVYIELD", "evs gained from defeating this pokemon");
        statsNodes.put("BASESTATS", "this pokemon's base stats");
        statsNodes.put("LOGIC", "logic states");
        statsNodes.put("EXPERIENCEMODE", "The function used for mapping exp to level");
        statsNodes.put("PARTICLEEFFECTS", "particles produced");
        statsNodes.put("GENDERRATIO", "ratio of genders");
        statsNodes.put("INTERACTIONLOGIC", "item interaction logic");
        statsNodes.put("SHADOWREPLACEMENTS", "list of mobs to replace with shadow versions of this pokemon");
        statsNodes.put("HATEDMATERIALRULES", "list of rules for what materials this pokemon hates");
        statsNodes.put("ACTIVETIMES", "list of times this pokemon is active");

        statAttribs.put("LOGIC", "shoulder" + "," + "fly" + "," + "dive" + "," + "dye" + "," + "stationary");
        statAttribs.put("TYPE", "type1,type2");
        statAttribs.put("ABILITY", "normal,hidden");
        statAttribs.put("SIZES", "height,length,width");
        statAttribs.put("BASESTATS", "hp,atk,def,spatk,spdef,spd");
        statAttribs.put("EVYIELD", "hp,atk,def,spatk,spdef,spd");

        movesNodes.put("LVLUP", "moves learned on lvl up");
        movesNodes.put("MISC", "any other obtainable move");
    }

    public Main()
    {
        setLayout(new GridLayout(3, 1));
        addWindowListener(this);

        Panel view = new Panel(new FlowLayout());
        Panel edit = new Panel(new BorderLayout());

        Panel file = new Panel(new GridLayout(2, 1));
        file.add(new Label("File"));
        file.add(fileName = new TextField(defaultFile));
        fileName.addActionListener(this);
        view.add(file);

        Panel pokemonControlButtons = new Panel(new GridLayout(3, 1));

        pokemonControlButtons.add(next = new Button("next"));
        pokemonControlButtons.add(prev = new Button("prev"));
        pokemonControlButtons.add(add = new Button("add new"));

        view.add(pokemonControlButtons);

        Panel selection = new Panel(new GridLayout(3, 1));

        lblInput = new Label("Pokemon");
        selection.add(lblInput);
        name = new TextField("", 10);
        selection.add(name);
        number = new TextField(10);
        selection.add(number);
        view.add(selection);

        Panel buttons = new Panel(new BorderLayout(2, 3));
        buttons.add(label = new TextField("label", 5), BorderLayout.CENTER);
        view.add(buttons);
        info = new TextField(50);

        Panel infoLayout = new Panel(new GridLayout(3, 1));
        infoLayout.add(nodeLabel = new Label("nodeLabel"));
        infoLayout.add(info);
        view.add(infoLayout);

        info.setEditable(false);
        label.setEditable(false);

        view.add(toggle = new Button("Stats"));

        inputLabel = new Label();
        input = new TextArea(10, 50);

        Panel editInputs = new Panel(new GridLayout(1, 3));

        Panel optionsPanel = new Panel(new GridLayout(1, 2));
        statNodeOptions = new Choice();
        statNodeOptions.addItemListener(choiceHandler);
        statNodeOptions.addFocusListener(choiceHandler);
        statNodeOptions.addComponentListener(choiceHandler);
        statNodeOptions.addMouseListener(choiceHandler);
        statNodeOptions.addMouseMotionListener(choiceHandler);
        ArrayList<String> options = new ArrayList<>(statsNodes.keySet());
        Collections.sort(options);
        for (String s : options)
        {
            statNodeOptions.add(s);
        }
        moveNodeOptions = new Choice();
        moveNodeOptions.addItemListener(choiceHandler);
        options = new ArrayList<>(movesNodes.keySet());
        Collections.sort(options);
        for (String s : options)
        {
            moveNodeOptions.add(s);
        }

        editInputs.add(statNodeOptions);
        editInputs.add(moveNodeOptions);
        editInputs.add(attribChoice = new Choice());
        attribChoice.addItemListener(choiceHandler);
        optionsPanel.add(inputLabel);
        optionsPanel.add(editInputs);

        edit.add(optionsPanel, BorderLayout.NORTH);
        edit.add(input, BorderLayout.CENTER);
        edit.add(parse = new Button("Parse"), BorderLayout.SOUTH);

        add(view);
        add(edit);

        updateBoxes(name);
        statNodeOptions.setEnabled(!moves);
        moveNodeOptions.setEnabled(moves);
        choiceHandler.itemStateChanged(null);
        setTitle("Pokecube Database Info");
        setSize(1350, 500);
        setVisible(true);

        // I am lazy.
        for (Field field : getClass().getDeclaredFields())
        {
            Object o;
            try
            {
                o = field.get(this);
                if (o instanceof TextField)
                {
                    ((TextField) o).addActionListener(this);
                }
                if (o instanceof Button)
                {
                    ((Button) o).addActionListener(this);
                }
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    /** The entry main() method */
    public static void main(String[] args)
    {
        // Invoke the constructor to setup the GUI, by allocating an anonymous
        // instance
        new Main();
    }

    @Override
    public void windowActivated(WindowEvent e)
    {
    }

    @Override
    public void windowClosed(WindowEvent e)
    {
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
    }

    @Override
    public void windowOpened(WindowEvent e)
    {
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == add)
        {
            if (add.getActionCommand().equals("add new"))
            {
                add.setActionCommand("edit");
                add.setLabel("confirm");
            }
            else
            {
                add.setActionCommand("add new");
                add.setLabel("add new");

                String newname = name.getText();
                int newNum = Integer.parseInt(number.getText());
                try
                {
                    if (hasEntry(file, newname, newNum))
                    {
                        info.setText("ERROR, THAT ENTRY ALREADY EXISTS");
                        return;
                    }
                    else
                    {
                        appendPokemon(newNum, newname);
                        info.setText("ADDED NEW ENTRY: "+newname);
                    }
                }
                catch (ParserConfigurationException | SAXException | IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                

            }
            System.out.println(add.getName() + " " + add.getActionCommand());
            return;
        }
        if (!add.getActionCommand().equals("add new")) { return; }

        statNodeOptions.setEnabled(!moves);
        moveNodeOptions.setEnabled(moves);
        if (evt.getSource() == fileName)
        {
            doc = null;
            file = null;
            file = new File("./" + fileName.getText());
            name.setText("");
            number.setText("");
            updateBoxes(name);
            return;
        }
        System.out.println(evt);
        if (evt.getSource() == parse)
        {

            try
            {
                parseInput();
            }
            catch (ParserConfigurationException | IOException | SAXException e)
            {
                e.printStackTrace();
            }
            return;
        }

        if (evt.getSource() == next)
        {
            int num = Integer.parseInt(number.getText()) + 1;
            number.setText("" + num);
            updateBoxes(number);
            return;
        }
        else if (evt.getSource() == prev)
        {
            int num = Integer.parseInt(number.getText()) - 1;
            number.setText("" + num);
            updateBoxes(number);
            return;
        }

        if (evt.getSource() == toggle)
        {
            moves = !moves;
            if (moves) toggle.setLabel("Moves");
            else toggle.setLabel("Stats");
            info.setText("");
            choiceHandler.itemStateChanged(null);
        }
        updateBoxes(evt.getSource());
    }

    void parseInput() throws ParserConfigurationException, IOException, SAXException
    {
        if (input.getText().trim().isEmpty()) { return; }

        Element node = null;

        String text = name.getText();
        node = getEntry(file, text);

        NodeList list;

        String type = moves ? "MOVES" : "STATS";
        Choice choice = moves ? moveNodeOptions : statNodeOptions;
        list = node.getElementsByTagName(type);
        Element selectedNode;
        if (list.getLength() == 0)
        {
            selectedNode = doc.createElement(type);
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
            subNode = doc.createElement(choice.getSelectedItem());
            selectedNode.appendChild(subNode);
        }
        else
        {
            subNode = (Element) list.item(0);
        }

        if (moves)
        {
            parseMoves(subNode);
        }
        else
        {
            parseStats(subNode);
        }
        cleanUpEmpty();
        updateBoxes(name);
    }

    private void parseStats(Element subNode)
    {
        System.out.println("Stats");
        if (subNode.getNodeName().equals("BASESTATS") || subNode.getNodeName().equals("EVYIELD"))
        {
            String[] attribs = statAttribs.get("BASESTATS").split(",");
            String[] stats = input.getText().split(" ");

            if (stats.length != attribs.length)
            {
                info.setText(
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
                    info.setText("ERROR, " + attribs[i] + " MUST BE AN INTEGER");
                    return;
                }
            }

            Attr attrib;
            for (int i = 0; i < attribs.length; i++)
            {
                String stat = attribs[i];
                subNode.removeAttribute(stat);
                attrib = doc.createAttribute(stat);
                attrib.setValue(stats[i].trim());
                subNode.setAttributeNode(attrib);
            }
        }

    }

    private void parseMoves(Element subNode)
    {
        if (moveNodeOptions.getSelectedItem().equals("MISC"))
        {
            String toParse = input.getText().trim().replace("\t", ":");

            String[] lines = toParse.split("\\r?\\n");

            for (int i = 0; i < lines.length; i++)
            {
                lines[i] = convertName(lines[i]);
            }
            String line = Arrays.toString(lines).replace("[", "").replace("]", "");
            subNode.removeAttribute("moves");

            Attr attrib;
            attrib = doc.createAttribute("moves");
            attrib.setValue(line);
            subNode.setAttributeNode(attrib);
        }
        else if (moveNodeOptions.getSelectedItem().equals("LVLUP"))
        {
            HashSet<Attr> toRemove = new HashSet<>();
            for (int i = 0; i < subNode.getAttributes().getLength(); i++)
            {
                toRemove.add((Attr) subNode.getAttributes().item(i));
            }
            for (Attr attrib : toRemove)
                subNode.removeAttributeNode(attrib);

            String toParse = input.getText().trim().replace("\t", ":");

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
                attrib = doc.createAttribute(key);
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

    void updateBoxes(Object source)
    {

        statNodeOptions.setEnabled(!moves);
        moveNodeOptions.setEnabled(moves);

        inputLabel.setText(moves ? movesNodes.get(moveNodeOptions.getSelectedItem())
                : statsNodes.get(statNodeOptions.getSelectedItem()));

        try
        {

            boolean toEdit = false;

            if (source == name || source instanceof Button)
            {
                String text = name.getText();
                node = getEntry(file, text);
            }
            else if (source == number)
            {
                int num = Integer.parseInt(number.getText());
                node = getEntry(file, num);
            }

            if (node == null) { return; }

            number.setText(node.getAttribute("number"));
            name.setText(node.getAttribute("name"));

            NodeList list;

            if (!moves)
            {
                list = node.getElementsByTagName("STATS");
                Element statsNode;
                Element subNode;
                if (list.getLength() == 0)
                {
                    statsNode = doc.createElement("STATS");
                    node.appendChild(statsNode);
                }
                else
                {
                    statsNode = (Element) list.item(0);
                }

                list = statsNode.getElementsByTagName(statNodeOptions.getSelectedItem());

                if (list.getLength() == 0)
                {
                    subNode = doc.createElement(statNodeOptions.getSelectedItem());
                    statsNode.appendChild(subNode);
                }
                else
                {
                    subNode = (Element) list.item(0);
                }

                nodeLabel.setText(subNode.getNodeName());
                if (!subNode.hasAttributes() || subNode.getAttributes().getLength() == 0)
                {
                    label.setText("n/a");
                    info.setText(subNode.getFirstChild().getNodeValue());
                }
                else
                {
                    Attr attrib = subNode.getAttributeNode(attribChoice.getSelectedItem());
                    if (attrib != null)
                    {
                        label.setText(attrib.getName());
                        info.setText(attrib.getNodeValue());
                    }
                }
            }
            else
            {
                list = node.getElementsByTagName("MOVES");
                Element movesNode;
                if (list.getLength() == 0)
                {
                    movesNode = doc.createElement("MOVES");
                    node.appendChild(movesNode);
                }
                else
                {
                    movesNode = (Element) list.item(0);
                }

                Element subNode;

                list = movesNode.getElementsByTagName(moveNodeOptions.getSelectedItem());

                if (list.getLength() == 0)
                {
                    subNode = doc.createElement(moveNodeOptions.getSelectedItem());
                    movesNode.appendChild(subNode);
                }
                else
                {
                    subNode = (Element) list.item(0);
                }

                nodeLabel.setText(subNode.getNodeName());
                if (!subNode.hasAttributes() || subNode.getAttributes().getLength() == 0)
                {
                    label.setText("n/a");
                    info.setText(subNode.getFirstChild().getNodeValue());
                }
                else
                {
                    Attr attrib = subNode.getAttributeNode(attribChoice.getSelectedItem());
                    if (attrib != null)
                    {
                        label.setText(attrib.getName());
                        info.setText(attrib.getNodeValue());
                    }
                }
            }
            if (toEdit)
            {
                writeXML(file);
            }
        }
        catch (Exception e)
        {
            info.setText("");
        }
    }

    Element getEntry(File file, String name) throws ParserConfigurationException, IOException, SAXException
    {
        return getEntry(file, name, -1);
    }

    Element getEntry(File file, int number) throws ParserConfigurationException, IOException, SAXException
    {
        return getEntry(file, null, number);
    }

    Element getEntry(File file, String name, int number) throws ParserConfigurationException, IOException, SAXException
    {
        return getEntry(file, name, number, true);
    }

    boolean hasEntry(File file, String name, int number) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.parse(file);
        NodeList entries = doc.getElementsByTagName("Pokemon");
        for (int i = 0; i < entries.getLength(); i++)
        {
            Element pokemonNode = (Element) entries.item(i);
            int num = Integer.parseInt(pokemonNode.getAttribute("number"));
            String name2 = pokemonNode.getAttribute("name");
            if (num == number && name2.equalsIgnoreCase(name)) return true;
        }

        return false;
    }

    Element getEntry(File file, String name, int number, boolean newDoc)
            throws ParserConfigurationException, IOException, SAXException
    {
        if (newDoc)
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(file);
        }
        NodeList entries = doc.getElementsByTagName("Pokemon");
        Element first = null;
        for (int i = 0; i < entries.getLength(); i++)
        {
            Element pokemonNode = (Element) entries.item(i);
            if (i == 0) first = pokemonNode;
            int num = Integer.parseInt(pokemonNode.getAttribute("number"));
            String name2 = pokemonNode.getAttribute("name");
            if (num == number || name2.equalsIgnoreCase(name)) return pokemonNode;
        }
        return first;
    }

    void cleanUpEmpty()
    {
        NodeList entries = doc.getChildNodes();
        for (int i = 0; i < entries.getLength(); i++)
        {
            if (entries.item(i).hasChildNodes())
            {
                cleanUpEmpty((Element) entries.item(i));
            }
        }
        try
        {
            writeXML(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void cleanUpEmpty(Element element)
    {
        boolean empty = !(element.hasChildNodes() || element.hasAttributes());
        if (empty) Thread.dumpStack();
        NodeList entries = element.getChildNodes();
        HashSet<Element> toRemove = new HashSet<>();
        for (int i = 0; i < entries.getLength(); i++)
        {
            boolean attribs = entries.item(i).hasAttributes();
            if (entries.item(i).hasChildNodes())
            {
                cleanUpEmpty((Element) entries.item(i));
            }
            else if (!attribs && (entries.item(i).getNodeValue() == null || entries.item(i).getNodeValue().isEmpty()))
            {
                toRemove.add((Element) entries.item(i));
            }
        }
        for (Element oldChild : toRemove)
            element.removeChild(oldChild);
    }

    void writeXML(File file) throws IOException
    {
        try
        {
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);

            // Output to console for testing
            transformer.transform(source, result);

            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();

                while (reader.ready())
                {
                    line += reader.readLine();
                }
                reader.close();
                line = new XmlFormatter().format(line);
                FileWriter writer;
                try
                {
                    writer = new FileWriter(file);
                    writer.write(line);
                    writer.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        catch (TransformerException tfe)
        {
            tfe.printStackTrace();
        }
    }

    Element appendPokemon(int number, String name) throws ParserConfigurationException, IOException, SAXException
    {
        Element first = getEntry(file, null, -1, true);
        Element next = getEntry(file, null, number + 1, false);
        Element document = doc.getDocumentElement();
        Element ret = doc.createElement("Pokemon");
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

        writeXML(file);

        return ret;
    }

    static class XmlFormatter
    {

        public XmlFormatter()
        {
        }

        public String format(String unformattedXml)
        {
            try
            {
                unformattedXml = unformattedXml.replace("\n", "");
                unformattedXml = unformattedXml.replace("( )+(<)", "<");

                final Document document = parseXmlFile(unformattedXml);

                OutputFormat format = new OutputFormat(document);
                // format.setLineWidth(65);
                format.setIndenting(true);
                format.setIndent(4);
                Writer out = new StringWriter();
                XMLSerializer serializer = new XMLSerializer(out, format);
                serializer.serialize(document);

                return out.toString();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        Document parseXmlFile(String in)
        {
            try
            {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(in));
                return db.parse(is);
            }
            catch (ParserConfigurationException e)
            {
                throw new RuntimeException(e);
            }
            catch (SAXException e)
            {
                throw new RuntimeException(e);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}