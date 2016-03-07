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
import java.util.ArrayList;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import thut.pokecubedatabase.serebii.SerebiiChecker;

// An AWT GUI program inherits from the top-level container java.awt.Frame
public class Main extends Frame implements ActionListener, WindowListener
{
    final static String defaultFile      = "pokemobs.xml";
    public static Main  instance;
    static File         file             = new File("./" + defaultFile);
    /**
     * 
     */
    static final long   serialVersionUID = 1L;
    Label               lblInput;
    Label               nodeLabel;

    // View Buttons
    TextField fileName;

    TextField        name;
    public TextField number;

    Button next;
    Button prev;

    Button add;

    TextField       label;
    TextField       info;
    public TextArea status;

    Button toggle;
    Button save;

    // Edit buttons

    TextArea inputLabel;

    TextArea input;
    Button   parse;
    Button   clear;

    Choice statNodeOptions;
    Choice moveNodeOptions;
    Choice attribChoice;

    ChoiceHandler choiceHandler = new ChoiceHandler(this);

    TextField doc1;
    TextField doc2;
    TextField output;

    Button merge;

    public boolean moves = false;

    public Document doc;
    public Element  node = null;

    public static HashMap<String, String> statsNodes  = new HashMap<>();
    public static HashMap<String, String> movesNodes  = new HashMap<>();
    public static HashMap<String, String> statAttribs = new HashMap<>();

    SerebiiChecker serebii;

    static
    {
        statsNodes.put("EVOLUTIONMODE",
                "Method of evolution, some pokemon have multiple evos, some have none. See Eevee"
                        + ".\nFormat: <evo1mode>:<requirement> <evo2mode>:<requirement>"
                        + ".\nValid modes: Level, Stone:<stonetype, example water, fire, etc>, Happiness, Move:<movename>, Trade, etc"
                        + ".\nModes can also have modifiers, see Gligar(207) for example of item and time requirements");
        statsNodes.put("CAPTURERATE", "The Capture Rate");
        statsNodes.put("EVOLUTIONANIMATION", "related to the colour of the evolution animation");
//        statsNodes.put("RIDDENOFFSET", "Offset for position of rider");
        statsNodes.put("PREY", "Species this pokemon eats." + "\nFormat: <Species1> <Species2>");
        statsNodes.put("SPECIES", "Species this pokemon is." + "\nFormat: <Species1> <Species2>");
        statsNodes.put("MOVEMENTTYPE", "options:normal, floating, flying, water");
        statsNodes.put("SPECIALEGGSPECIESRULES",
                "special rules for what eggs are produced from varied parents, see NidoranF for example."
                        + "\nFormat, values are pokedex numbers: <father>:<child>`<child>;<father>:<child>`<child>");
        statsNodes.put("MASSKG", "The mass in kg");
        statsNodes.put("EVOLVESTO", "the pokemon's number which this evolves to.\nFormat: <evo1nb> <evo2nb>");
        statsNodes.put("EXPYIELD", "Base EXP from defeating");
        statsNodes.put("FOODDROP", "\"food\" item dropped, a guarenteed drop when wild one is killed."
                + "\nFormat: <number>:<itemname>:<metadata>" + "\nmetadata is optional.");
        statsNodes.put("COMMONDROP",
                "list of common drops."
                        + "\nFormat: <number>:<itemname>:<metadata>:<chance> <number>:<itemname>:<metadata>:<chance>"
                        + "\nmetadata is optional, but if chance is used, it is needed.");
        statsNodes.put("RAREDROP",
                "list of rare drops."
                        + "\nFormat: <number>:<itemname>:<metadata>:<chance> <number>:<itemname>:<metadata>:<chance>"
                        + "\nmetadata is optional, but if chance is used, it is needed.");
        statsNodes.put("HELDITEM",
                "list of items held randomly by wild versions."
                        + "\nFormat: <number>:<itemname>:<metadata>:<chance> <number>:<itemname>:<metadata>:<chance>"
                        + "\nmetadata is optional, but if chance is used, it is needed.");
        statsNodes.put("FOODMATERIAL", "materials this pokemon can eat (example, water, rock, light)");
        statsNodes.put("BASEFRIENDSHIP", "Base friendship for the pokemon");
        statsNodes.put("BIOMESALLNEEDED",
                "spawn biomes, where all are needed to spawn."
                        + "\nFormat: <biometype1> <biometype2> <rate1>:<max1><min1>; <biometype3> <biometype4> <rate2>:<max2>:<min2>"
                        + "\nMax and min are optional, but if you have one, you need both.  Their default values are 4:2."
                        + "\nIn this example, the pokemob can spawn in a <biometype2> and <biometype1> biome, "
                        + "\nor a <biometype3> and <biometype4> biome.");
        statsNodes.put("BIOMESANYACCEPTABLE",
                "spawn biomes where any are needed to spawn, any biome type listed here will be allowed."
                        + "\nFormat: <biometype1> <biometype2> <rate1>:<max1><min1>; <biometype3> <biometype4> <rate2>:<max2>:<min2>"
                        + "\nIn this case, <rate1> is the spawn rate in <biometype1> or <biometype2> and"
                        + "\n<rate2> is the spawn rate in <biometype3> or <biometype4>"
                        + "\notherwise same format as for BIOMESALLNEEDED");
        statsNodes.put("EXCLUDEDBIOMES", "biomes it cannot spawn in. Types listed here are blacklisted for spawning."
                + "\nFormat: <badBiome1> <badBiome2>");
        statsNodes.put("SPECIALCASES", "special spawn rules." + "\nFormat: <option1> <option2>"
                + "\nValid Options: starter, day, night, legendary, fossil, water, water+."
                + "\nday/night are light level for spawn, starter adds to starter list, legendary has much lower spawn rate"
                + "\nfossil will not spawn naturally, water only spawns in water, water+ spawns on land and in water.");
        statsNodes.put("ABILITY",
                "the abilities available to the pokemon.\n"
                        + "normal: the basic abilities, Format: <ability1>, <ability2>"
                        + "\nhidden: the hidden ability, Format: <ability>");
        statsNodes.put("TYPE", "the pokemon's types." + "\ntype1: the first type listed, Format: <type1>"
                + "\ntype2: the second type listed, optional, Format: <type2>");
        statsNodes.put("SIZES",
                "the dimensions of the pokemon." + "\nheight: how tall is the hitbox, Format: <height in meters>"
                        + "\nwidth: how wide is the hitbox, Format: <width in meters>"
                        + "\nlength: how long is the hitbox, Format: <length in meters>");
        statsNodes.put("EVYIELD", "evs gained from defeating this pokemon, blank values mean no EV of that stat.");
        statsNodes.put("BASESTATS", "this pokemon's base stats");
        statsNodes.put("LOGIC", "logic states.\n"
                + "shoulder: should the pokemob jump on shoulder when right clicked with a stick."
                + "\nfly: should the pokemob be able to carry player (defaulted true for flying types)"
                + "\ndive: should the pokemob be able to dive with the player."
                + "\nstationary: should the pokemob sit still, instead of wandering while idle"
                + "\ndye: Format: <boolean>:<number>, does the pokemob support dying, number is the default state.");
        statsNodes.put("EXPERIENCEMODE", "The function used for mapping exp to level."
                + "\nOptions: erratic, fast, medium fast, medium slow, slow, fluctuating");
//        statsNodes.put("PARTICLEEFFECTS", "particles produced." + "\nFormat: <particle>:<rate>");
        statsNodes.put("GENDERRATIO", "ratio of genders.\n"
                + "Options: 255 -> No gender, 254 -> all female, 0 -> all male, otherwise fraction of 254 as male/female");
        statsNodes.put("INTERACTIONLOGIC",
                "item interaction logic." + "\nFormat: <playerItem>`<result1> <playerIdem>`<result2>"
                        + "\nResult format: <itemname>#<metadata>"
                        + "\nIf <playerItem> is shears, it will attemp to shear, otherwise it will consume the item.");
        statsNodes.put("SHADOWREPLACEMENTS", "list of mobs to replace with shadow versions of this pokemon");
        statsNodes.put("HATEDMATERIALRULES", "list of rules for what materials this pokemon hates");
        statsNodes.put("ACTIVETIMES",
                "list of times this pokemon is active." + "\nDefault: all day. valid options: day, night, dusk, dawn");

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
        setLayout(new GridLayout());
        addWindowListener(this);

        Panel left = new Panel(new GridLayout(3, 1));

        Panel view = new Panel(new FlowLayout());
        Panel edit = new Panel(new BorderLayout());

        Panel file = new Panel(new GridLayout(3, 1));
        file.add(new Label("File"));
        file.add(fileName = new TextField(defaultFile));
        file.add(save = new Button("Save"));
        save.addActionListener(this);
        fileName.addActionListener(this);
        view.add(file);

        Panel pokemonControlButtons = new Panel(new GridLayout(3, 1));

        pokemonControlButtons.add(next = new Button("next"));
        pokemonControlButtons.add(prev = new Button("prev"));
        pokemonControlButtons.add(add = new Button("add new"));
        next.addActionListener(this);
        prev.addActionListener(this);
        add.addActionListener(new AddHandler());

        view.add(pokemonControlButtons);

        Panel selection = new Panel(new GridLayout(3, 1));

        lblInput = new Label("Pokemon");
        selection.add(lblInput);
        name = new TextField("", 10);
        selection.add(name);
        number = new TextField(10);
        selection.add(number);
        view.add(selection);
        name.addActionListener(this);
        number.addActionListener(this);

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

        view.add(new Label("stats/moves toggle:"));
        view.add(toggle = new Button("Stats"));
        toggle.addActionListener(this);

        inputLabel = new TextArea(5, 25);
        inputLabel.setEditable(false);
        input = new TextArea(10, 50);

        Panel editInputs = new Panel(new GridLayout(1, 3));

        Panel optionsPanel = new Panel(new GridLayout(2, 1));
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
        Panel parseButtons = new Panel();
        edit.add(parseButtons, BorderLayout.SOUTH);
        parseButtons.add(parse = new Button("Parse"));
        parseButtons.add(clear = new Button("Clear"));
        ParseHandler parser = new ParseHandler();
        parse.addActionListener(parser);
        clear.addActionListener(parser);

        Panel mergePanel = new Panel(new FlowLayout());

        mergePanel.add(doc1 = new TextField(5));
        mergePanel.add(doc2 = new TextField(5));
        mergePanel.add(output = new TextField(5));

        mergePanel.add(merge = new Button("merge"));
        merge.addActionListener(new MergeHandler());

        doc1.setText("doc1");
        doc2.setText("doc2");
        output.setText("output");

        left.add(view);
        left.add(status = new TextArea(50, 50));
        status.setEditable(false);
        left.add(mergePanel);

        serebii = new SerebiiChecker(this);
        view.add(serebii.panel);

        add(left);
        add(edit);

        try
        {
            getEntry(null, 0, true);
        }
        catch (ParserConfigurationException | IOException | SAXException e)
        {
            String mess = e + "";
            for (Object o : e.getStackTrace())
            {
                mess += "\n" + o;
            }
            status.append(mess);
        }
        updateBoxes(name);
        statNodeOptions.setEnabled(!moves);
        moveNodeOptions.setEnabled(moves);
        choiceHandler.itemStateChanged(null);
        setTitle("Pokecube Database Info");
        setSize(1350, 600);
        setVisible(true);
    }

    /** The entry main() method */
    public static void main(String[] args)
    {
        // Invoke the constructor to setup the GUI, by allocating an anonymous
        // instance
        instance = new Main();
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
        status.setText("");

        if (evt.getSource() == save)
        {
            try
            {
                status.setText("Saving Changes, please wait...");
                cleanUpEmpty(doc);
                writeXML(file);
                doc = null;
                getEntry(0);
                status.setText("Done Saving");
            }
            catch (Exception e)
            {
                String mess = e + "";
                for (Object o : e.getStackTrace())
                {
                    mess += "\n" + o;
                }
                status.append(mess);
            }
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

            if (!file.exists())
            {
                file = new File("./" + fileName.getText() + ".xml");
            }

            name.setText("");
            number.setText("");
            try
            {
                getEntry(null, 0, true);
            }
            catch (ParserConfigurationException | IOException | SAXException e)
            {
                String mess = e + "";
                for (Object o : e.getStackTrace())
                {
                    mess += "\n" + o;
                }
                status.append(mess);
            }
            if (doc != null) updateBoxes(name);
            return;
        }

        if (evt.getSource() == next)
        {
            try
            {
                Element next = nextEntry(name.getText(), 1);
                if (next != null) name.setText(next.getAttribute("name"));
            }
            catch (ParserConfigurationException | IOException | SAXException e)
            {
                String mess = e + "";
                for (Object o : e.getStackTrace())
                {
                    mess += "\n" + o;
                }
                status.append(mess);
            }

            updateBoxes(name);
            return;
        }
        else if (evt.getSource() == prev)
        {
            try
            {
                Element next = nextEntry(name.getText(), -1);
                if (next != null) name.setText(next.getAttribute("name"));
            }
            catch (ParserConfigurationException | IOException | SAXException e)
            {
                String mess = e + "";
                for (Object o : e.getStackTrace())
                {
                    mess += "\n" + o;
                }
                status.append(mess);
            }
            updateBoxes(name);
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

    void updateBoxes(Object source)
    {

        statNodeOptions.setEnabled(!moves);
        moveNodeOptions.setEnabled(moves);

        inputLabel.setText(moves ? movesNodes.get(moveNodeOptions.getSelectedItem())
                : statsNodes.get(statNodeOptions.getSelectedItem()));

        try
        {
            if (source == name || source instanceof Button)
            {
                String text = name.getText();
                node = getEntry(text);
            }
            else if (source == number)
            {
                int num = Integer.parseInt(number.getText());
                node = getEntry(num);
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
                    if (subNode.hasChildNodes()) info.setText(subNode.getFirstChild().getNodeValue());
                    else info.setText("");
                }
                else if (attribChoice.getSelectedItem() != null)
                {
                    Attr attrib = subNode.getAttributeNode(attribChoice.getSelectedItem());
                    if (attrib != null)
                    {
                        label.setText(attrib.getName());
                        info.setText(attrib.getNodeValue());
                    }
                    else
                    {
                        info.setText("");
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
                    if (subNode.hasChildNodes()) info.setText(subNode.getFirstChild().getNodeValue());
                    else info.setText("");
                }
                else if (subNode != null)
                {
                    Attr attrib = subNode.getAttributeNode(attribChoice.getSelectedItem());
                    if (attrib != null)
                    {
                        label.setText(attrib.getName());
                        info.setText(attrib.getNodeValue());
                    }
                }
            }
        }
        catch (Exception e)
        {
            String mess = e + "";
            for (Object o : e.getStackTrace())
            {
                mess += "\n" + o;
            }
            status.append(mess);
        }
    }

    public void clearTag(String name, String key) throws ParserConfigurationException, SAXException, IOException
    {

        Element node = null;

        if (!hasEntry(name, -1))
        {
            System.err.println("No Entry for " + name);
            return;
        }
        else
        {
            node = Main.instance.getEntry(name);
        }

        NodeList list;

        String type = Main.instance.moves ? "MOVES" : "STATS";
        list = node.getElementsByTagName(type);
        Element selectedNode;
        if (list.getLength() == 0)
        {

        }
        else
        {
            selectedNode = (Element) list.item(0);
            list = selectedNode.getElementsByTagName(key);
            if (list.getLength() == 0)
            {

            }
            else
            {
                selectedNode.removeChild(list.item(0));
            }
        }

    }

    public void editEntry(String name, String key1, String key2, String value)
            throws ParserConfigurationException, IOException, SAXException
    {

        Element node = null;

        if (!hasEntry(name, -1))
        {
            System.err.println("No Entry for " + name);
            return;
        }

        node = Main.instance.getEntry(name);

        NodeList list;

        String type = Main.instance.moves ? "MOVES" : "STATS";
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
        list = selectedNode.getElementsByTagName(key1);
        if (list.getLength() == 0)
        {
            subNode = Main.instance.doc.createElement(key1);
            selectedNode.appendChild(subNode);
        }
        else
        {
            subNode = (Element) list.item(0);
        }
        if (value == null) System.out.println(name + " " + key1 + " " + key2);

        String attrib = key2;
        if (attrib == null)
        {
            if (subNode.getFirstChild() == null)
            {
                Text text = Main.instance.doc.createTextNode(value);
                subNode.appendChild(text);
            }
            else subNode.getFirstChild().setNodeValue(value);
        }
        else
        {
            subNode.removeAttribute(attrib);
            if (value != null && !value.isEmpty())
            {
                Attr at = Main.instance.doc.createAttribute(attrib);
                at.setValue(value);
                subNode.setAttributeNode(at);
            }
        }

    }

    Element getEntry(String name) throws ParserConfigurationException, IOException, SAXException
    {
        return getEntry(name, 0);
    }

    Element getEntry(int number) throws ParserConfigurationException, IOException, SAXException
    {
        return getEntry(null, number);
    }

    Element getEntry(String name, int number) throws ParserConfigurationException, IOException, SAXException
    {
        return getEntry(name, number, false);
    }

    public boolean hasEntry(String name, int number, boolean checkFormes)
            throws ParserConfigurationException, IOException, SAXException
    {
        if (doc == null)
        {
            getEntry(number);
        }
        NodeList entries = doc.getElementsByTagName("Pokemon");
        for (int i = 0; i < entries.getLength(); i++)
        {
            Element pokemonNode = (Element) entries.item(i);
            int num = Integer.parseInt(pokemonNode.getAttribute("number"));
            String name2 = pokemonNode.getAttribute("name");
            if ((num == number || number == -1) && name2.equalsIgnoreCase(name)) return true;
        }
        if (checkFormes) for (int i = 0; i < entries.getLength(); i++)
        {
            Element pokemonNode = (Element) entries.item(i);
            int num = Integer.parseInt(pokemonNode.getAttribute("number"));
            String name2 = pokemonNode.getAttribute("name");
            if ((num == number || number == -1) && name2.contains(name)) return true;
        }
        return false;
    }

    public boolean hasEntry(String name, int number) throws ParserConfigurationException, SAXException, IOException
    {
        return hasEntry(name, number, false);
    }

    Element nextEntry(String name, int dir) throws ParserConfigurationException, SAXException, IOException
    {
        if (doc == null)
        {
            getEntry(name);
        }

        NodeList entries = doc.getElementsByTagName("Pokemon");
        Element next = null;
        for (int i = 0; i < entries.getLength(); i++)
        {
            Element pokemonNode = (Element) entries.item(i);
            String name2 = pokemonNode.getAttribute("name");
            if (name2.equalsIgnoreCase(name))
            {
                if (dir == 1)
                    return (i < entries.getLength() - 1) ? (Element) entries.item(i + 1) : (Element) entries.item(0);
                else return (i > 0) ? (Element) entries.item(i - 1) : (Element) entries.item(entries.getLength() - 1);
            }
        }
        return next;
    }

    Element getEntry(String name, int number, boolean newDoc)
            throws ParserConfigurationException, IOException, SAXException
    {
        return getEntry(name, number, newDoc, false);
    }

    public Element getEntry(String name, int number, boolean newDoc, boolean checkFormes)
            throws ParserConfigurationException, IOException, SAXException
    {
        if (doc == null)
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
        if (checkFormes) for (int i = 0; i < entries.getLength(); i++)
        {
            Element pokemonNode = (Element) entries.item(i);
            if (i == 0) first = pokemonNode;
            int num = Integer.parseInt(pokemonNode.getAttribute("number"));
            String name2 = pokemonNode.getAttribute("name");
            if (num == number || name2.toLowerCase().contains(name.toLowerCase())) return pokemonNode;
        }
        return first;
    }

    static void cleanUpEmpty(Document doc)
    {
        NodeList entries = doc.getChildNodes();
        for (int i = 0; i < entries.getLength(); i++)
        {
            if (entries.item(i).hasChildNodes())
            {
                cleanUpEmpty((Element) entries.item(i));
            }
        }
    }

    static void cleanUpEmpty(Element element)
    {
        boolean empty = !(element.hasChildNodes() || element.hasAttributes());
        if (empty) Thread.dumpStack();
        NodeList entries = element.getChildNodes();
        HashSet<Object> toRemove = new HashSet<>();
        for (int i = 0; i < entries.getLength(); i++)
        {
            boolean attribs = entries.item(i).hasAttributes();
            if (entries.item(i).hasChildNodes())
            {
                cleanUpEmpty((Element) entries.item(i));
            }
            else if (!attribs && (entries.item(i).getNodeValue() == null || entries.item(i).getNodeValue().isEmpty()))
            {
                toRemove.add(entries.item(i));
            }
            else if (attribs && entries.item(i).getNodeName().equals("EVYIELD"))
            {
                HashSet<Attr> zero = new HashSet<>();
                for (int j = 0; j < entries.item(i).getAttributes().getLength(); j++)
                {
                    Attr atrib = (Attr) entries.item(i).getAttributes().item(j);
                    if (atrib.getValue().trim().equals("0")) zero.add(atrib);
                }
                for (Attr at : zero)
                    entries.item(i).getAttributes().removeNamedItem(at.getName());
            }
            else if (attribs)
            {
                HashSet<Attr> zero = new HashSet<>();
                for (int j = 0; j < entries.item(i).getAttributes().getLength(); j++)
                {
                    Attr atrib = (Attr) entries.item(i).getAttributes().item(j);
                    if (atrib.getValue().trim().equals("")) zero.add(atrib);
                }
                for (Attr at : zero)
                    entries.item(i).getAttributes().removeNamedItem(at.getName());
            }
        }
        for (Object oldChild : toRemove)
            element.removeChild((Node) oldChild);
    }

    void writeXML(File file) throws IOException
    {
        writeXML(doc, file);
        doc = null;
    }

    void writeXML(Document doc, File file) throws IOException
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
                    String mess = e + "";
                    for (Object o : e.getStackTrace())
                    {
                        mess += "\n" + o;
                    }
                    status.append(mess);
                }
            }
            catch (Exception e)
            {
                String mess = e + "";
                for (Object o : e.getStackTrace())
                {
                    mess += "\n" + o;
                }
                status.append(mess);
            }

        }
        catch (TransformerException e)
        {
            String mess = e + "";
            for (Object o : e.getStackTrace())
            {
                mess += "\n" + o;
            }
            status.append(mess);
        }
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