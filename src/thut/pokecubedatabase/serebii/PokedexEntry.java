package thut.pokecubedatabase.serebii;

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

import thut.pokecubedatabase.Main;
import thut.pokecubedatabase.ParseHandler;
import thut.pokecubedatabase.XMLEntries;
import thut.pokecubedatabase.XMLEntries.XMLPokedexEntry;

public class PokedexEntry
{
    HashMap<String, PokedexEntry> formes;
    XMLPokedexEntry               entry;

    public PokedexEntry(XMLPokedexEntry entry)
    {
        this.entry = entry;
    }

    public PokedexEntry(String name, int number)
    {
        XMLPokedexEntry old = XMLEntries.getDatabase(Main.file).getEntry(name, number, false);
        if (old != null) entry = old;
        else
        {
            entry = new XMLPokedexEntry();
            entry.name = name;
            entry.number = number + "";
            XMLEntries.getDatabase(Main.file).pokemon.add(entry);
            XMLEntries.getDatabase(Main.file).pokemon.sort(new Comparator<XMLPokedexEntry>()
            {
                @Override
                public int compare(XMLPokedexEntry o1, XMLPokedexEntry o2)
                {
                    if (o1.number.compareTo(o2.number) != 0) return o1.number.compareTo(o2.number);
                    int diff = 0;
                    if (Boolean.parseBoolean(o1.base) && !Boolean.parseBoolean(o2.base)) diff = -1;
                    else if (Boolean.parseBoolean(o2.base) && !Boolean.parseBoolean(o1.base)) diff = 1;
                    if (diff != 0) return diff;
                    return o1.name.compareTo(o2.name);
                }
            });
            XMLEntries.getDatabase(Main.file).init();
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
        entry.stats.types.values.put(new QName(key), type);
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
        entry.moves.lvlupMoves.values.put(new QName("lvl_" + lvl), ParseHandler.convertName(move));
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
                moveset.add(ParseHandler.convertName(s));
            move = ParseHandler.convertName(move);
            moveset.add(move);
            List<String> movesList = new ArrayList<>(moveset);
            Collections.sort(movesList);
            entry.moves.misc.moves = movesList.get(0);
            for (int i = 1; i < movesList.size(); i++)
                entry.moves.misc.moves = entry.moves.misc.moves + ", " + movesList.get(i);
        }
    }

    private void parseEVs(Matcher matcher, String info)
    {
        String val = matcher.group().replace("Points ", "");
        String val2 = val.substring(0, val.lastIndexOf(" "));
        entry.stats.expMode = val2.substring(0, val2.lastIndexOf(" "));
        entry.stats.baseFriendship = Integer.parseInt(val2.replace(entry.stats.expMode, "").trim()) + "";
        val = info.substring(info.indexOf(val) + val.length() - 1, info.lastIndexOf("(s)")).replace("(s)", "");
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
