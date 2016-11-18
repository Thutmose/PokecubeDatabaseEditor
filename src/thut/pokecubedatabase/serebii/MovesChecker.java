package thut.pokecubedatabase.serebii;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.FieldApplicator;
import pokecube.core.database.moves.json.JsonMoves.IValueFixer;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import thut.pokecubedatabase.Main;

public class MovesChecker
{
    public static String                       movedex   = "attackdex-sm";
    public static IValueFixer                  typeFixer = new IValueFixer()
                                                         {
                                                             @Override
                                                             public String fix(String input)
                                                             {
                                                                 String typeName = input
                                                                         .replace("/" + movedex + "/", "")
                                                                         .replace(".shtml", "");
                                                                 typeName = Character.toUpperCase(typeName.charAt(0))
                                                                         + typeName.substring(1);
                                                                 return typeName;
                                                             }
                                                         };

    public static Map<String, FieldApplicator> fieldMap  = new HashMap<>();
    static
    {
        try
        {
            fieldMap.put("Battle Type", new FieldApplicator(MoveJsonEntry.class.getField("type"), typeFixer));
            fieldMap.put("Category", new FieldApplicator(MoveJsonEntry.class.getField("category"), typeFixer));
            fieldMap.put("Power Points", new FieldApplicator(MoveJsonEntry.class.getField("pp")));
            fieldMap.put("Base Power", new FieldApplicator(MoveJsonEntry.class.getField("pwr")));
            fieldMap.put("Accuracy", new FieldApplicator(MoveJsonEntry.class.getField("acc")));
            fieldMap.put("Battle Effect:", new FieldApplicator(MoveJsonEntry.class.getField("battleEffect")));
            fieldMap.put("Secondary Effect:", new FieldApplicator(MoveJsonEntry.class.getField("secondaryEffect")));
            fieldMap.put("Effect Rate:", new FieldApplicator(MoveJsonEntry.class.getField("effectRate")));
            fieldMap.put("Corresponding Z-Move", new FieldApplicator(MoveJsonEntry.class.getField("zMovesTo")));
            fieldMap.put("Z-Move Power", new FieldApplicator(MoveJsonEntry.class.getField("zMovePower")));
            fieldMap.put("TM #", new FieldApplicator(MoveJsonEntry.class.getField("tmNum")));
            fieldMap.put("Speed Priority", new FieldApplicator(MoveJsonEntry.class.getField("speedPriority")));
            fieldMap.put("Pokémon Hit in Battle", new FieldApplicator(MoveJsonEntry.class.getField("target")));
            fieldMap.put("Physical Contact", new FieldApplicator(MoveJsonEntry.class.getField("contact")));
            fieldMap.put("Sound-Type - Details", new FieldApplicator(MoveJsonEntry.class.getField("soundType")));
            fieldMap.put("Punch Move - Details", new FieldApplicator(MoveJsonEntry.class.getField("punchType")));
            fieldMap.put("Snatchable", new FieldApplicator(MoveJsonEntry.class.getField("snatchable")));
            fieldMap.put("Z Move?", new FieldApplicator(MoveJsonEntry.class.getField("zMove")));
            fieldMap.put("In-Depth Effect:", new FieldApplicator(MoveJsonEntry.class.getField("inDepthEffect")));
            fieldMap.put("Z-Version", new FieldApplicator(MoveJsonEntry.class.getField("zVersion")));
            fieldMap.put("Detailed Effect", new FieldApplicator(MoveJsonEntry.class.getField("detailedEffect")));
            fieldMap.put("Defrosts When Used?", new FieldApplicator(MoveJsonEntry.class.getField("defrosts")));
            fieldMap.put("Hits the opposite side in Triple Battles?",
                    new FieldApplicator(MoveJsonEntry.class.getField("wideArea")));
            fieldMap.put("Reflected By Magic Coat/Magic Bounce?",
                    new FieldApplicator(MoveJsonEntry.class.getField("magiccoat")));
            fieldMap.put("Blocked by Protect/Detect?", new FieldApplicator(MoveJsonEntry.class.getField("protect")));
            fieldMap.put("Copyable by Mirror Move?", new FieldApplicator(MoveJsonEntry.class.getField("mirrormove")));
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            e.printStackTrace();
        }
    }

    public MovesChecker()
    {
    }

    public void checkAttack(String attack, int n, boolean fromhere) throws IOException
    { // Make a URL to the web page
        attack = convertMoveName(attack);
        String serebii = "http://www.serebii.net";

        String html = serebii + "/" + movedex + "/" + attack + ".shtml";
        Document doc = Jsoup.connect(html).get();
        doc.outputSettings().escapeMode(EscapeMode.xhtml);
        Elements options = doc.select("SELECT > OPTION");

        Map<String, List<String>> erroredSet = new HashMap<>();
        List<String> miscErrors = new ArrayList<String>();
        Main.instance.clearStatus();
        boolean found = !fromhere;
        for (Element element : options)
        {
            String attr = element.attr("value");
            if (attr.isEmpty()) continue;
            if (!found)
            {
                String name = convertMoveName(element.text());
                found = name.equals(attack);
            }
            if (!found) continue;
            MoveEntry move = new MoveEntry(element.text());
            move.entry.readableName = element.text();
            Main.instance.addToStatus("  Updating " + element.text());
            String html2 = serebii + attr;
            Document moveDoc;
            try
            {
                moveDoc = Jsoup.connect(html2).get();
            }
            catch (Exception e1)
            {
                miscErrors.add(move.entry.readableName);
                System.err.println("    Error with " + move.entry.readableName);
                continue;
            }
            moveDoc.outputSettings().escapeMode(EscapeMode.xhtml);
            Elements tables = moveDoc.select("table");
            for (Element table : tables)
            {
                String attr2 = table.attr("class");
                if (attr2.equals("dextable"))
                {
                    Elements rows = table.select("tr");
                    for (int i = 0; i + 1 < rows.size(); i += 2)
                    {
                        Elements headers = rows.get(i).select("td");
                        Elements values = rows.get(i + 1).select("td");
                        if (headers.isEmpty()) continue;
                        if (headers.size() == values.size())
                        {
                            for (int i1 = 0; i1 < values.size(); i1++)
                            {
                                String key = headers.get(i1).text();
                                if (key.equals("Z-" + element.text()))
                                {
                                    key = "Z-Version";
                                }
                                String val = values.get(i1).text();
                                if (val.trim().isEmpty())
                                {
                                    try
                                    {
                                        val = values.get(i1).children().get(0).attr("href");
                                    }
                                    catch (Exception e)
                                    {
                                        val = "???";
                                        System.out.println("    Error with " + key);
                                        List<String> errors = erroredSet.get(element.text());
                                        if (errors == null) erroredSet.put(element.text(), errors = new ArrayList<>());
                                        errors.add(key);
                                    }
                                }
                                if (fieldMap.containsKey(key))
                                {
                                    fieldMap.get(key).apply(val, move.entry);
                                }
                            }
                        }
                    }
                }
            }
            JsonMoves.write(Main.movesFile);
            if (n-- <= 0) break;
        }
        if (!erroredSet.isEmpty()) Main.instance.addToStatus("Problem sets: " + erroredSet);
        if (!miscErrors.isEmpty()) Main.instance.addToStatus("Problem moves: " + miscErrors);
        System.out.println("done");
    }

    public static String convertMoveName(String moveNameFromBulbapedia)
    {
        String ret = "";
        String name = moveNameFromBulbapedia.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "");
        String[] args = name.split(" ");
        for (int i = 0; i < args.length; i++)
        {
            ret += args[i];
        }
        return ret;
    }

}
