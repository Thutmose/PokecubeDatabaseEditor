package thut.pokecubedatabase.serebii;

import java.io.IOException;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class SerebiiChecker
{
    public static final int TOTALCOUNT = 721;

    static HashSet<String> movesets = new HashSet<>();

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

    public SerebiiChecker()
    {
    }

    public void updateFromSerebii(int num)
    {
        try
        {
            parseForNumber(num);
        }
        catch (Exception e)
        {
            System.out.println(num + " Failed");
            e.printStackTrace();
        }
    }

    void parseForNumber(int num) throws IOException
    {
        String numStr = "" + num;
        if (numStr.length() == 1) numStr = "00" + numStr;
        else if (numStr.length() == 2) numStr = "0" + numStr;
        // Make a URL to the web page
        String html = "http://www.serebii.net/pokedex-xy/" + numStr + ".shtml";

        Document doc = Jsoup.connect(html).get();
        doc.outputSettings().escapeMode(EscapeMode.xhtml);
        Elements tableElements = doc.select("table");
        Elements tableRowElements = tableElements.select(":not(thead) tr");

        char[] genders = getGenderSymbols(tableRowElements);

        String name = "";

        Element row = tableRowElements.get(3);
        Elements rowItems = row.select("td");
        int index = 30 - getGen(num);

        for (int j = index; j < rowItems.size(); j++)
        {
            Element item = rowItems.get(j - 5);
            String text = item.text().replace(genders[0], 'M').replace(genders[1], 'F');
            if (name.isEmpty() && text.contains("Gender Ratio"))
            {
                name = rowItems.get(j).text().replace(genders[0], 'M').replace(genders[1], 'F');
            }
        }
        if (name == null) return;

        PokedexEntry entry = new PokedexEntry(name, num);
        boolean gender = false;
        boolean stats = false;
        boolean lvlup = false;
        boolean type = false;
        for (int i = 3; i < tableRowElements.size(); i++)
        {
            row = tableRowElements.get(i);
            rowItems = row.select("td");

            for (int j = 0; j < rowItems.size(); j++)
            {
                Element item = rowItems.get(j);
                String text = item.text().replace(genders[0], 'M').replace(genders[1], 'F');

                if (text.equals("Type") && !type)
                {
                    boolean classification = false;
                    int k = 1;
                    while (!classification)
                    {
                        k++;
                        String input = rowItems.get(j + k).toString();
                        classification = rowItems.get(j + k).text().equals("Classification");
                        if (isTypeField(input))
                        {
                            int n = 0;
                            for (Node node : rowItems.get(j + k).childNodes())
                            {
                                if (node.childNodeSize() > 0)
                                {
                                    String typeName = node.childNode(0).attr("src").replace(".gif", "")
                                            .replace("/pokedex-bw/type/", "");
                                    try
                                    {
                                        typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
                                        entry.types[n++] = typeName;
                                    }
                                    catch (Exception e)
                                    {
//                                        System.err.println(entry.name + " " + node.childNode(0).attr("src") + " "
//                                                + node.childNode(0));
                                        System.err.println("Error with types for "+name);
                                    }
                                }
                            }
                        }
                    }

                    type = true;
                }

                if (!gender && isGenderString(text))
                {
                    entry.parseGenderAndInfo(text);
                    gender = true;
                }
                if (gender && !stats && isBaseStats(text))
                {
                    stats = true;
                    for (int k = 1; k <= 6; k++)
                    {
                        Element stat = rowItems.get(k + j);
                        entry.baseStats[k - 1] = Integer.parseInt(stat.text().trim());
                    }
                }
                if (gender && !lvlup && text.length() < 40 && text.contains("Level Up") && !text.contains("X & Y")
                        && !text.contains("Anchors"))
                {
                    lvlup = true;
                    int indexIn = j + 1;
                    indexIn = parseLevelMoves(indexIn, rowItems, entry);
                    Element move = rowItems.get(indexIn);
                    parseMoves(move.text(), indexIn, rowItems, entry);
                }
            }
        }
        entry.editDatabase();
    }

    private boolean isTypeField(String input)
    {
        return input.contains("/type/");
    }

    private void parseMoves(String header, int indexIn, Elements rowItems, PokedexEntry entry)
    {
        int newIndex = indexIn;
        if (header.contains("TM & HM Attacks"))
        {
            newIndex = parseTMMoves(indexIn, rowItems, entry);
        }
        else if (header.contains("Pre-Evolution Only Moves"))
        {
            newIndex = parsePreEvoMoves(indexIn, rowItems, entry);
        }
        else if (header.contains("Egg Moves"))
        {
            newIndex = parseEggMoves(indexIn, rowItems, entry);
        }
        else if (header.contains("Move Tutor"))
        {
            newIndex = parseMoveTutor(indexIn, rowItems, entry);
        }
        else if (header.contains("Transfer Only"))
        {
            newIndex = parseTransferMoves(indexIn, rowItems, entry);
        }
        else if (header.contains("Special Moves"))
        {
            newIndex = parseSpecialMoves(indexIn, rowItems, entry);
        }
        if (indexIn != newIndex)
        {
            header = rowItems.get(newIndex).text();
            parseMoves(header, newIndex, rowItems, entry);
        }
    }

    private int parseTMMoves(int indexIn, Elements rowItems, PokedexEntry entry)
    {
        Element move;
        boolean tms = false;
        int index = indexIn + 2;
        while (true)
        {
            move = rowItems.get(index);
            String attack = move.text();
            String info = rowItems.get(index - 1).text();
            if (!(info.contains("TM") || info.contains("HM"))) break;
            entry.otherMoves.add(attack);
            index += 9;
            tms = true;
        }
        move = rowItems.get(index - 1);
        index = index - 1;
        if (tms) indexIn = index;
        return indexIn;
    }

    private int parseEggMoves(int indexIn, Elements rowItems, PokedexEntry entry)
    {
        Element move;
        boolean valid = false;
        int index = indexIn + 1;
        while (true)
        {
            move = rowItems.get(index);
            if (isMoveSection(rowItems.get(index + 1).text())) break;
            if (isMoveSection(rowItems.get(index).text())) break;
            valid = true;
            String attack = move.text();
            entry.otherMoves.add(attack);
            index += 9;
        }
        if (valid) indexIn = index;
        return indexIn;
    }

    private int parseSpecialMoves(int indexIn, Elements rowItems, PokedexEntry entry)
    {
        Element move;
        boolean valid = false;
        int index = indexIn + 1;
        while (true)
        {
            move = rowItems.get(index);
            if (isMoveSection(rowItems.get(index + 1).text()) || isMoveSection(move.text())) break;
            valid = true;
            String attack = move.text();
            entry.otherMoves.add(attack);
            index += 9;
        }
        if (valid) indexIn = index;
        return indexIn;
    }

    private int parseMoveTutor(int indexIn, Elements rowItems, PokedexEntry entry)
    {
        Element move;
        boolean valid = false;
        int index = indexIn + 1;
        while (true)
        {
            if (rowItems.size() >= index) break;
            move = rowItems.get(index);
            if (isMoveSection(rowItems.get(index + 1).text()) || isMoveSection(rowItems.get(index).text())) break;
            valid = true;
            String attack = move.text();
            entry.otherMoves.add(attack);
            index += 8;
        }
        if (valid) indexIn = index;
        return indexIn;
    }

    private int parseTransferMoves(int indexIn, Elements rowItems, PokedexEntry entry)
    {
        Element move;
        boolean valid = false;
        int index = indexIn + 1;
        boolean cont = true;
        while (cont)
        {
            move = rowItems.get(index);
            String attack = move.text();
            if (isMoveSection(attack)) break;
            cont = !isMoveSection(rowItems.get(index + 10).text());
            entry.otherMoves.add(attack);
            valid = true;
            index += 9;
        }
        if (valid) indexIn = index;
        return indexIn;
    }

    private int parsePreEvoMoves(int indexIn, Elements rowItems, PokedexEntry entry)
    {
        Element move;
        boolean valid = false;
        int index = indexIn + 1;
        boolean cont = true;
        while (cont)
        {
            move = rowItems.get(index);
            String attack = move.text();
            if (isMoveSection(attack)) break;
            cont = !isMoveSection(rowItems.get(index + 12).text());
            entry.otherMoves.add(attack);
            valid = true;
            index += 11;
        }
        if (valid) indexIn = index;
        return indexIn;
    }

    private int parseLevelMoves(int indexIn, Elements rowItems, PokedexEntry entry)
    {
        Element move;
        move = rowItems.get(indexIn);
        while (true)
        {
            move = rowItems.get(indexIn);
            String level = move.text();
            if (level.length() > 3) break;

            int l = 1;
            try
            {
                l = Integer.parseInt(level);
            }
            catch (NumberFormatException e)
            {
            }
            entry.lvlMoves.add(l + ":" + rowItems.get(indexIn + 1).text());
            indexIn += 9;
        }
        return indexIn;
    }

    public static boolean isMoveSection(String input)
    {
        for (String s : movesets)
        {
            if (input.contains(s)) return true;
        }
        return false;
    }

    public static int getGen(int pokedexNb)
    {
        if (pokedexNb < 152) return 1;
        if (pokedexNb < 252) return 2;
        if (pokedexNb < 387) return 3;
        if (pokedexNb < 494) return 4;
        if (pokedexNb < 650) return 5;
        if (pokedexNb < 722) return 6;
        return 0;
    }

    boolean isGenderString(String text)
    {
        if (text.contains("Male") && text.contains("Female") && text.contains("%")
                || text.contains("is Genderless")) { return true; }
        return false;
    }

    boolean isBaseStats(String text)
    {
        if (text.length() == 23 && text.trim().contains("Base Stats - Total:")) return true;
        return false;
    }

    // Find the special gender symbols.
    char[] getGenderSymbols(Elements tableRowElements)
    {
        char[] ret = new char[2];
        boolean foundPicture = false;
        for (int i = 0; i < tableRowElements.size(); i++)
        {
            Element row = tableRowElements.get(i);
            Elements rowItems = row.select("td");
            for (int j = 0; j < rowItems.size(); j++)
            {
                Element item = rowItems.get(j);

                String text = item.text();
                if (!foundPicture) foundPicture = text.equals("Picture");
                if (!foundPicture) continue;

                if (text.contains("Male") && text.contains("Female") && text.contains("%"))
                {
                    int index = text.indexOf("Male ") + "Male ".length();
                    ret[0] = text.charAt(index);
                    index = text.indexOf("Female ") + "Female ".length();
                    ret[1] = text.charAt(index);
                    return ret;
                }
                // if (!text.trim().isEmpty()) System.out.println(text);
            }
            // System.out.println();
        }
        return ret;
    }

}
