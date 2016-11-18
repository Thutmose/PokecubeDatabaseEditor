package thut.pokecubedatabase.serebii;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.IValueFixer;
import thut.pokecubedatabase.Main;
import thut.pokecubedatabase.pokedex.XMLEntries;
import thut.pokecubedatabase.pokedex.XMLEntries.XMLPokedexEntry;

public class PokedexEntry
{
    HashMap<String, PokedexEntry> formes;
    XMLPokedexEntry               entry;
    public static IValueFixer     typeFixer = new IValueFixer()
                                            {
                                                @Override
                                                public String fix(String input)
                                                {
                                                    String typeName = input
                                                            .replace("/" + PokedexChecker.pokedex + "/", "")
                                                            .replace(".shtml", "");
                                                    typeName = Character.toUpperCase(typeName.charAt(0))
                                                            + typeName.substring(1);
                                                    return typeName;
                                                }
                                            };

    public PokedexEntry(XMLPokedexEntry entry)
    {
        this.entry = entry;
    }

    public void initField(Field f, Object obj) throws InstantiationException, IllegalAccessException
    {
        if (f.getType() != String.class && f.get(obj) == null && !f.getType().isInterface()
                && !f.getName().equals("body"))
        {
            Class<?> c1 = f.getType();
            Object o1 = c1.newInstance();
            f.set(obj, o1);
            for (Field f1 : c1.getFields())
            {
                try
                {
                    if (f1.get(o1) == null) initField(f1, o1);
                }
                catch (Exception e)
                {
                    System.err.println("Error with " + f1.getName() + " " + f1.getType() + " " + f1.get(o1));
                }
            }
        }
    }

    public PokedexEntry(String name, int number)
    {
        XMLPokedexEntry old = XMLEntries.getDatabase(Main.pokedexfile).getEntry(name, number, false, -1);
        if (old != null) entry = old;
        else
        {
            entry = new XMLPokedexEntry();
            System.out.println("Creating new Entry for " + name);
            entry.name = name;
            entry.number = number + "";
            entry.base = "true";
            for (Field f : XMLPokedexEntry.class.getFields())
            {
                try
                {
                    initField(f, entry);
                }
                catch (Exception e)
                {
                    System.err.println("Error with " + f.getName());
                }
            }
            XMLEntries.getDatabase(Main.pokedexfile).pokemon.add(entry);
            XMLEntries.getDatabase(Main.pokedexfile).pokemon.sort(new Comparator<XMLPokedexEntry>()
            {
                @Override
                public int compare(XMLPokedexEntry o1, XMLPokedexEntry o2)
                {
                    int num1 = Integer.parseInt(o1.number);
                    int num2 = Integer.parseInt(o2.number);
                    if (num1 != num2) return num1 - num2;
                    int diff = 0;
                    if (Boolean.parseBoolean(o1.base) && !Boolean.parseBoolean(o2.base)) diff = -1;
                    else if (Boolean.parseBoolean(o2.base) && !Boolean.parseBoolean(o1.base)) diff = 1;
                    if (diff != 0) return diff;
                    return o1.name.compareTo(o2.name);
                }
            });
            XMLEntries.getDatabase(Main.pokedexfile).init();
        }
    }

    public void parseGenderAndInfo(String input)
    {
        int indexMale = input.indexOf("Male M:") + "Male M:".length();
        int indexFemale = input.indexOf("Female F:");
        String text = input;
        if (input.contains("is Genderless"))
        {

        }
        else
        {
            String male = input.substring(indexMale, indexFemale).trim().replace("%", "");
            String female = input.substring(indexFemale + "Female F:".length()).trim();
            text = input.substring(indexFemale + "Female F:".length() + female.indexOf("%") + 3);
            female = female.substring(0, female.indexOf("%"));
            if (!male.isEmpty())
            {
                float f = Float.parseFloat(female);
                entry.stats.genderRatio = (int) (f * 254 / 100) + "";
            }
        }

        int classStart = text.indexOf("Base Egg Steps") + "Base Egg Steps ".length();
        int classEnd = text.indexOf("Abilities:");

        String classInfo = text.substring(classStart, classEnd);

        String pattern = "\\d*\\.\\dm";
        Matcher matcher = Pattern.compile(pattern).matcher(classInfo);
        // if (matcher.find())
        // {
        // float size = Float.parseFloat(matcher.group().replace("m", ""));
        // // TODO decide if to use size.
        // }
        pattern = "\\d*\\.\\dkg";
        matcher = Pattern.compile(pattern).matcher(classInfo);
        if (matcher.find())
        {
            entry.stats.mass = Float.parseFloat(matcher.group().replace("kg", "")) + "";
        }
        pattern = "kg \\d+ ";
        matcher = Pattern.compile(pattern).matcher(classInfo);
        if (matcher.find())
        {
            entry.stats.captureRate = Integer.parseInt(matcher.group().replace("kg ", "").trim()) + "";
        }

        String abilityInfo = text.substring(classEnd + "Abilities: ".length());
        int abilityEnd = abilityInfo.indexOf(":");
        abilityInfo = abilityInfo.substring(0, abilityEnd);

        String[] args = abilityInfo.split("-");

        if (args.length > 1) args[args.length - 1] = args[args.length - 1].replace(args[0].trim(), "");
        else
        {
            args[0] = args[0].trim().substring(0, args[0].trim().length() / 2);
        }

        String abilities = "";
        String hiddenAbilities = "";
        for (int i = 0; i < args.length; i++)
        {
            String val = args[i];
            if (!val.contains("("))
            {
                abilities += val.trim() + ", ";
            }
            else
            {
                val = val.substring(0, val.indexOf("("));
                hiddenAbilities += val.trim() + ", ";
            }
        }
        if (abilities.endsWith(", ")) abilities = abilities.substring(0, abilities.length() - 2);
        if (hiddenAbilities.endsWith(", "))
            hiddenAbilities = hiddenAbilities.substring(0, hiddenAbilities.length() - 2);
        entry.stats.abilities.values.put(new QName("hidden"), hiddenAbilities);
        entry.stats.abilities.values.put(new QName("normal"), abilities);
        String info = text.substring(text.indexOf("Battle?") + "Battle?".length(), text.indexOf("Damage Taken"));
        pattern = "(Points ).{2,20}\\s\\d";
        matcher = Pattern.compile(pattern).matcher(info);
        if (matcher.find())
        {
            try
            {
                parseEVs(matcher, info);
            }
            catch (Exception e)
            {
                System.err.println("No EVs for " + entry.name + " " + e.toString());
            }
        }
    }

    public void setType(int index, String type)
    {
        String key = index == 0 ? "type1" : "type2";
        if (type != null && !type.isEmpty()) entry.stats.types.values.put(new QName(key), typeFixer.fix(type));
        else entry.stats.types.values.remove(new QName(key), type);
    }

    public void setAbilities(boolean hidden, String abilities)
    {
        String key = hidden ? "hidden" : "normal";
        if (abilities != null && !abilities.isEmpty()) entry.stats.types.values.put(new QName(key), abilities);
        else entry.stats.types.values.remove(new QName(key), abilities);
    }

    public void setBaseStat(int index, String stat)
    {
        entry.stats.stats.values.put(new QName(Main.statsNames[index]), stat);
    }

    public void clearMoves()
    {
        entry.moves.misc.moves = null;
        entry.moves.lvlupMoves.values.clear();
    }

    public void addLvlMove(int lvl, String move)
    {
        QName key = new QName("lvl_" + lvl);
        String old = entry.moves.lvlupMoves.values.get(key);
        if (old != null && old.contains(JsonMoves.convertMoveName(move))) return;
        else if (old == null) entry.moves.lvlupMoves.values.put(key, JsonMoves.convertMoveName(move));
        else entry.moves.lvlupMoves.values.put(key, old + "," + JsonMoves.convertMoveName(move));
    }

    public void addOtherMove(String move)
    {
        if (entry.moves.misc.moves == null)
        {
            entry.moves.misc.moves = move;
        }
        else
        {
            String[] moves = entry.moves.misc.moves.split(", ");
            Set<String> moveset = new HashSet<>();
            for (String s : moves)
                moveset.add(JsonMoves.convertMoveName(s));
            move = JsonMoves.convertMoveName(move);
            moveset.add(move);
            List<String> movesList = new ArrayList<>(moveset);
            Collections.sort(movesList);
            entry.moves.misc.moves = movesList.get(0);
            for (int i = 1; i < movesList.size(); i++)
                entry.moves.misc.moves = entry.moves.misc.moves + ", " + movesList.get(i);
        }
    }

    public void parseEVs(Matcher matcher, String info)
    {
        String val = matcher.group().replace("Points ", "");
        String val2 = val.substring(0, val.lastIndexOf(" "));
        entry.stats.expMode = val2.substring(0, val2.lastIndexOf(" "));
        entry.stats.baseFriendship = Integer.parseInt(val2.replace(entry.stats.expMode, "").trim()) + "";
        val = info.substring(info.indexOf(val) + val.length() - 1, info.lastIndexOf("(s)")).replace("(s)", "");

    }

    public void parseEVs(String val)
    {
        val = val.replace(" Point(s)", "");
        String stat;
        stat = " HP";
        int[] evs = new int[6];
        if (val.contains(stat))
        {
            int index = val.indexOf(stat);
            String var = val.substring(index - 1, index);
            try
            {
                evs[0] = Integer.parseInt(var);
            }
            catch (NumberFormatException e)
            {
            }
        }
        stat = " Attack";
        if (val.contains(stat))
        {
            int index = val.indexOf(stat);
            String var = val.substring(index - 1, index);
            try
            {
                evs[1] = Integer.parseInt(var);
            }
            catch (NumberFormatException e)
            {
            }
        }
        stat = " Defense";
        if (val.contains(stat))
        {
            int index = val.indexOf(stat);
            String var = val.substring(index - 1, index);
            try
            {
                evs[2] = Integer.parseInt(var);
            }
            catch (NumberFormatException e)
            {
            }
        }
        stat = " Sp. Attack";
        if (val.contains(stat))
        {
            int index = val.indexOf(stat);
            String var = val.substring(index - 1, index);
            try
            {
                evs[3] = Integer.parseInt(var);
            }
            catch (NumberFormatException e)
            {
            }
        }
        stat = " Sp. Defense";
        if (val.contains(stat))
        {
            int index = val.indexOf(stat);
            String var = val.substring(index - 1, index);
            try
            {
                evs[4] = Integer.parseInt(var);
            }
            catch (NumberFormatException e)
            {
            }
        }
        stat = " Speed";
        if (val.contains(stat))
        {
            int index = val.indexOf(stat);
            String var = val.substring(index - 1, index);
            try
            {
                evs[5] = Integer.parseInt(var);
            }
            catch (NumberFormatException e)
            {
            }
        }
        for (int i = 0; i < 6; i++)
        {
            if (evs[i] > 0)
            {
                entry.stats.evs.values.put(new QName(Main.statsNames[i]), evs[i] + "");
            }
        }
    }

    void editDatabase()
    {

    }
}
