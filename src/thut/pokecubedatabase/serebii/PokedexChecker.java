package thut.pokecubedatabase.serebii;

import java.io.IOException;
import java.util.HashSet;

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

    static HashSet<String> movesets = new HashSet<>();
    public static String   pokedex  = "pokedex-sm";   // pokedex-xy
    public static String   typeLoc  = "pokedex-bw";

    static
    {
        movesets.add("Special Moves");
        movesets.add("Move Tutor");
        movesets.add("Egg Moves");
        movesets.add("TM & HM Attacks");
        movesets.add("Pre-Evolution Only Moves");
        movesets.add("Transfer Only Moves");
        movesets.add("Stats");
    }

    public PokedexChecker()
    {
    }

    void parseForName(String name) throws IOException
    {
        XMLPokedexEntry old = XMLEntries.getDatabase(Main.pokedexfile).getEntry(name, -1, true, -1);
        if (old == null) throw new IOException("Entry does not exist for " + name);
        PokedexEntry entry = new PokedexEntry(old);
        String numStr = entry.entry.number;
        if (numStr.length() == 1) numStr = "00" + numStr;
        else if (numStr.length() == 2) numStr = "0" + numStr;
        // Make a URL to the web page
        String html = "http://www.serebii.net/" + pokedex + "/" + numStr + ".shtml";
        Document doc = Jsoup.connect(html).get();
        doc.outputSettings().escapeMode(EscapeMode.xhtml);
        parseEntry(entry, doc);
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
        PokedexEntry entry = createEntry(doc, num);
        parseEntry(entry, doc);
    }

    private PokedexEntry createEntry(Document doc, int num)
    {
        PokedexEntry entry = null;
        Elements tables = doc.select("table");
        for (Element table : tables)
        {
            String attr2 = table.attr("class");
            if (attr2.equals("dextable"))
            {
                Elements rows = table.select("tr");
                String firstLine = rows.get(0).select("td").text();

                if (firstLine.contains("Mega Evolution"))
                {
                    break;
                }

                if (firstLine.contains("Picture Name Other Names"))
                {
                    if (entry != null)
                    {
                        System.out.println("duplicate? " + entry.entry.name);
                        break;
                    }
                    entry = parseInitialRowsAndCreateEntry(rows, num);
                    if (entry.entry.moves.misc != null) entry.entry.moves.misc.moves = null;
                    break;
                }
            }
        }
        return entry;
    }

    private void parseEntry(PokedexEntry entry, Document doc)
    {
        Elements tables = doc.select("table");
        MovesJson validMoves = JsonMoves.getMoves(Main.movesFile);
        for (Element table : tables)
        {
            String attr2 = table.attr("class");
            if (attr2.equals("dextable"))
            {
                Elements rows = table.select("tr");
                String firstLine = rows.get(0).select("td").text();
                if (entry == null) System.out.println("null?");
                boolean alola = entry.entry.name.contains("Alola");

                if (firstLine.contains("Abilities:"))
                {
                    parseAbilitiesHappinessEVs(rows, entry);
                }

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
                        int level = 1;
                        try
                        {
                            level = Integer.parseInt(values.get(0).text());
                        }
                        catch (NumberFormatException e)
                        {
                        }
                        String move = values.get(1).text();
                        entry.addLvlMove(level, move);
                    }
                }
                if (firstLine.contains("TM & HM Attacks") || firstLine.contains("Egg Moves")
                        || firstLine.contains("Move Tutor") || firstLine.contains("Special Moves")
                        || firstLine.contains("Pre-Evolution Only Moves") || firstLine.contains("Transfer Only Moves"))
                {
                    // TM moves
                    // TODO make this also check which forme before adding.
                    Elements headers = rows.get(1).select("th");
                    boolean formeInfo = headers.get(headers.size() - 2).text().equals("Effect %")
                            && !firstLine.contains("Transfer Only Moves");
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
                if (firstLine.contains("Usable Z Moves"))
                {
                    // Z Moves
                }

                if (firstLine.equals("Stats"))
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
        entry.entry.stats.baseFriendship = values.get(1).text().trim();

        // EVs
        // This means it doesn't have other formes listed.
        if (values.get(2).select("b").size() == 0)
        {
            entry.parseEVs(values.get(2).text());
        }
        else
        {
            // TODO make this allow selecting which forme to pick
            // from the set.
            String forme1 = values.get(2).select("b").get(1).text();
            String forme = values.get(2).select("b").get(0).text();
            String val = values.get(2).text().replace(forme, "").trim();
            entry.parseEVs(val.substring(0, val.indexOf(forme1)));
        }
    }

    /** This is the result of parsing the first row of the table, the one
     * titled: Picture Name Other Names
     * 
     * @param rows
     * @param num
     * @return */
    private static PokedexEntry parseInitialRowsAndCreateEntry(Elements rows, int num)
    {
        PokedexEntry entry = null;
        // Name, gender, types, capture rate, mass, egg time,
        // size
        Elements values = rows.get(1).select("td");
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
        entry = new PokedexEntry(name, num);
        // TODO output this to many, many lang files at once.
        System.out.println("pkmn." + name + ".name=" + name);

        // Set genders
        if (!male.isEmpty())
        {
            float f = Float.parseFloat(female);
            entry.entry.stats.genderRatio = (int) (f * 254 / 100) + "";
        }

        // Set Types
        int index = values.size() - 1;//
        String test = values.get(index - 1).text();
        if (!test.contains("%") && !test.contains("Genderless"))
        {
            index -= 2;
        }
        Element e = values.get(index);
        Elements types = e.select("a");
        String type1 = types.get(0).attr("href");
        String type2 = types.size() < 2 ? null : types.get(1).attr("href");
        entry.setType(0, type1);
        entry.setType(1, type2);
        values = rows.get(rows.size() - 1).select("td");

        // Height
        if (entry.entry.stats.sizes.values.isEmpty())
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
            entry.entry.stats.mass = mass;
        }

        // Capture rate.
        String captureRate = values.get(3).text();
        entry.entry.stats.captureRate = captureRate;
        return entry;
    }
}
