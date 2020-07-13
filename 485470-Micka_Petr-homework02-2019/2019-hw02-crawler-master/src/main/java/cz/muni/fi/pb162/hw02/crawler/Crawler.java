package cz.muni.fi.pb162.hw02.crawler;

import cz.muni.fi.pb162.hw02.SimpleHttpClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Petr Micka
 */
public class Crawler implements SmartCrawler{
    /**
     *
     * @param page html of page
     * @param urls array to be filled
     * @return rest of page
     */
    private String addNextUrl(String page,ArrayList<String> urls){
        int beginingIndex = page.indexOf("a href=")+"a href=".length()+1;
        if(beginingIndex == -1){
            return null;
        }
        int endIndex = page.indexOf("\"",beginingIndex);
        if(endIndex == -1){
            return null;
        }
        urls.add(page.substring(beginingIndex, endIndex));
        return page.substring(endIndex+1);
    }
    /**
     * Scans a given URL and parses out all links
     *
     * @param url URL to be crawled
     * @return a list of URLs contained by given URL
     */

    @Override
    public List<String> crawl(String url) {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        String page = null;
        try {
            page = simpleHttpClient.get(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> list = new ArrayList<>();
        while (page != null){

            page = addNextUrl(page,list);

        }
        return list;
    }

    /**
     * Similar to {@link #crawl(String)} but follows the links
     *
     * @param url URL to be crawled
     * @return a map of URL and links contained by it
     */
    @Override
    public Map<String, List<String>> crawlAll(String url) {
        Map<String, List<String>> map = new HashMap<String,List<String>>();
        Map<String, List<String>> adition = new HashMap<String,List<String>>();
        map.put(url,crawl(url));
        Boolean changed = true;
        while (changed){
            changed = false;
            for(List<String> urls: map.values()){
                for(String actualUrl :urls){
                    if(map.containsKey(actualUrl) || adition.containsKey(actualUrl)){
                        continue;
                    }
                    adition.put(actualUrl,crawl(actualUrl));
                    changed = true;
                }
            }
            map.putAll(adition);
            if(!adition.isEmpty()){
                adition.clear();
            }
        }
        return map;
    }

    /**
     * Starts crawling at given URL and builds a reverse index (same as {@link #reverseIndex(Map)}
     *
     * @param url URL to be crawled
     * @return a map of urls and their references
     */
    @Override
    public Map<String, List<String>> crawlReverse(String url) {
        return reverseIndex(crawlAll(url));
    }

    /**
     * Builds a reverse index of URLs and links containing a reference to it
     *
     * @param index a map of URL and links contained by it (as returned by {@link  IndexCrawler#crawlAll(String)}
     * @return a map of urls and their references
     */
    @Override
    public Map<String, List<String>> reverseIndex(Map<String, List<String>> index) {
        Map<String,List<String>> reverseIndex = new HashMap<String, List<String>>();
        for(Map.Entry<String ,List<String>> entry : index.entrySet()){
            append(entry.getValue(),entry.getKey(),reverseIndex);
            if(reverseIndex.get(entry.getKey()) == null){
                reverseIndex.put(entry.getKey(),new ArrayList<>());
            }
        }

        return reverseIndex;
    }

    /**
     *
     * @param keys to be added
     * @param value to be added
     * @param reverseIndex map in what we will place it
     */
    private void append(List<String> keys, String value, Map<String, List<String>> reverseIndex) {
        List<String> oldValue;
        for(String key : keys){
            oldValue = reverseIndex.get(key);
            if(oldValue == null){
                oldValue = new ArrayList<>();
            }
            if(!oldValue.contains(value)){
                oldValue.add(value);
            }
            reverseIndex.put(key,oldValue);
        }
    }


}
