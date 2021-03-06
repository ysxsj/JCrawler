package com.dict.crawl;

import cn.edu.hfut.dmic.webcollector.model.Page;
import com.dict.souplang.SoupLang;
import com.dict.util.RSSReaderHelper;
import com.rometools.rome.feed.synd.SyndEntry;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pojava.datetime.DateTime;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuhl on 15-8-17.
 */
@CommonsLog
public class CNNRSSExtractor extends BaseExtractor {

    public CNNRSSExtractor(Page page) {
        super(page);
    }

    public CNNRSSExtractor(String url) {
        super(url);
    }


    public boolean init() {
        log.debug("*****init*****");
        try {
            SoupLang soupLang = new SoupLang(SoupLang.class.getClassLoader().getResourceAsStream("CNNRule.xml"));
            context = soupLang.extract(doc);
            content = (Element) context.output.get("content");
            log.info("*****init  success*****");
            return true;
        } catch (Exception e) {
            log.error("*****init  failed***** url:" + url);
            return false;
        }
    }

    public boolean extractorTitle() {
        log.debug("*****extractorTitle*****");
        String title = (String) context.output.get("title");
        if (title == null || "".equals(title.trim())) {
            log.error("*****extractorTitle  failed***** url:" + url);
            return false;
        }
        title = title.replaceAll("\\\\s*|\\t|\\r|\\n", "");//去除换行符制表符/r,/n,/t
        if (title.contains("-"))
            p.setTitle(title.substring(0, title.lastIndexOf("-")).trim());
        else
            p.setTitle(title.trim());
        log.debug("*****extractorTitle  success*****");
        return true;
    }

    public boolean extractorType() {

        String type = RSSReaderHelper.getType(url);
        if(type != null && !type.equals("")) {
            p.setType(type);
        }else{
            log.error("cant get type, false");
            return false;
//            return true;
        }

        Element labelElement = (Element) context.output.get("label");
        String label = "";
        if (labelElement != null) {
            label = labelElement.attr("content");
        }
        p.setLabel(label);
        log.debug("*****extractorTitle  success*****");
        return true;

    }

    public boolean extractorTime() {
        log.debug("*****extractorTime*****");
        SyndEntry entry = RSSReaderHelper.getSyndEntry(url);
        if(entry != null && entry.getPublishedDate() != null){
            p.setTime(new Timestamp(entry.getPublishedDate().getTime()).toString());
            return true;
        }

        Element elementTime = (Element) context.output.get("time");
        if (elementTime == null) {
            log.error("extract time null, return false");
            return false;
        }
        String time = elementTime.attr("content");
        if (time == null || "".equals(time.trim())) {
            log.error("*****extractorTime  failed***** url:" + url);
            return false;
        }
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date date = format.parse(time);
            if (System.currentTimeMillis() - date.getTime() > 7 * 24 * 60 * 60 * 1000) {
                log.error("out of date, return false");
                return false;
            }
            p.setTime(new Timestamp(date.getTime()).toString());
            log.debug("*****extractorTime  success*****");
        } catch (Exception e) {
            DateTime dt = new DateTime(time);
            p.setTime(dt.toString());
            log.debug("*****extractorTime  success*****");
            return true;
//            log.info("*****extractorTime  failed***** url:" + url);
        }
        return true;
    }


    public boolean extractorDescription() {
        log.debug("*****extractor Desc*****");
        Element elementTime = (Element) context.output.get("description");
        if (elementTime == null){
            log.error("can't extract desc, continue");
            return true;
        }
        String description = elementTime.attr("content");
        if (description == null || "".equals(description.trim())) {
            log.info("*****extractor Desc  failed***** url:" + url);
            return true;
        }
        description = StringEscapeUtils.unescapeHtml(description);

        p.setDescription(description);

        return true;
    }
//    public boolean extractorAndUploadImg() {
//        return extractorAndUploadImg(" ", "7890");
//    }


}
