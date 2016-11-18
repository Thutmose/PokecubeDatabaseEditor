package thut.pokecubedatabase.serebii;

import java.io.IOException;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

import thut.pokecubedatabase.Main;

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

    void parseForNumber(int num) throws IOException
    {
        String numStr = "" + num;
        if (numStr.length() == 1) numStr = "00" + numStr;
        else if (numStr.length() == 2) numStr = "0" + numStr;
        // Make a URL to the web page
        String html = "http://www.serebii.net/" + pokedex + "/" + numStr + ".shtml";
        Document doc = Jsoup.connect(html).get();
        doc.outputSettings().escapeMode(EscapeMode.xhtml);
        Elements tables = doc.select("table");
        PokedexEntry entry = null;
        for (Element table : tables)
        {
            String attr2 = table.attr("class");
            if (attr2.equals("dextable"))
            {
                Elements rows = table.select("tr");
                String firstLine = rows.get(0).select("td").text();

                if (firstLine.contains("Mega Evolution"))
                {
                    // TODO make new thing for megas here.
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
                }

                if (entry == null) System.out.println("null?");

                if (firstLine.contains("Abilities:"))
                {
                    parseAbilitiesHappinessEVs(rows, entry);
                }

                boolean lvlup = firstLine.contains("Level Up");
                if (lvlup)
                {
                    if (firstLine.contains("Alola") && !(entry.entry.name.contains("Alolan"))) lvlup = false;
                    else if (entry.entry.name.contains("Alolan") && !firstLine.contains("Alola")) lvlup = false;
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
                if (firstLine.contains("TM & HM Attacks"))
                {
                    // TM moves
                    // TODO make this also check which forme before adding.
                    Elements headers = rows.get(1).select("th");
                    int index = 2;
                    if (headers.last().text().equals("Form")) index = 3;
                    for (int i = 2; i + index - 1 < rows.size(); i += index)
                    {
                        Elements values = rows.get(i).select("td");
                        String move = values.get(1).text();
                        boolean valid = true;
                        if (index == 3)
                        {
                            valid = false;
                            Elements formes = values.get(8).select("img");
                            boolean normal = !entry.entry.name.contains("Alolan");
                            for (Element e : formes)
                            {
                                if (e.text().contains("Alola") && !normal)
                                {
                                    valid = true;
                                    break;
                                }
                                if (!e.text().contains("Alola") && normal)
                                {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                        if (valid) entry.addOtherMove(move);
                    }
                }
                if (firstLine.contains("Egg Moves") || firstLine.contains("Move Tutor")
                        || firstLine.contains("Special Moves"))
                {
                    // Egg moves
                    // TODO make this also check which forme before adding.
                    Elements headers = rows.get(1).select("th");
                    int index = 2;
                    if (headers.last().text().equals("Form")) index = 3;
                    for (int i = index; i + 1 < rows.size(); i += 2)
                    {
                        Elements values = rows.get(i).select("td");
                        String move = values.get(0).text();
                        boolean valid = false;
                        Elements formes = values.select("img");
                        int n = 0;
                        boolean normal = !entry.entry.name.contains("Alolan");
                        for (Element e : formes)
                        {
                            if (!e.attr("src").contains("icon")) continue;
                            n++;
                            String name = e.attr("title");
                            if (name.contains("Alola") && !normal)
                            {
                                valid = true;
                                break;
                            }
                            if (!name.contains("Alola") && normal)
                            {
                                valid = true;
                                break;
                            }
                        }
                        if (n == 0) valid = true;
                        if (valid) entry.addOtherMove(move);
                    }
                }
                if (firstLine.contains("Usable Z Moves"))
                {
                    // Z Moves
                }
                if (firstLine.contains("Transfer Only Moves"))
                {
                    // th is more than 0 means there is another header there, so
                    // we need to increment down 1
                    int startIndex = rows.get(2).select("th").size() == 0 ? 2 : 3;
                    // Other move tutor or tm moves.
                    for (int i = startIndex; i + 1 < rows.size(); i += 2)
                    {
                        Elements values = rows.get(i).select("td");
                        String move = values.get(0).text();
                        entry.addOtherMove(move);
                    }
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
        Main.instance.status.setText("Updated " + entry.entry.name);

    }

    /** Parse through and fill in the abilities, exp mode, happiness, evs, and
     * maybe the boolean of has mega evolution later.
     * 
     * @param rows
     * @param entry */
    public static void parseAbilitiesHappinessEVs(Elements rows, PokedexEntry entry)
    {
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

        values = rows.get(3).select("td");

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
        System.out.println("Starting " + name);

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
