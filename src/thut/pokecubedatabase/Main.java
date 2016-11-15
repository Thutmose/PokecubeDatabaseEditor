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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import thut.pokecubedatabase.XMLEntries.Moves;
import thut.pokecubedatabase.XMLEntries.Moves.LvlUp;
import thut.pokecubedatabase.XMLEntries.StatsNode;
import thut.pokecubedatabase.XMLEntries.StatsNode.Stats;
import thut.pokecubedatabase.XMLEntries.XMLPokedexEntry;
import thut.pokecubedatabase.serebii.SerebiiChecker;

// An AWT GUI program inherits from the top-level container java.awt.Frame
public class Main extends Frame implements ActionListener, WindowListener
{
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

    final static String                     defaultFile      = "pokemobs.xml";
    public static Main                      instance;
    public static File                      file             = new File("./" + defaultFile);
    public static XMLPokedexEntry           currentEntry     = null;
    /**
     * 
     */
    static final long                       serialVersionUID = 1L;
    public static HashMap<String, String>   statsNodes       = new HashMap<>();
    public static HashMap<String, String>   statsNodeNames   = new HashMap<>();
    public static HashMap<String, String>   movesNodes       = new HashMap<>();
    public static HashMap<String, String>   movesNodesNames  = new HashMap<>();
    public static HashMap<String, String[]> validAttribs     = new HashMap<>();
    public static final String[]            statsNames       = { "hp", "atk", "def", "spatk", "spdef", "spd" };

    static
    {
        statsNodeNames.put("EVOLUTIONMODE",
                "Method of evolution, some pokemon have multiple evos, some have none. See Eevee"
                        + ".\nFormat: <evo1mode>:<requirement> <evo2mode>:<requirement>"
                        + ".\nValid modes: Level, Stone:<stonetype, example water, fire, etc>, Happiness, Move:<movename>, Trade, etc"
                        + ".\nModes can also have modifiers, see Gligar(207) for example of item and time requirements");
        statsNodeNames.put("CAPTURERATE", "The Capture Rate");
        statsNodeNames.put("EVOLUTIONANIMATION", "related to the colour of the evolution animation");
        // statsNodeNames.put("RIDDENOFFSET", "Offset for position of rider");
        statsNodeNames.put("PREY", "Species this pokemon eats." + "\nFormat: <Species1> <Species2>");
        statsNodeNames.put("SPECIES", "Species this pokemon is." + "\nFormat: <Species1> <Species2>");
        statsNodeNames.put("MOVEMENTTYPE", "options:normal, floating, flying, water");
        statsNodeNames.put("SPECIALEGGSPECIESRULES",
                "special rules for what eggs are produced from varied parents, see NidoranF for example."
                        + "\nFormat, values are pokedex numbers: <father>:<child>`<child>;<father>:<child>`<child>");
        statsNodeNames.put("MASSKG", "The mass in kg");
        statsNodeNames.put("EVOLVESTO", "the pokemon's number which this evolves to.\nFormat: <evo1nb> <evo2nb>");
        statsNodeNames.put("EXPYIELD", "Base EXP from defeating");
        statsNodeNames.put("HELDITEM",
                "list of items held randomly by wild versions."
                        + "\nFormat: <number>:<itemname>:<metadata>:<chance> <number>:<itemname>:<metadata>:<chance>"
                        + "\nmetadata is optional, but if chance is used, it is needed.");
        statsNodeNames.put("FOODMATERIAL", "materials this pokemon can eat (example, water, rock, light)");
        statsNodeNames.put("BASEFRIENDSHIP", "Base friendship for the pokemon");
        statsNodeNames.put("ABILITY",
                "the abilities available to the pokemon.\n"
                        + "normal: the basic abilities, Format: <ability1>, <ability2>"
                        + "\nhidden: the hidden ability, Format: <ability>");
        statsNodeNames.put("TYPE", "the pokemon's types." + "\ntype1: the first type listed, Format: <type1>"
                + "\ntype2: the second type listed, optional, Format: <type2>");
        statsNodeNames.put("SIZES",
                "the dimensions of the pokemon." + "\nheight: how tall is the hitbox, Format: <height in meters>"
                        + "\nwidth: how wide is the hitbox, Format: <width in meters>"
                        + "\nlength: how long is the hitbox, Format: <length in meters>");
        statsNodeNames.put("EVYIELD", "evs gained from defeating this pokemon, blank values mean no EV of that stat.");
        statsNodeNames.put("BASESTATS", "this pokemon's base stats");
        statsNodeNames.put("MEGARULES", "");
        statsNodeNames.put("FORMEITEMS", "");
        statsNodeNames.put("LOGIC", "logic states.\n"
                + "shoulder: should the pokemob jump on shoulder when right clicked with a stick."
                + "\nfly: should the pokemob be able to carry player (defaulted true for flying types)"
                + "\ndive: should the pokemob be able to dive with the player."
                + "\nstationary: should the pokemob sit still, instead of wandering while idle"
                + "\ndye: Format: <boolean>:<number>, does the pokemob support dying, number is the default state.");
        statsNodeNames.put("EXPERIENCEMODE", "The function used for mapping exp to level."
                + "\nOptions: erratic, fast, medium fast, medium slow, slow, fluctuating");
        statsNodeNames.put("GENDERRATIO", "ratio of genders.\n"
                + "Options: 255 -> No gender, 254 -> all female, 0 -> all male, otherwise fraction of 254 as male/female");
        statsNodeNames.put("INTERACTIONLOGIC",
                "item interaction logic." + "\nFormat: <playerItem>`<result1> <playerIdem>`<result2>"
                        + "\nResult format: <itemname>#<metadata>"
                        + "\nIf <playerItem> is shears, it will attemp to shear, otherwise it will consume the item.");
        statsNodeNames.put("SHADOWREPLACEMENTS", "list of mobs to replace with shadow versions of this pokemon");
        statsNodeNames.put("HATEDMATERIALRULES", "list of rules for what materials this pokemon hates");
        statsNodeNames.put("ACTIVETIMES",
                "list of times this pokemon is active." + "\nDefault: all day. valid options: day, night, dusk, dawn");
        movesNodesNames.put("LVLUP", "moves learned on lvl up");
        movesNodesNames.put("MISC", "any other obtainable move");

        validAttribs.put("stats", statsNames);
        validAttribs.put("evs", statsNames);
        validAttribs.put("types", new String[] { "type1", "type2" });
        validAttribs.put("abilities", new String[] { "normal", "hidden" });
        validAttribs.put("sizes", new String[] { "length", "width", "height" });
        validAttribs.put("logics", new String[] { "shoulder", "fly", "dive", "surf", "stationary", "dye" });
    }

    /** The entry main() method */
    public static void main(String[] args)
    {
        // Invoke the constructor to setup the GUI, by allocating an anonymous
        // instance
        instance = new Main();
    }

    Label            lblInput;
    Label            nodeLabel;
    // View Buttons
    TextField        fileName;

    TextField        name;
    public TextField number;

    // Edit buttons

    Button           next;

    Button           prev;
    Button           add;
    TextField        label;

    TextField        info;
    public TextArea  status;
    Button           toggle;

    Button           save;

    TextArea         inputLabel;
    TextArea         input;
    Button           parse;

    Button           clear;

    Choice           statNodeOptions;

    Choice           moveNodeOptions;
    Choice           attribChoice;

    ChoiceHandler    choiceHandler = new ChoiceHandler(this);
    TextField        doc1;
    TextField        doc2;

    TextField        output;

    Button           merge;

    public boolean   moves         = false;

    SerebiiChecker   serebii;

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

        ArrayList<String> options = new ArrayList<>();
        Field[] fields = StatsNode.class.getDeclaredFields();
        for (Field f : fields)
        {
            options.add(f.getName());
            XmlElement element = f.getAnnotation(XmlElement.class);
            if (element != null)
            {
                String var = statsNodeNames.get(element.name());
                if (var == null) var = "n/a";
                statsNodes.put(f.getName(), var);
            }
        }
        Collections.sort(options);
        for (String s : options)
        {
            statNodeOptions.add(s);
        }
        moveNodeOptions = new Choice();
        moveNodeOptions.addItemListener(choiceHandler);
        options = new ArrayList<>();
        fields = Moves.class.getDeclaredFields();
        for (Field f : fields)
        {
            options.add(f.getName());
            XmlElement element = f.getAnnotation(XmlElement.class);
            if (element != null)
            {
                String var = movesNodesNames.get(element.name());
                if (var == null) var = "n/a";
                movesNodes.put(f.getName(), var);
            }
        }
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
        getEntry(null, 0, true);
        updateBoxes(name);
        statNodeOptions.setEnabled(!moves);
        moveNodeOptions.setEnabled(moves);
        choiceHandler.itemStateChanged(null);
        setTitle("Pokecube Database Info");
        setSize(1350, 600);
        setVisible(true);
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
                writeXML(file);
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
            file = null;
            file = new File("./" + fileName.getText());

            if (!file.exists())
            {
                file = new File("./" + fileName.getText() + ".xml");
            }
            name.setText("");
            number.setText("");
            getEntry(null, 0, true);
            updateBoxes(name);
            return;
        }

        if (evt.getSource() == next)
        {
            XMLPokedexEntry currentEntry = nextEntry(name.getText(), 1);
            if (next != null) name.setText(currentEntry.name);
            updateBoxes(name);
            return;
        }
        else if (evt.getSource() == prev)
        {
            XMLPokedexEntry currentEntry = nextEntry(name.getText(), -1);
            if (next != null) name.setText(currentEntry.name);
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

    XMLPokedexEntry getEntry(int number)
    {
        return getEntry(null, number);
    }

    XMLPokedexEntry getEntry(String name)
    {
        return getEntry(name, 0);
    }

    XMLPokedexEntry getEntry(String name, int number)
    {
        return getEntry(name, number, false);
    }

    XMLPokedexEntry getEntry(String name, int number, boolean newDoc)
    {
        return getEntry(name, number, newDoc, false);
    }

    XMLPokedexEntry getEntry(String name, int number, boolean newDoc, boolean checkFormes)
    {
        return XMLEntries.getDatabase(file).getEntry(name, number, checkFormes);
    }

    String getNameFromNumber(int number) throws NullPointerException
    {
        XMLPokedexEntry pokemonNode = getEntry(number);
        return pokemonNode.name;
    }

    public boolean hasEntry(String name, int number) throws ParserConfigurationException, SAXException, IOException
    {
        return hasEntry(name, number, false);
    }

    public boolean hasEntry(String name, int number, boolean checkFormes)
    {
        return XMLEntries.getDatabase(file).getEntry(name, number, checkFormes) != null;
    }

    XMLPokedexEntry nextEntry(String name, int dir)
    {
        XMLPokedexEntry entry = getEntry(name, -1, false, true);
        return XMLEntries.getDatabase(file).next(entry, dir);
    }

    void renameTextures()
    {
        File dir = new File("./toRename");
        for (File file : dir.listFiles())
        {
            String name = file.getName();
            try
            {
                name = getNameFromNumber(Integer.parseInt(name.replace(".png", ""))) + ".png";
                File dest = new File(dir, name);
                file.renameTo(dest);
            }
            catch (Exception e)
            {

            }
        }
    }

    void updateBoxes(Object source)
    {

        statNodeOptions.setEnabled(!moves);
        moveNodeOptions.setEnabled(moves);

        inputLabel.setText(moves ? movesNodes.get(moveNodeOptions.getSelectedItem())
                : statsNodes.get(statNodeOptions.getSelectedItem()));

        try
        {
            XMLPokedexEntry entry = null;
            System.out.println((name == source) + " " + name.getText());
            if (source == name || source instanceof Button)
            {
                String text = name.getText();
                entry = getEntry(text, -1, false, true);
            }
            else if (source == number)
            {
                int num = Integer.parseInt(number.getText());
                entry = getEntry(num);
            }

            if (entry == null) { return; }

            number.setText(entry.number + "");
            name.setText(entry.name);
            System.out.println((name == source) + " " + name.getText());

            if (!moves)
            {
                String selectedItem = statNodeOptions.getSelectedItem();
                String selectedAtrib = attribChoice.getSelectedItem();
                Field selected = StatsNode.class.getDeclaredField(selectedItem);
                Object value = selected.get(entry.stats);

                boolean simple = ((selected.getType() == Double.TYPE) || (selected.getType() == Double.class))
                        || ((selected.getType() == Integer.TYPE) || (selected.getType() == Integer.class))
                        || ((selected.getType() == Float.TYPE) || (selected.getType() == Float.class))
                        || ((selected.getType() == Boolean.TYPE) || (selected.getType() == Boolean.class))
                        || ((value instanceof String) || (selected.getType() == String.class));

                if (selectedAtrib != null)
                {
                    if (value instanceof Stats)
                    {
                        Stats stat = (Stats) value;
                        String val = stat.values.get(new QName(selectedAtrib));
                        nodeLabel.setText(selectedAtrib);
                        if (val != null) info.setText(val);
                    }
                }
                else if (simple)
                {
                    nodeLabel.setText(selectedItem);
                    if (value != null) info.setText(value.toString());
                    else info.setText("");
                }
                else
                {
                    nodeLabel.setText("");
                    info.setText("");
                }
            }
            else
            {
                String selectedItem = moveNodeOptions.getSelectedItem();
                String selectedAtrib = attribChoice.getSelectedItem();
                if (selectedAtrib != null)
                {
                    Field selected = Moves.class.getDeclaredField(selectedItem);
                    Object value = selected.get(entry.moves);
                    if (value instanceof LvlUp)
                    {
                        LvlUp stat = (LvlUp) value;
                        String val = stat.values.get(new QName(selectedAtrib));
                        nodeLabel.setText(selectedAtrib);
                        if (val != null) info.setText(val);
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

    void writeXML(File file) throws JAXBException
    {
        XMLEntries.write(file);
    }
}