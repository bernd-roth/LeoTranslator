package at.co.netconsulting.leotranslater;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by bernd on 18.05.16.
 */
public class HTMLReader {

    public String getTranslation(String html){
        final org.jsoup.nodes.Document parse = Jsoup.parse(html);

        if(parse.body().getElementsByTag("<kbd>").first()!=null)
            return parse.body().getElementsByTag("<kbd>").first().text().toString().substring(1);
        else
            return "Wrong input";
    }
}