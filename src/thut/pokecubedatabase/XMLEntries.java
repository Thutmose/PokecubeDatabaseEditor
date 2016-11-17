package thut.pokecubedatabase;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Marshaller.Listener;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import thut.pokecubedatabase.XMLEntries.StatsNode.Stats;

public class XMLEntries
{

    @XmlRootElement(name = "BODY")
    public static class BodyNode
    {
        @XmlElement(name = "PART")
        public List<BodyPart> parts = new ArrayList<>();
    }

    @XmlRootElement(name = "PART")
    public static class BodyPart
    {
        @XmlAttribute(name = "name")
        public String name;
        @XmlAttribute(name = "offset")
        public String offset;
        @XmlAttribute(name = "dimensions")
        public String dimensions;
    }

    @XmlRootElement(name = "Drop")
    public static class Drop
    {
        @XmlAnyAttribute
        public Map<QName, String> values = new HashMap<>();
        @XmlElement(name = "tag")
        public String             tag;
    }

    @XmlRootElement(name = "Interact")
    public static class Interact
    {
        @XmlElement(name = "Key")
        public Key    key;
        @XmlElement(name = "Action")
        public Action action;
    }

    @XmlRootElement(name = "Key")
    public static class Key
    {
        @XmlAnyAttribute
        Map<QName, String> values = new HashMap<>();
        @XmlElement(name = "tag")
        public String      tag;
    }

    @XmlRootElement(name = "Action")
    public static class Action
    {
        @XmlAnyAttribute
        Map<QName, String> values = new HashMap<>();
        @XmlElement(name = "tag")
        public String      tag;
        @XmlElement(name = "Drop")
        public List<Drop>  drops  = new ArrayList<>();
    }

    @XmlRootElement(name = "Spawn")
    public static class SpawnRule
    {
        @XmlAnyAttribute
        public Map<QName, String> values = new HashMap<>();
    }

    @XmlRootElement(name = "MOVES")
    public static class Moves
    {
        @XmlRootElement(name = "LVLUP")
        public static class LvlUp
        {
            @XmlAnyAttribute
            public Map<QName, String> values = new HashMap<>();
        }

        @XmlRootElement(name = "MISC")
        public static class Misc
        {
            @XmlAttribute(name = "moves")
            public String moves;

            @Override
            public String toString()
            {
                return moves;
            }
        }

        @XmlElement(name = "LVLUP")
        public LvlUp lvlupMoves;

        @XmlElement(name = "MISC")
        public Misc  misc;
    }

    @XmlRootElement(name = "STATS")
    public static class StatsNode
    {
        public static class Stats
        {
            @XmlAnyAttribute
            public Map<QName, String> values = new HashMap<>();
        }

        @XmlAttribute
        public String          spawns;
        // Evolution stuff
        @XmlElement(name = "EVOLUTIONMODE")
        public String          evoModes;
        @XmlElement(name = "EVOLUTIONANIMATION")
        public String          evolAnims;

        @XmlElement(name = "EVOLVESTO")
        public String          evoTo;
        // Species and food
        @XmlElement(name = "SPECIES")
        public String          species;
        @XmlElement(name = "PREY")
        public String          prey;
        @XmlElement(name = "FOODMATERIAL")
        public String          foodMat;

        @XmlElement(name = "SPECIALEGGSPECIESRULES")
        public String          specialEggRules;
        // Drops and items
        @XmlElement(name = "Drop")
        public List<Drop>      drops        = new ArrayList<>();
        @XmlElement(name = "Held")
        public List<Drop>      held         = new ArrayList<>();
        // Spawn Rules
        @XmlElement(name = "Spawn")
        public List<SpawnRule> spawnRules   = new ArrayList<>();
        // STATS
        @XmlElement(name = "BASESTATS")
        public Stats           stats;
        @XmlElement(name = "EVYIELD")
        public Stats           evs;
        @XmlElement(name = "SIZES")
        public Stats           sizes;
        @XmlElement(name = "TYPE")
        public Stats           types;
        @XmlElement(name = "ABILITY")
        public Stats           abilities;
        @XmlElement(name = "MASSKG")
        public String          mass;
        @XmlElement(name = "CAPTURERATE")
        public String          captureRate;
        @XmlElement(name = "EXPYIELD")
        public String          baseExp;
        @XmlElement(name = "BASEFRIENDSHIP")
        public String          baseFriendship;
        @XmlElement(name = "EXPERIENCEMODE")
        public String          expMode;

        @XmlElement(name = "GENDERRATIO")
        public String          genderRatio;
        // MISC
        @XmlElement(name = "LOGIC")
        public Stats           logics;
        @XmlElement(name = "FORMEITEMS")
        public Stats           formeItems;
        @XmlElement(name = "MEGARULES")
        public Stats           megaRules;
        @XmlElement(name = "MOVEMENTTYPE")
        public String          movementType;
        @XmlElement(name = "Interact")
        public List<Interact>  interactions = new ArrayList<>();
        @XmlElement(name = "SHADOWREPLACEMENTS")
        public String          shadowReplacements;
        @XmlElement(name = "HATEDMATERIALRULES")
        public String          hatedMaterials;

        @XmlElement(name = "ACTIVETIMES")
        public String          activeTimes;
    }

    @XmlRootElement(name = "Document")
    public static class XMLDatabase
    {
        public static Map<XMLPokedexEntry, Integer> entriesMap = new HashMap<>();

        @XmlElement(name = "Pokemon")
        public List<XMLPokedexEntry>                pokemon    = new ArrayList<>();

        public void init()
        {
            for (int i = 0; i < pokemon.size(); i++)
            {
                entriesMap.put(pokemon.get(i), i);
            }
        }

        public XMLPokedexEntry next(XMLPokedexEntry current, int dir)
        {
            if (entriesMap.isEmpty()) init();
            int index = entriesMap.get(current) + dir;
            XMLPokedexEntry ret;
            if (index > 0 && index < pokemon.size()) ret = pokemon.get(index);
            else if (index > pokemon.size()) ret = pokemon.get(0);
            else ret = pokemon.get(pokemon.size() - 1);
            System.out
                    .println(current.name + " " + ret.name + " " + entriesMap.get(current) + " " + entriesMap.get(ret));
            return ret;
        }

        public XMLPokedexEntry getEntry(String name, int number, boolean checkFormes, int default_)
        {
            boolean checkNum = number > 0;
            boolean checkName = name != null;
            for (int i = 0; i < pokemon.size(); i++)
            {
                XMLPokedexEntry test = pokemon.get(i);
                if (checkName && test.name.equals(name) && (checkFormes || Boolean.parseBoolean(test.base)))
                    return test;
                if (checkNum && Integer.parseInt(test.number) == number
                        && (checkFormes || Boolean.parseBoolean(test.base)))
                    return test;
            }
            return default_ >= 0 ? pokemon.get(0) : null;
        }

        public XMLPokedexEntry getEntry(String name, int number, boolean checkFormes)
        {
            return getEntry(name, number, checkFormes, 0);
        }
    }

    @XmlRootElement(name = "Pokemon")
    public static class XMLPokedexEntry
    {
        @XmlAttribute
        public String    name;
        @XmlAttribute
        public String    number;
        @XmlAttribute
        public String    special;
        @XmlAttribute
        public String    base;
        @XmlAttribute
        public String    breed;
        @XmlAttribute
        public String    starter;
        @XmlAttribute
        public String    legend;
        @XmlAttribute
        public String    hasShiny;
        @XmlAttribute
        public String    gender     = "";
        @XmlAttribute
        public String    genderBase = "";
        @XmlElement(name = "STATS")
        public StatsNode stats;
        @XmlElement(name = "MOVES")
        public Moves     moves;
        @XmlElement(name = "BODY")
        public BodyNode  body;

        @Override
        public String toString()
        {
            return name + " " + number + " " + stats + " " + moves;
        }

        public void mergeMissingFrom(XMLPokedexEntry other)
        {
            if (moves == null && other.moves != null)
            {
                moves = other.moves;
            }
            else if (other.moves != null)
            {
                if (moves.lvlupMoves == null)
                {
                    moves.lvlupMoves = other.moves.lvlupMoves;
                }
                if (moves.misc == null)
                {
                    moves.misc = other.moves.misc;
                }
            }
            if (body == null && other.body != null)
            {
                body = other.body;
            }
            if (stats == null && other.stats != null)
            {
                stats = other.stats;
            }
            else if (other.stats != null)
            {
                // Copy everything which is missing
                for (Field f : StatsNode.class.getDeclaredFields())
                {
                    try
                    {
                        Object ours = f.get(stats);
                        Object theirs = f.get(other.stats);
                        boolean isNumber = !(ours instanceof String || ours instanceof Stats);
                        if (isNumber)
                        {
                            if (ours instanceof Float)
                            {
                                isNumber = (float) ours == -1;
                            }
                            else if (ours instanceof Integer)
                            {
                                isNumber = (int) ours == -1;
                            }
                        }
                        if (ours == null)
                        {
                            f.set(stats, theirs);
                        }
                        else if (isNumber)
                        {
                            f.set(stats, theirs);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof XMLPokedexEntry)
            {
                XMLPokedexEntry other = (XMLPokedexEntry) obj;
                return other.name.equals(name);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return super.hashCode();
        }
    }

    private static XMLDatabase  database;

    static Set<XMLPokedexEntry> entries = new HashSet<>();

    public static XMLDatabase loadDatabase(File file) throws Exception
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FileReader reader = new FileReader(file);
        XMLDatabase database = (XMLDatabase) unmarshaller.unmarshal(reader);
        reader.close();
        return database;
    }

    public static void reload(File file)
    {
        try
        {
            database = loadDatabase(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static XMLDatabase getDatabase(File file)
    {
        if (database == null)
        {
            try
            {
                database = loadDatabase(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return database;
    }

    public static void write(File file) throws JAXBException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        for (XMLPokedexEntry entry : database.pokemon)
        {
            if (entry.moves != null && entry.moves.misc != null && entry.moves.misc.moves != null)
            {
                String[] moves = entry.moves.misc.moves.split(", ");
                Set<String> moveset = new HashSet<>();
                for (String s : moves)
                    moveset.add(ParseHandler.convertName(s));
                List<String> movesList = new ArrayList<>(moveset);
                Collections.sort(movesList);
                entry.moves.misc.moves = movesList.get(0);
                for (int i = 1; i < movesList.size(); i++)
                    entry.moves.misc.moves = entry.moves.misc.moves + ", " + movesList.get(i);
            }
            if (entry.moves != null && entry.moves.lvlupMoves != null)
            {
                List<QName> keys = new ArrayList<>(entry.moves.lvlupMoves.values.keySet());
                Map<QName, String> updated = new HashMap<QName, String>();
                for (QName key : keys)
                {
                    updated.put(key, ParseHandler.convertName(entry.moves.lvlupMoves.values.get(key)));
                }
                for (QName key : keys)
                {
                    entry.moves.lvlupMoves.values.put(key, updated.get(key));
                }
            }
            if (entry.stats != null && entry.stats.evolAnims != null)
                entry.stats.evolAnims = entry.stats.evolAnims.replace("tile crack_", "");
        }
        jaxbMarshaller.setListener(new Listener()
        {
            @Override
            public void beforeMarshal(Object source)
            {
                if (source instanceof XMLPokedexEntry)
                {
                    XMLPokedexEntry entry = (XMLPokedexEntry) source;
                    if (entry.gender != null && entry.gender.isEmpty()) entry.gender = null;
                    if (entry.genderBase != null && entry.genderBase.isEmpty()) entry.genderBase = null;
                    if (entry.breed != null && entry.breed.equals("true")) entry.breed = null;
                    if (entry.hasShiny != null && entry.hasShiny.equals("true")) entry.hasShiny = null;
                    if (entry.legend != null && !entry.legend.equals("true")) entry.legend = null;
                    if (entry.base != null && !entry.base.equals("true")) entry.base = null;
                    if (entry.starter != null && !entry.starter.equals("true")) entry.starter = null;
                    if (entry.starter != null && !entry.starter.equals("true")) entry.starter = null;
                }
            }
        });
        jaxbMarshaller.marshal(database, file);
    }
}
