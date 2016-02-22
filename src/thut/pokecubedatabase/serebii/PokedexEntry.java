package thut.pokecubedatabase.serebii;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import thut.pokecubedatabase.Main;
import thut.pokecubedatabase.ParseHandler;

public class PokedexEntry
{

    HashMap<String, PokedexEntry> formes;

    int               number;
    String            name;
    String[]          types           = new String[2];
    int               genderRatio     = 255;
    float             size;
    float             mass;
    int               captureRate;
    String            abilities       = "";
    String            hiddenAbilities = "";
    String            expMode;
    int[]             baseStats       = new int[6];
    int[]             evs             = new int[6];
    int               baseHappiness   = -1;
    ArrayList<String> lvlMoves        = new ArrayList<>();
    ArrayList<String> otherMoves      = new ArrayList<>();

    public PokedexEntry(String name, int number)
    {
        this.name = name;
        this.number = number;
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
                genderRatio = (int) (f * 254 / 100);
            }
        }

        int classStart = text.indexOf("Base Egg Steps") + "Base Egg Steps ".length();
        int classEnd = text.indexOf("Abilities:");

        String classInfo = text.substring(classStart, classEnd);

        String pattern = "\\d*\\.\\dm";
        Matcher matcher = Pattern.compile(pattern).matcher(classInfo);
        if (matcher.find())
        {
            size = Float.parseFloat(matcher.group().replace("m", ""));
        }
        pattern = "\\d*\\.\\dkg";
        matcher = Pattern.compile(pattern).matcher(classInfo);
        if (matcher.find())
        {
            mass = Float.parseFloat(matcher.group().replace("kg", ""));
        }
        pattern = "kg \\d+ ";
        matcher = Pattern.compile(pattern).matcher(classInfo);
        if (matcher.find())
        {
            captureRate = Integer.parseInt(matcher.group().replace("kg ", "").trim());
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
                System.err.println("No EVs for " + name + " " + e.toString());
            }
        }
    }

    private void parseEVs(Matcher matcher, String info)
    {
        String val = matcher.group().replace("Points ", "");
        String val2 = val.substring(0, val.lastIndexOf(" "));
        expMode = val2.substring(0, val2.lastIndexOf(" "));
        baseHappiness = Integer.parseInt(val2.replace(expMode, "").trim());
        val = info.substring(info.indexOf(val) + val.length() - 1, info.lastIndexOf("(s)")).replace("(s)", "");
        String stat;

        stat = " HP";
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
    }

    void editDatabase()
    {
        try
        {
            if (!Main.instance.hasEntry(name, -1))
            {
                if (Main.instance.hasEntry(name, -1, true))
                {
                    Element e = Main.instance.getEntry(name, number, false, true);
                    name = e.getAttribute("name");
                    return;
                }
                else
                {
                    System.err.println("No Entry for " + name);
                    return;
                }
            }

        }
        catch (ParserConfigurationException | SAXException | IOException e1)
        {
            e1.printStackTrace();
            return;
        }

        Main.instance.status.setText("");
        Main.instance.status.append(name + "\n");
        Main.instance.status.append(this.abilities + "\n");
        Main.instance.status.append(this.hiddenAbilities + "\n");

        try
        {
            Main.instance.moves = false;
            Main.instance.editEntry(name, "ABILITY", "normal", abilities);
            Main.instance.editEntry(name, "ABILITY", "hidden", hiddenAbilities);

            String[] attribs = Main.statAttribs.get("BASESTATS").split(",");

            boolean hasEVs = false;
            for (int i = 0; i < 6; i++)
                if (evs[i] != 0) hasEVs = true;

            for (int i = 0; i < attribs.length; i++)
            {
                String stat = attribs[i];
                Main.instance.editEntry(name, "BASESTATS", stat, baseStats[i] + "");
                if (hasEVs) Main.instance.editEntry(name, "EVYIELD", stat, evs[i] + "");
            }

            if (baseHappiness != -1) Main.instance.editEntry(name, "BASEFRIENDSHIP", null, baseHappiness + "");
            if (expMode != null) Main.instance.editEntry(name, "EXPERIENCEMODE", null, expMode);
            Main.instance.editEntry(name, "GENDERRATIO", null, genderRatio + "");
            Main.instance.editEntry(name, "MASSKG", null, mass + "");

            if (types[0] != null) Main.instance.editEntry(name, "TYPE", "type1", types[0]);
            if (types[1] != null) Main.instance.editEntry(name, "TYPE", "type2", types[1]);

            Main.instance.moves = true;

            HashMap<String, String> levelmoves = new HashMap<>();

            if (!lvlMoves.isEmpty())
            {
                Main.instance.clearTag(name, "LVLUP");
            }
            for (String s : lvlMoves)
            {
                String[] args = s.split(":");
                String key = "lvl_" + args[0].trim();
                String val = "";
                if (levelmoves.containsKey(key))
                {
                    val = levelmoves.get(key) + ", ";
                }
                val += ParseHandler.convertName(args[1]);
                levelmoves.put(key, val);
            }
            for (String key : levelmoves.keySet())
            {
                Main.instance.editEntry(name, "LVLUP", key, levelmoves.get(key));
            }
            String value = "";
            HashSet<String> added = new HashSet<>();
            for (String s : otherMoves)
            {
                String move = ParseHandler.convertName(s);
                if (!added.contains(move))
                {
                    value += move + ", ";
                    added.add(move);
                }
            }
            if (value.isEmpty()) return;

            value = value.substring(0, value.length() - 2);
            Main.instance.editEntry(name, "MISC", "moves", value);
            Main.instance.moves = false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
