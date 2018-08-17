package thut.pokecubedatabase.pokedex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import thut.pokecubedatabase.pokedex.XMLEntries.StatsNode.Stats;

public class XMLEntries
{

    public static final Gson                        gson;

    public static final Comparator<XMLPokedexEntry> ENTRYSORTER = new Comparator<XMLPokedexEntry>()
                                                                {
                                                                    @Override
                                                                    public int compare(XMLPokedexEntry o1,
                                                                            XMLPokedexEntry o2)
                                                                    {
                                                                        try
                                                                        {
                                                                            int num1 = o1.number;
                                                                            int num2 = o2.number;
                                                                            if (num1 != num2) return num1 - num2;
                                                                            int diff = 0;
                                                                            boolean base1 = o1.base == null ? false
                                                                                    : o1.base;
                                                                            boolean base2 = o2.base == null ? false
                                                                                    : o2.base;
                                                                            if (base1 && !base2) diff = -1;
                                                                            else if (base2 && !base1) diff = 1;
                                                                            if (diff != 0) return diff;
                                                                            return o1.name.compareTo(o2.name);
                                                                        }
                                                                        catch (Exception e)
                                                                        {
                                                                            e = new Exception(
                                                                                    "Error with " + o1 + " " + o2, e);
                                                                            e.printStackTrace();
                                                                            return 0;
                                                                        }
                                                                    }
                                                                };

    public static XMLPokedexEntry                   missingno   = new XMLPokedexEntry();

    static
    {
        gson = new GsonBuilder().registerTypeAdapter(QName.class, new TypeAdapter<QName>()
        {
            @Override
            public void write(JsonWriter out, QName value) throws IOException
            {
                out.value(value.toString());
            }

            @Override
            public QName read(JsonReader in) throws IOException
            {
                return new QName(in.nextString());
            }
        }).setPrettyPrinting().create();
        missingno.stats = new StatsNode();
    }

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
        @XmlAttribute
        public Boolean male       = true;
        @XmlAttribute
        public Boolean female     = true;
        @XmlAttribute
        public Integer cooldown   = 50;
        @XmlAttribute
        public Integer variance   = 100;
        @XmlAttribute
        public Integer baseHunger = 100;
        @XmlElement(name = "Key")
        public Key     key;
        @XmlElement(name = "Action")
        public Action  action;
    }

    @XmlRootElement(name = "Key")
    public static class Key
    {
        @XmlAnyAttribute
        public Map<QName, String> values = new HashMap<>();
        @XmlElement(name = "tag")
        public String             tag;
    }

    @XmlRootElement(name = "Action")
    public static class Action
    {
        @XmlAnyAttribute
        public Map<QName, String> values = new HashMap<>();
        @XmlElement(name = "tag")
        public String             tag;
        @XmlElement(name = "Drop")
        public List<Drop>         drops  = new ArrayList<>();
    }

    @XmlRootElement(name = "Spawn")
    public static class SpawnRule
    {
        @XmlAnyAttribute
        public Map<QName, String> values = new HashMap<>();

        @Override
        public String toString()
        {
            return values + "";
        }
    }

    public static class Evolution
    {
        @XmlAttribute(name = "clear")
        public Boolean   clear;
        @XmlAttribute(name = "Name")
        public String    name;
        @XmlAttribute(name = "Level")
        public Integer   level;
        @XmlAttribute(name = "Priority")
        public Integer   priority;
        @XmlElement(name = "Location")
        public SpawnRule location;
        @XmlAttribute(name = "Animation")
        public String    animation;
        @XmlElement(name = "Key")
        public Key       item;
        @XmlElement(name = "PresetItem")
        public String    item_preset;
        @XmlAttribute(name = "Time")
        public String    time;
        @XmlAttribute(name = "Trade")
        public Boolean   trade;
        @XmlAttribute(name = "Rain")
        public Boolean   rain;
        @XmlAttribute(name = "Happy")
        public Boolean   happy;
        @XmlAttribute(name = "Sexe")
        public String    sexe;
        @XmlAttribute(name = "Move")
        public String    move;
        @XmlAttribute(name = "Chance")
        public Float     chance;
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
        public LvlUp  lvlupMoves;

        @XmlElement(name = "MISC")
        public Misc   misc;

        @XmlElement(name = "EVOMOVES")
        public String evolutionMoves;
    }

    @XmlRootElement(name = "STATS")
    public static class StatsNode
    {
        public static class Stats
        {
            @XmlAnyAttribute
            public Map<QName, String> values = new HashMap<>();
        }

        // Evolution stuff
        @XmlElement(name = "Evolution")
        public List<Evolution>   evolutions     = new ArrayList<>();

        // Species and food
        @XmlElement(name = "SPECIES")
        public String            species;
        @XmlElement(name = "PREY")
        public String            prey;
        @XmlElement(name = "FOODMATERIAL")
        public String            foodMat;

        @XmlElement(name = "SPECIALEGGSPECIESRULES")
        public String            specialEggRules;
        // Drops and items
        @XmlElement(name = "Drop")
        public List<Drop>        drops          = new ArrayList<>();
        @XmlElement(name = "Held")
        public List<Drop>        held           = new ArrayList<>();
        @XmlElement(name = "lootTable")
        public String            lootTable;
        @XmlElement(name = "heldTable")
        public String            heldTable;
        // Spawn Rules
        @XmlAttribute
        public Boolean           overwrite      = false;
        @XmlElement(name = "Spawn")
        public List<SpawnRule>   spawnRules     = new ArrayList<>();
        // STATS
        @XmlElement(name = "BASESTATS")
        public Stats             stats;
        @XmlElement(name = "EVYIELD")
        public Stats             evs;
        @XmlElement(name = "SIZES")
        public Stats             sizes;
        @XmlElement(name = "TYPE")
        public Stats             types;
        @XmlElement(name = "ABILITY")
        public Stats             abilities;
        @XmlElement(name = "MASSKG")
        public Float             mass           = -1f;
        @XmlElement(name = "CAPTURERATE")
        public Integer           captureRate    = -1;
        @XmlElement(name = "EXPYIELD")
        public Integer           baseExp        = -1;
        @XmlElement(name = "BASEFRIENDSHIP")
        public Integer           baseFriendship = 70;
        @XmlElement(name = "EXPERIENCEMODE")
        public String            expMode;

        @XmlElement(name = "GENDERRATIO")
        public Integer           genderRatio    = -1;
        // MISC
        @XmlElement(name = "LOGIC")
        public Stats             logics;
        @XmlElement(name = "FORMEITEMS")
        public Stats             formeItems;

        // Old mega rules
        @XmlElement(name = "MEGARULES")
        public Stats             megaRules_old;
        // New Mega rules
        @XmlElement(name = "MegaRules")
        public List<XMLMegaRule> megaRules      = new ArrayList<>();

        @XmlElement(name = "MOVEMENTTYPE")
        public String            movementType   = "normal";
        @XmlElement(name = "Interact")
        public List<Interact>    interactions   = new ArrayList<>();
        @XmlElement(name = "SHADOWREPLACEMENTS")
        public String            shadowReplacements;
        @XmlElement(name = "HATEDMATERIALRULES")
        public String            hatedMaterials;

        @XmlElement(name = "ACTIVETIMES")
        public String            activeTimes;
    }

    @XmlRootElement(name = "MegaRule")
    public static class XMLMegaRule
    {
        @XmlAttribute(name = "Name")
        public String name;
        @XmlAttribute(name = "Preset")
        public String preset;
        @XmlAttribute(name = "Move")
        public String move;
        @XmlAttribute(name = "Ability")
        public String ability;
        @XmlElement(name = "Key")
        public Key    item;
        @XmlElement(name = "Key_Template")
        public String item_preset;
    }

    @XmlRootElement(name = "Document")
    public static class XMLDatabase
    {
        @XmlElement(name = "Pokemon")
        public List<XMLPokedexEntry> pokemon = new ArrayList<>();

        public void addEntry(XMLPokedexEntry toAdd)
        {
            if (map.containsKey(toAdd.name))
            {
                pokemon.remove(map.remove(toAdd.name));
            }
            pokemon.add(toAdd);
            Collections.sort(pokemon, ENTRYSORTER);
        }

        public void addOverrideEntry(XMLPokedexEntry entry, boolean overwrite)
        {
            for (XMLPokedexEntry e : pokemon)
            {
                if (e.name.equals(entry.name))
                {
                    if (overwrite)
                    {
                        pokemon.remove(e);
                        map.put(entry.name, entry);
                        pokemon.add(entry);
                        entry.mergeMissingFrom(e);
                        return;
                    }
                    else
                    {
                        e.mergeMissingFrom(entry);
                    }
                    return;
                }
            }
            pokemon.add(entry);
            map.put(entry.name, entry);
        }

        public Map<String, XMLPokedexEntry>  map        = new HashMap<>();

        public Map<XMLPokedexEntry, Integer> entriesMap = new HashMap<>();

        public void init()
        {
            if (map == null) map = new HashMap<>();
            map.clear();
            for (XMLPokedexEntry e : pokemon)
            {
                map.put(e.name, e);
            }
            entriesMap.clear();
            Collections.sort(pokemon, ENTRYSORTER);
            for (int i = 0; i < pokemon.size(); i++)
            {
                entriesMap.put(pokemon.get(i), i);
            }
        }

        public XMLPokedexEntry getEntry(String name, int number, boolean checkFormes, int default_)
        {
            boolean checkNum = number > 0;
            boolean checkName = name != null;
            XMLPokedexEntry test = null;
            for (int i = 0; i < pokemon.size(); i++)
            {
                XMLPokedexEntry temp = pokemon.get(i);
                boolean base = temp.base == null ? false : temp.base;
                // Exact name match, so return here.
                if (checkName && temp.name.equals(name) && (checkFormes || base)) return temp;
                // Set to number match and continue
                if (checkNum && temp.number == number && (checkFormes || base))
                {
                    test = temp;
                }
            }
            if (checkFormes && test != null && !test.name.equals(name)) test = null;
            if (test != null) return test;
            return default_ >= 0 ? pokemon.get(0) : null;
        }

        public XMLPokedexEntry getEntry(String name, int number, boolean checkFormes)
        {
            return getEntry(name, number, checkFormes, 0);
        }

        public XMLPokedexEntry next(XMLPokedexEntry entry, int dir)
        {
            if (entriesMap == null) entriesMap = new HashMap<>();
            if (entriesMap.isEmpty()) init();
            int index = 0;
            try
            {
                index = entriesMap.get(entry) + dir;
            }
            catch (Exception e)
            {
                init();
                index = entriesMap.get(entry) + dir;
            }
            XMLPokedexEntry ret;
            if (index > 0 && index < pokemon.size()) ret = pokemon.get(index);
            else if (index > pokemon.size()) ret = pokemon.get(0);
            else ret = pokemon.get(pokemon.size() - 1);
            System.out.println(entry.name + " " + ret.name + " " + entriesMap.get(entry) + " " + entriesMap.get(ret));
            return ret;
        }
    }

    @XmlRootElement(name = "Pokemon")
    public static class XMLPokedexEntry
    {
        @XmlAttribute
        public String    name;
        @XmlAttribute
        public Integer   number;
        @XmlAttribute
        public String    special;
        @XmlAttribute
        public Boolean   base       = false;
        @XmlAttribute
        public Boolean   breed      = true;
        @XmlAttribute
        public Boolean   dummy      = true;
        @XmlAttribute
        public Boolean   starter    = false;
        @XmlAttribute
        public Boolean   ridable    = true;
        @XmlAttribute
        public Boolean   legend     = false;
        @XmlAttribute
        public Boolean   hasShiny   = true;
        @XmlAttribute
        public String    gender     = "";
        @XmlAttribute
        public String    genderBase = "";
        @XmlAttribute
        public String    sound      = "";
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

        void mergeMissingFrom(XMLPokedexEntry other)
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
                if (moves.evolutionMoves != null)
                {
                    moves.evolutionMoves = other.moves.evolutionMoves;
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
    }

    public static void mergeNonDefaults(Object defaults, Object outOf, Object inTo)
    {
        if (outOf.getClass() != inTo.getClass())
            throw new IllegalArgumentException("To and From must be of the same class!");
        Field fields[] = new Field[] {};
        try
        {
            fields = outOf.getClass().getDeclaredFields();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Object valueOut;
        Object valueIn;
        Object valueDefault;
        for (Field field : fields)
        {
            try
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                field.setAccessible(true);
                valueOut = field.get(outOf);
                valueIn = field.get(inTo);
                valueDefault = field.get(defaults);
                if (valueOut == null) continue;
                if (valueIn == null && valueOut != null)
                {
                    field.set(inTo, valueOut);
                    continue;
                }

                boolean outIsDefault = valueOut == valueDefault
                        || (valueDefault != null && valueDefault.equals(valueOut));
                if (!outIsDefault)
                {
                    if (valueOut instanceof String)
                    {
                        field.set(inTo, valueOut);
                    }
                    else if (valueOut instanceof Object[])
                    {
                        field.set(inTo, ((Object[]) valueOut).clone());
                    }
                    else if (valueOut instanceof Map)
                    {
                        field.set(inTo, valueOut);
                    }
                    else if (valueOut instanceof Collection)
                    {
                        field.set(inTo, valueOut);
                    }
                    else
                    {
                        try
                        {
                            valueDefault = valueOut.getClass().newInstance();
                            mergeNonDefaults(valueDefault, valueOut, valueIn);
                            field.set(inTo, valueIn);
                        }
                        catch (Exception e)
                        {
                            field.set(inTo, valueOut);
                        }
                    }
                }
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object getSerializableCopy(Class<?> type, Object original, Predicate<Field> accepted,
            boolean nullDefaults) throws InstantiationException, IllegalAccessException
    {
        Field fields[] = new Field[] {};
        try
        {
            // returns the array of Field objects representing the public fields
            fields = type.getDeclaredFields();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Object copy = null;
        try
        {
            copy = type.newInstance();
        }
        catch (Exception e1)
        {
            copy = original;
        }
        if (copy == original || (copy != null && copy.equals(original))) { return copy; }
        Object value;
        Object defaultvalue;
        for (Field field : fields)
        {
            try
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                field.setAccessible(true);
                if (!accepted.test(field))
                {
                    field.set(copy, null);
                    continue;
                }
                value = field.get(original);
                defaultvalue = field.get(copy);
                if (value == null) continue;
                if (value.getClass().isPrimitive())
                {
                    field.set(copy, value);
                }
                else if (defaultvalue != null && defaultvalue.equals(value))
                {
                    field.set(copy, null);
                }
                else if (value instanceof String)
                {
                    if (((String) value).isEmpty())
                    {
                        field.set(copy, null);
                    }
                    else field.set(copy, value);
                }
                else if (value instanceof Object[])
                {
                    if (((Object[]) value).length == 0) field.set(copy, null);
                    else field.set(copy, value);
                }
                else if (value instanceof Map)
                {
                    if (((Map<?, ?>) value).isEmpty()) field.set(copy, null);
                    else field.set(copy, value);
                }
                else if (value instanceof Collection)
                {
                    if (((Collection<?>) value).isEmpty()) field.set(copy, null);
                    else
                    {
                        if (value instanceof List)
                        {
                            List args = (List) value;
                            ListIterator iter = args.listIterator();
                            while (iter.hasNext())
                            {
                                Object var = iter.next();
                                iter.set(getSerializableCopy(var.getClass(), var, accepted, nullDefaults));
                            }
                        }
                        field.set(copy, value);
                    }
                }
                else field.set(copy, getSerializableCopy(value.getClass(), value, accepted, nullDefaults));
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        return copy;
    }

    public static XMLDatabase   database;

    static Set<XMLPokedexEntry> entries = new HashSet<>();

    public static XMLDatabase loadDatabase(File file) throws Exception
    {
        return loadDatabase(new FileInputStream(file), true);
    }

    public static XMLDatabase loadDatabase(InputStream stream, boolean json) throws Exception
    {
        XMLDatabase database = null;
        InputStreamReader reader = new InputStreamReader(stream);
        if (json)
        {
            database = gson.fromJson(reader, XMLDatabase.class);
        }
        else
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            database = (XMLDatabase) unmarshaller.unmarshal(reader);
        }
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

    public static final Set<Field> CORESET   = new HashSet<>();
    public static final Set<Field> SPAWNS    = new HashSet<>();
    public static final Set<Field> DROPS     = new HashSet<>();
    public static final Set<Field> EVOLS     = new HashSet<>();
    public static final Set<Field> INTERACTS = new HashSet<>();

    public static void cleanup(XMLDatabase database, boolean cullEntries)
    {
        ListIterator<XMLPokedexEntry> iter = database.pokemon.listIterator();
        Set<Field> toIgnore = new HashSet<Field>();
        for (Field f : XMLPokedexEntry.class.getDeclaredFields())
        {
            if (f.getName().equals("name") || f.getName().equals("number"))
            {
                toIgnore.add(f);
            }
        }
        Predicate<Field> ignore = new Predicate<Field>()
        {
            @Override
            public boolean test(Field t)
            {
                return toIgnore.contains(t);
            }
        };
        while (iter.hasNext())
        {
            XMLPokedexEntry entry = iter.next();
            entry.number = null;
            if (isEmpty(entry.stats, null))
            {
                entry.stats = null;
            }
            if (cullEntries && isEmpty(entry, ignore)) iter.remove();
        }
    }

    public static void cleanup(XMLDatabase database)
    {
        cleanup(database, true);
    }

    public static boolean isEmpty(Object o, Predicate<Field> ignore)
    {
        if (o == null) return true;
        for (Field field : o.getClass().getDeclaredFields())
        {
            try
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                if (ignore != null && ignore.test(field)) continue;
                field.setAccessible(true);
                if (field.get(o) != null) return false;
            }
            catch (Exception e)
            {
            }

        }

        return true;
    }

    static
    {
        for (Field f : XMLPokedexEntry.class.getDeclaredFields())
        {
            if (f.getName().equals("name") || f.getName().equals("number") || f.getName().equals("stats"))
            {
                CORESET.add(f);
            }
        }
        for (Field f : Interact.class.getDeclaredFields())
        {
            CORESET.add(f);
        }
        for (Field f : Evolution.class.getDeclaredFields())
        {
            CORESET.add(f);
        }
        for (Field f : SpawnRule.class.getDeclaredFields())
        {
            CORESET.add(f);
        }
        for (Field f : Drop.class.getDeclaredFields())
        {
            CORESET.add(f);
        }
        for (Field f : XMLMegaRule.class.getDeclaredFields())
        {
            CORESET.add(f);
        }
        for (Field f : Key.class.getDeclaredFields())
        {
            CORESET.add(f);
        }
        for (Field f : Action.class.getDeclaredFields())
        {
            CORESET.add(f);
        }
        for (Field f : Stats.class.getDeclaredFields())
        {
            CORESET.add(f);
        }
        for (Field f : StatsNode.class.getDeclaredFields())
        {
            if (f.getName().equals("spawnRules")) SPAWNS.add(f);

            if (f.getName().equals("drops")) DROPS.add(f);
            if (f.getName().equals("held")) DROPS.add(f);
            if (f.getName().equals("lootTable")) DROPS.add(f);
            if (f.getName().equals("heldTable")) DROPS.add(f);

            if (f.getName().equals("evolutions")) EVOLS.add(f);
            if (f.getName().equals("megaRules")) EVOLS.add(f);

            if (f.getName().equals("interactions")) INTERACTS.add(f);
            if (f.getName().equals("prey")) INTERACTS.add(f);
            if (f.getName().equals("species")) INTERACTS.add(f);
            if (f.getName().equals("foodMat")) INTERACTS.add(f);
            if (f.getName().equals("hatedMaterials")) INTERACTS.add(f);
            if (f.getName().equals("activeTimes")) INTERACTS.add(f);
            if (f.getName().equals("specialEggRules")) INTERACTS.add(f);
        }
    }

    public static final Predicate<Field> ACCEPTALL      = new Predicate<Field>()
                                                        {
                                                            @Override
                                                            public boolean test(Field t)
                                                            {
                                                                return true;
                                                            }
                                                        };

    public static final Predicate<Field> JUSTMOBS       = new Predicate<Field>()
                                                        {
                                                            @Override
                                                            public boolean test(Field t)
                                                            {
                                                                return !DROPS.contains(t) && !SPAWNS.contains(t)
                                                                        && !INTERACTS.contains(t) && !EVOLS.contains(t);
                                                            }
                                                        };

    public static final Predicate<Field> SPAWNSONLY     = new Predicate<Field>()
                                                        {
                                                            @Override
                                                            public boolean test(Field t)
                                                            {
                                                                return CORESET.contains(t) || SPAWNS.contains(t);
                                                            }
                                                        };

    public static final Predicate<Field> DROPSONLY      = new Predicate<Field>()
                                                        {
                                                            @Override
                                                            public boolean test(Field t)
                                                            {
                                                                return CORESET.contains(t) || DROPS.contains(t);
                                                            }
                                                        };

    public static final Predicate<Field> EVOLSINTERACTS = new Predicate<Field>()
                                                        {
                                                            @Override
                                                            public boolean test(Field t)
                                                            {
                                                                return CORESET.contains(t) || INTERACTS.contains(t)
                                                                        || EVOLS.contains(t);
                                                            }
                                                        };

    public static void write(File file) throws Exception
    {
        // if (true)
        // {
        // System.out.println("");
        // System.out.println("");
        // System.out.println("");
        // System.out.println("");
        // XMLPokedexEntry entry = database.getEntry("Raticate", 20, false);
        // getSerializableCopy(entry.getClass(), entry, ACCEPTALL, false);
        //
        // return;
        // }

        file = new File(file.getParentFile(), "pokemobs_.json");
        File dataFile = file;
        database.pokemon.sort(ENTRYSORTER);
        database.pokemon.replaceAll(new UnaryOperator<XMLEntries.XMLPokedexEntry>()
        {
            @Override
            public XMLPokedexEntry apply(XMLPokedexEntry t)
            {
                try
                {
                    return (XMLPokedexEntry) getSerializableCopy(t.getClass(), t, ACCEPTALL, false);
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    return t;
                }
            }
        });
        Map<String, XMLPokedexEntry> back = database.map;
        Map<XMLPokedexEntry, Integer> back2 = database.entriesMap;
        database.map = null;
        database.entriesMap = null;
        String json = gson.toJson(database);
        database.map = back;
        database.entriesMap = back2;
        FileWriter writer = new FileWriter(file);
        writer.append(json);
        writer.close();

        XMLDatabase database = loadDatabase(dataFile);
        file = new File(file.getParentFile(), "pokemobs_pokedex.json");
        database.pokemon.sort(ENTRYSORTER);
        database.pokemon.replaceAll(new UnaryOperator<XMLEntries.XMLPokedexEntry>()
        {
            @Override
            public XMLPokedexEntry apply(XMLPokedexEntry t)
            {
                try
                {
                    return (XMLPokedexEntry) getSerializableCopy(t.getClass(), t, JUSTMOBS, true);
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    return t;
                }
            }
        });
        database.map = null;
        database.entriesMap = null;
        json = gson.toJson(database);
        writer = new FileWriter(file);
        writer.append(json);
        writer.close();

        database = loadDatabase(dataFile);
        file = new File(file.getParentFile(), "pokemobs_spawns.json");
        database.pokemon.sort(ENTRYSORTER);
        database.pokemon.replaceAll(new UnaryOperator<XMLEntries.XMLPokedexEntry>()
        {
            @Override
            public XMLPokedexEntry apply(XMLPokedexEntry t)
            {
                try
                {
                    return (XMLPokedexEntry) getSerializableCopy(t.getClass(), t, SPAWNSONLY, true);
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    return t;
                }
            }
        });
        cleanup(database);
        database.map = null;
        database.entriesMap = null;
        json = gson.toJson(database);
        writer = new FileWriter(file);
        writer.append(json);
        writer.close();

        database = loadDatabase(dataFile);
        file = new File(file.getParentFile(), "pokemobs_drops.json");
        database.pokemon.sort(ENTRYSORTER);
        database.pokemon.replaceAll(new UnaryOperator<XMLEntries.XMLPokedexEntry>()
        {
            @Override
            public XMLPokedexEntry apply(XMLPokedexEntry t)
            {
                try
                {
                    return (XMLPokedexEntry) getSerializableCopy(t.getClass(), t, DROPSONLY, true);
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    return t;
                }
            }
        });
        cleanup(database, false);
        database.map = null;
        database.entriesMap = null;
        json = gson.toJson(database);
        writer = new FileWriter(file);
        writer.append(json);
        writer.close();

        database = loadDatabase(dataFile);
        file = new File(file.getParentFile(), "pokemobs_interacts.json");
        database.pokemon.sort(ENTRYSORTER);
        database.pokemon.replaceAll(new UnaryOperator<XMLEntries.XMLPokedexEntry>()
        {
            @Override
            public XMLPokedexEntry apply(XMLPokedexEntry t)
            {
                try
                {
                    return (XMLPokedexEntry) getSerializableCopy(t.getClass(), t, EVOLSINTERACTS, true);
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    return t;
                }
            }
        });
        cleanup(database);
        database.map = null;
        database.entriesMap = null;
        json = gson.toJson(database);
        writer = new FileWriter(file);
        writer.append(json);
        writer.close();
    }
}
