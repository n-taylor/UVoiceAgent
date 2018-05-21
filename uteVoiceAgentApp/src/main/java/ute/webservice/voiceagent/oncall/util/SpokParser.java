package ute.webservice.voiceagent.oncall.util;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses certain information retrieved from the Spok On-call system.
 * Created by Nathan Taylor on 5/16/2018.
 */

public class SpokParser {

    private static final String OPEN_TAGS_GET_PAGER_ID = "<?xml version=\"1.0\" encoding=\"utf-8\"?> \n"
            + " <procedureCall name=\"GetPagerId\" xmlns=\"http://xml.amcomsoft.com/api/request\">"
            + "\n <parameter name=\"mid\" null=\"false\">";
    private static final String CLOSE_TAGS_GET_PAGER_ID = "</parameter>   \n</procedureCall>";

    private static final String REGEX_PAGER_ID = "\\[(.+)]\\s*\\[(\\d*)]";

    /**
     * Given an mid, returns the xml formatted call that will retrieve the Pager IDs associated with the mid
     */
    public static String getPagerIdCall(String mid){
        return OPEN_TAGS_GET_PAGER_ID + mid.trim() + CLOSE_TAGS_GET_PAGER_ID;
    }

    public static ArrayList<String> parsePagerIdResponse(InputStream in) throws XmlPullParserException, IOException {
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.nextTag();
            return readPagerIds(parser);
        }
        finally {
            in.close();
        }
    }

    private static ArrayList<String> readPagerIds(XmlPullParser parser) throws XmlPullParserException, IOException {
        String pagerIds = null;
        parser.require(XmlPullParser.START_TAG, null, "success");
        while (parser.next() != XmlPullParser.END_TAG){
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            String attrType = parser.getAttributeValue(null, "name");
            // Look for assignment tags
            if (name.equals("parameter")){
                if (attrType.equalsIgnoreCase("pager_id")){
                    pagerIds = readText(parser);
                }
                else{
                    if (parser.next() != XmlPullParser.END_TAG)
                        parser.next();
                }
            }
            else{
                skip(parser); // skip this tag since it isn't an assignment.
            }
        }

        return extractPagerIds(pagerIds);
    }

    /**
     * Extracts the pager IDs of from a string in the format [pager id 1][display order]|[pager id 2][dis..]|etc
     * @param pagerIds
     * @return an array list of pagers ids appended by 'PAGER' and then their display order
     */
    private static ArrayList<String> extractPagerIds(String pagerIds){
        ArrayList<String> descriptions = new ArrayList<>();
        String[] ids = pagerIds.split("\\|");

        for (String id : ids){
            Pattern pattern = Pattern.compile(REGEX_PAGER_ID);
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()){
                String order = matcher.group(2);
                descriptions.add(matcher.group(1) + " : PAGER " + (descriptions.size() + 1));
            }
        }
        return descriptions;
    }

    /**
     * Reads the text of a tag. Requires that calling parser.next() == XmlPullParser.TEXT
     *
     * @return empty if there is no text associated with parser.next()
     */
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
