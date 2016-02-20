package thut.pokecubedatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MergeHandler implements ActionListener
{

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        try
        {
            Main.instance.info.setText("");
            mergeFiles(new File("./" + Main.instance.doc1.getText()), new File("./" + Main.instance.doc2.getText()),
                    new File("./" + Main.instance.output.getText()));
            Main.instance.info.setText("MERGED DATABASE FILES");
        }
        catch (ParserConfigurationException | SAXException | IOException e)
        {
            Main.instance.info.setText("ERROR:" + e);
        }
    }

    void mergeFiles(File file1, File file2, File output) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc1 = docBuilder.parse(file1);

        docFactory = DocumentBuilderFactory.newInstance();
        docBuilder = docFactory.newDocumentBuilder();
        Document doc2 = docBuilder.parse(file2);

        Element root = doc1.getDocumentElement();

        NodeList entries = doc2.getElementsByTagName("Pokemon");
        NodeList entries2 = doc1.getElementsByTagName("Pokemon");
        outer:
        for (int i = 0; i < entries.getLength(); i++)
        {
            Element pokemonNode = (Element) entries.item(i);
            for (int i1 = 0; i1 < entries2.getLength(); i1++)
            {
                Element pokemonNode2 = (Element) entries2.item(i1);
                if (pokemonNode2.getAttribute("name").equals(pokemonNode.getAttribute("name")))
                {
                    System.out.println("Skipping " + pokemonNode2.getAttribute("name"));
                    continue outer;
                }
            }
            pokemonNode = (Element) pokemonNode.cloneNode(true);
            doc1.adoptNode(pokemonNode);
            root.appendChild(pokemonNode);
        }
        Main.instance.writeXML(doc1, output);
    }

}
