package thut.pokecubedatabase.serebii;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import thut.pokecubedatabase.Main;
import thut.pokecubedatabase.pokedex.XMLEntries;
import thut.pokecubedatabase.pokedex.XMLEntries.XMLPokedexEntry;

public class PokedexChecker
{

    static HashSet<String> movesets        = new HashSet<>();
    static HashSet<String> move_sections   = new HashSet<>();
    static HashSet<String> z_move_sections = new HashSet<>();
    static HashSet<String> other_forms     = new HashSet<>();
    public static String   pokedex         = "pokedex-sm";   // pokedex-xy
    public static String   typeLoc         = "pokedex-bw";

    static
    {
        movesets.add("Special Moves");
        movesets.add("Move Tutor");
        movesets.add("Egg Moves");
        movesets.add("TM & HM Attacks");
        movesets.add("Pre-Evolution Only Moves");
        movesets.add("Transfer Only Moves");
        movesets.add("Stats");

        move_sections.add("Generation VI Level Up");
        move_sections.add("Generation VII Level Up");
        move_sections.add("Sun/Moon Level Up");
        move_sections.add("Ultra Sun/Ultra Moon Level Up");
        move_sections.add("Special Moves");
        move_sections.add("Move Tutor");
        move_sections.add("Egg Moves");
        move_sections.add("TM & HM Attacks");
        move_sections.add("Pre-Evolution Only Moves");
        move_sections.add("Transfer Only Moves");
        move_sections.add("Ultra Sun/Ultra Moon Move Tutor Attacks");

        z_move_sections.add("Usable Z Moves");
        z_move_sections.add("Usable Z Moves (Transfer Only/Improved)");

        other_forms.add("Mega Evolution");
        other_forms.add("Mega Evolution X");
        other_forms.add("Mega Evolution Y");
        other_forms.add("Primal Reversion");
    }

    static boolean isMovesSection(String line)
    {
        for (String s : move_sections)
        {
            if (line.contains(s)) return true;
        }
        return false;
    }

    static boolean isZMovesSection(String line)
    {
        for (String s : z_move_sections)
        {
            if (line.contains(s)) return true;
        }
        return false;
    }

    public PokedexChecker()
    {
    }

    void parseForName(String name) throws IOException
    {
        XMLPokedexEntry old = XMLEntries.getDatabase(Main.pokedexfile).getEntry(name, -1, true, -1);
        if (old == null) throw new IOException("Entry does not exist for " + name);
        PokedexEntry entry = new PokedexEntry(old);
        parseForNumber(entry.entry.number);
    }

    void parseForNumber(int num) throws IOException
    {
        String numStr = "" + num;
        if (numStr.length() == 1) numStr = "00" + numStr;
        else if (numStr.length() == 2) numStr = "0" + numStr;
        // Make a URL to the web page
        String html = "http://www.serebii.net/" + pokedex + "/" + numStr + ".shtml";
        Document doc = Jsoup.connect(html).get();
        doc.outputSettings().escapeMode(EscapeMode.xhtml);
        PokedexEntry[] entries = createEntry(doc, num);
        for (PokedexEntry entry : entries)
            parseEntry(entry, doc);
    }

    private PokedexEntry[] createEntry(Document doc, int num)
    {
        List<PokedexEntry> entries = new ArrayList<>();
        Elements tables = doc.select("table");
        PokedexEntry entry;
        Elements baseRow = null;
        for (Element table : tables)
        {
            String attr2 = table.attr("class");
            if (attr2.equals("dextable"))
            {
                Elements rows = table.select("tr");
                String firstLine = rows.get(0).select("td").text();
                if (firstLine.contains("Picture Name Other Names") || other_forms.contains(firstLine))
                {
                    String mega = null;
                    if (firstLine.contains("Mega"))
                    {
                        mega = "Mega";
                        if (firstLine.contains("X"))
                        {
                            mega = mega + "-X";
                        }
                        if (firstLine.contains("Y"))
                        {
                            mega = mega + "-Y";
                        }
                    }
                    else if (firstLine.contains("Primal")) mega = "Primal";
                    if (baseRow == null) baseRow = rows;
                    entry = parseInitialRowsAndCreateEntry(rows, num, other_forms.contains(firstLine) ? 2 : 1, false,
                            mega);
                    entries.add(entry);
                }
                if (firstLine.startsWith("Stats - Alola"))
                {
                    entry = parseInitialRowsAndCreateEntry(baseRow, num, 1, true, null);
                    entries.add(entry);
                }
            }
        }
        return entries.toArray(new PokedexEntry[0]);
    }

    private static void parseMoves(PokedexEntry entry, Elements rows, String firstLine, MovesJson validMoves)
    {
        boolean alola = entry.entry.name.contains("Alola");
        boolean lvlup = firstLine.contains("Level Up");
        if (lvlup)
        {
            if (firstLine.contains("Alola") && !alola) lvlup = false;
            else if (alola && !firstLine.contains("Alola")) lvlup = false;
        }

        if (lvlup)
        {
            // Level up moves, clear them first.
            entry.entry.moves.lvlupMoves.values.clear();
            for (int i = 2; i + 1 < rows.size(); i += 2)
            {
                Elements values = rows.get(i).select("td");
                String var = values.get(0).text();
                int level = 1;
                try
                {
                    level = Integer.parseInt(var);
                }
                catch (NumberFormatException e)
                {
                    if (var.equals("Evolve")) level = -1;
                }
                String move = values.get(1).text();
                if (level > 0)
                {
                    entry.addLvlMove(level, move);
                }
                else
                {
                    entry.addEvolutionMove(move);
                }
            }
        }
        if (isMovesSection(firstLine))
        {
            // TM moves
            // TODO make this also check which forme before adding.
            boolean formeInfo = false;
            try
            {
                Elements headers = rows.get(1).select("th");
                formeInfo = headers.get(headers.size() - 2).text().equals("Effect %")
                        && !firstLine.contains("Transfer Only Moves");
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                return;
            }
            for (int i = 0; i < rows.size(); i++)
            {
                Elements values = rows.get(i).select("td");
                Elements vars = values.select("a");
                if (vars.isEmpty() || validMoves.getEntry(vars.get(0).text(), false) == null) continue;
                String move = vars.get(0).text();
                boolean valid = !formeInfo;
                if (!valid)
                {
                    Elements formes = values.select("img");
                    for (Element e : formes)
                    {
                        String src = e.attr("src");
                        if (!src.contains("/pokedex-sm/icon/")) continue;
                        if (src.contains("-a") && alola)
                        {
                            valid = true;
                            break;
                        }
                        if (!src.contains("-a") && !alola)
                        {
                            valid = true;
                            break;
                        }
                    }
                }
                if (valid)
                {
                    entry.addOtherMove(move);
                }
            }
        }
        else if (isZMovesSection(firstLine))
        {
            // Z Moves
        }
    }

    private void parseEntry(PokedexEntry entry, Document doc)
    {
        Elements tables = doc.select("table");
        MovesJson validMoves = JsonMoves.getMoves(Main.movesFile);
        boolean erroredMoves = false;
        if (entry == null) System.out.println("null?");
        boolean base = entry.entry.base == null ? false : entry.entry.base;
        boolean mega = !base && entry.entry.name.contains(" Mega");
        boolean primal = !base && entry.entry.name.contains(" Primal");
        boolean abilities = true;

        String stats = mega ? "Stats - Mega Evolution" : primal ? "Stats - Primal Reversion" : "Stats";
        System.out.println(entry.entry.name);
        for (Element table : tables)
        {
            String attr2 = table.attr("class");
            if (attr2.equals("dextable"))
            {
                Elements rows = table.select("tr");
                String firstLine = rows.get(0).select("td").text();

                if (firstLine.contains("Abilities:") && abilities)
                {
                    parseAbilitiesHappinessEVs(rows, entry);
                    abilities = mega || primal;
                }

                if (!erroredMoves) try
                {
                    parseMoves(entry, rows, firstLine, validMoves);
                }
                catch (Exception e)
                {
                    erroredMoves = true;
                    System.out.println("error with moves for " + entry.entry.name);
                }

                if (firstLine.equals(stats))
                {
                    // Stats
                    Elements values = rows.get(2).select("td");
                    for (int i = 1; i < 7; i++)
                    {
                        entry.setBaseStat(i - 1, values.get(i).text());
                    }
                }
            }
        }
        entry.editDatabase();
        Main.instance.setStatus("Updated " + entry.entry.name);
    }

    /** Parse through and fill in the abilities, exp mode, happiness, evs, and
     * maybe the boolean of has mega evolution later.
     * 
     * @param rows
     * @param entry */
    public static void parseAbilitiesHappinessEVs(Elements rows, PokedexEntry entry)
    {
        Elements expValues = null;
        for (int i = 0; i < rows.size() - 1; i++)
        {
            Elements values = rows.get(i).select("td");
            if (values.get(0).text().equals("Experience Growth"))
            {
                expValues = rows.get(i + 1).select("td");
                break;
            }
        }
        if (expValues == null) { return; }

        // Abilities, exp mode, base happiness, evs, does it
        // mega evolve.
        Elements values = rows.get(1).select("b");
        boolean foundHidden = false;
        String normal = "";
        String hidden = null;
        for (int i = 0; i < values.size(); i++)
        {
            foundHidden = values.get(i).text().equals("Hidden Ability");
            if (!foundHidden)
            {
                if (normal.isEmpty()) normal = values.get(i).text();
                else normal = normal + ", " + values.get(i).text();
            }
            else
            {
                hidden = values.get(i + 1).text();
                break;
            }
        }
        entry.setAbilities(false, normal);
        entry.setAbilities(true, hidden);

        values = expValues;

        // Exp mode
        String text = values.get(0).text();
        int index = text.indexOf("Points") + 7;
        entry.entry.stats.expMode = text.substring(index, text.length()).trim();

        // Base Happiness
        entry.entry.stats.baseFriendship = Integer.parseInt(values.get(1).text().trim());

        // EVs
        // This means it doesn't have other formes listed.
        if (values.get(2).select("b").size() == 0)
        {
            entry.parseEVs(values.get(2).text());
        }
        else
        {
            Pattern numMatcher = Pattern.compile("[0-9]");
            // TODO make this allow selecting which forme to pick
            // from the set.
            Element nums0 = values.get(2);
            String line = nums0.select("td").html().replaceAll("<b>", "").replaceAll("</b>", "");
            String[] lines = line.split("<br>");
            List<String> evs = new ArrayList<>();
            List<String> forms = new ArrayList<>();
            line = "";
            for (String s : lines)
            {
                Matcher match = numMatcher.matcher(s);
                if (!match.find())
                {
                    if (!line.isEmpty())
                    {
                        evs.add(line.trim());
                    }
                    forms.add(s);
                    line = "";
                }
                else
                {
                    line = line + " " + s;
                }
            }
            if (!line.isEmpty()) evs.add(line.trim());
            if (evs.size() != forms.size())
            {
                forms.add(0, "Normal");
            }
            System.out.println(evs);
            System.out.println(forms);
        }
    }

    /** This is the result of parsing the first row of the table, the one
     * titled: Picture Name Other Names
     * 
     * @param rows
     * @param num
     * @param val
     * @param mega
     * @return */
    private static PokedexEntry parseInitialRowsAndCreateEntry(Elements rows, int num, int val, boolean alolan,
            String mega)
    {
        PokedexEntry entry = null;
        // Name, gender, types, capture rate, mass, egg time,
        // size
        Elements values = rows.get(val).select("td");
        String name = values.get(3).text();

        // This is used to replace symbols with M and F in names
        // of nidorans.
        char[] genders = new char[2];
        String male = "";
        String female = "";

        // Find genders, this is needed before name for the
        // symbol.
        for (int i = 0; i < values.size(); i++)
        {
            Element e = values.get(i);
            if (e.text().contains("Male") && !e.text().contains("Female"))
            {
                male = values.get(i + 1).text().replace("%", "");
                genders[0] = e.text().charAt(5);
            }
            if (e.text().contains("Female") && !e.text().contains("Male"))
            {
                genders[1] = e.text().charAt(7);
                female = values.get(i + 1).text().replace("%", "");
            }
        }
        name = name.replace(genders[0], 'M');
        name = name.replace(genders[1], 'F');
        if (alolan) name = name + " Alola";
        // if (mega != null)
        // {
        // name = name + " " + mega;
        // }
        entry = new PokedexEntry(name, num);
        // Set genders
        if (!male.isEmpty())
        {
            float f = Float.parseFloat(female);
            entry.entry.stats.genderRatio = (int) (f * 254 / 100);
        }
        int index = values.size() - 1;//
        String test = values.get(index - 1).text();
        if (!test.contains("%") && !test.contains("Genderless"))
        {
            index -= 2;
        }
        // Set Types
        Element e = values.get(alolan ? index + 2 : index);
        Elements types = e.select("a");
        String type1 = types.get(0).attr("href");
        String type2 = types.size() < 2 ? null : types.get(1).attr("href");

        entry.setType(0, type1);
        entry.setType(1, type2);
        values = rows.get(rows.size() - 1).select("td");

        // Height
        if (entry.entry.stats.sizes != null && entry.entry.stats.sizes.values.isEmpty())
        {
            String[] vals = values.get(1).text().split(" ");
            String size = null;
            for (String s : vals)
            {
                if (s.contains("m"))
                {
                    size = s.replace("m", "").trim();
                }
            }
            if (size != null)
            {
                entry.entry.stats.sizes.values.put(new QName("height"), size);
                entry.entry.stats.sizes.values.put(new QName("width"), size);
                entry.entry.stats.sizes.values.put(new QName("length"), size);
            }
        }

        // Mass
        String[] vals = values.get(2).text().split(" ");
        String mass = null;
        for (String s : vals)
        {
            if (s.contains("kg"))
            {
                mass = s.replace("kg", "").trim();
            }
        }
        if (mass != null)
        {
            entry.entry.stats.mass = Float.parseFloat(mass);
        }

        // Capture rate.
        String captureRate = values.get(3).text();
        try
        {
            entry.entry.stats.captureRate = Integer.parseInt(captureRate);
        }
        catch (NumberFormatException e1)
        {
            Pattern matcher = Pattern.compile("[0-9].");
            Matcher match = matcher.matcher(captureRate);
            if (match.find())
            {
                entry.entry.stats.captureRate = Integer.parseInt(match.group().trim());
            }
            else
            {
                System.err.println("error with capture rate for " + entry.entry.name);
                entry.entry.stats.captureRate = 3;
            }
        }
        return entry;
    }

    static Element findValue(int row, int column, Elements table)
    {
        Elements r = table.get(row).select("td");
        return r.get(column);
    }
}
