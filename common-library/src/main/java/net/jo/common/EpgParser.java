package net.jo.common;

import net.jo.model.LiveChannelGroup;
import net.jo.model.LiveChannelItem;
import net.jo.model.LiveEpgGroup;
import net.jo.model.LiveEpgItem;
import net.jo.model.LiveSourceSite;
import net.jo.model.Tv;
import net.jo.http.HttpResult;
import net.jo.http.HttpSimpleUtils;

import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EpgParser {

    public static boolean start(LiveSourceSite live) throws Exception {
        if (!live.getEpg().endsWith(".xml") && !live.getEpg().endsWith(".gz")) {
            return false;
        }
        try {
            HttpSimpleUtils hsu = new HttpSimpleUtils();
            int try_time = 0;
            HttpResult result = null;
            while(result == null || !result.isSign()){
                try_time++;
                if(try_time > 3) return false;
                result = hsu.doGetBody(live.getEpg(), null, null, "UTF-8");
                if(result == null || !result.isSign())Utils.sleep(5000);
            }

            String epg_data = null;
            if (live.getEpg().endsWith(".gz")) {
                epg_data = Utils.uncompressToString(result.getBytesResult(), "UTF-8");
            } else {
                epg_data = new String(result.getBytesResult(), "UTF-8");
            }
            readXml(live , epg_data);
            return true;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean isToday(Date date) {
        return isToday(date.getTime());
    }

    private static boolean isToday(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    //Live - LiveSourceSite
    //Group - LiveChannelGroup
    //Channel - LiveChanelItem
    //Epg - LiveEpgGroup
    //EpgData - LiveEpgItem
    private static void readXml(LiveSourceSite live, String epg_data) throws Exception {
        Set<String> exist = new HashSet<>();
        Map<String, LiveEpgGroup> epgMap = new HashMap<>();
        Map<String, String> mapping = new HashMap<>();
        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatFull = new SimpleDateFormat("yyyyMMddHHmmss Z", Locale.getDefault());
        String today = formatDate.format(new Date());
        Tv tv = new Persister().read(Tv.class, epg_data, false);

        for (LiveChannelGroup group : live.getLiveChannelGroups()) {
            for (LiveChannelItem channel : group.getChannel()) {
                exist.add(channel.getName());
            }
        }

        for (Tv.Channel channel : tv.getChannel()) {
            mapping.put(channel.getId(), channel.getDisplayName());
        }
        for (Tv.Programme programme : tv.getProgramme()) {
            String key = mapping.get(programme.getChannel());
            Date startDate = formatFull.parse(programme.getStart());
            Date endDate = formatFull.parse(programme.getStop());
            if (!exist.contains(key)) continue;
            if (!isToday(startDate) && !isToday(endDate)) continue;
            if (!epgMap.containsKey(key)) {
                epgMap.put(key,LiveEpgGroup.create(key, today));
            }
            LiveEpgItem epgData = new LiveEpgItem();
            epgData.setTitle(Trans.s2t(programme.getTitle()));
            epgData.setStart(formatTime.format(startDate));
            epgData.setEnd(formatTime.format(endDate));
            epgData.setStartTime(startDate.getTime());
            epgData.setEndTime(endDate.getTime());
            epgMap.get(key).getList().add(epgData);
        }

        for (LiveChannelGroup group : live.getLiveChannelGroups()) {
            for (LiveChannelItem channel : group.getChannel()) {
                channel.setData(epgMap.get(channel.getTvgName()));
            }
        }
    }
}