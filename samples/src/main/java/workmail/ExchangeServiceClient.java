package workmail;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.availability.AvailabilityData;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.response.AttendeeAvailability;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.availability.AttendeeInfo;
import microsoft.exchange.webservices.data.misc.availability.GetUserAvailabilityResults;
import microsoft.exchange.webservices.data.misc.availability.TimeWindow;
import microsoft.exchange.webservices.data.property.complex.availability.CalendarEvent;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ARahman on 8/21/2016.
 */
public class ExchangeServiceClient {

    static final String EMAIL = "hals_faks@yahoo.com";
    static final String PASSWORD = "mohammed";
    static ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2007_SP1);
    static ExchangeCredentials credentials = new WebCredentials(EMAIL, PASSWORD);
    static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Folder inbox = null;

    public ExchangeServiceClient(){
        try {
            service.setCredentials(credentials);
            service.autodiscoverUrl(EMAIL);
            Folder.bind(service, WellKnownFolderName.Inbox);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<CalendarEvent> getAppointmentsForDay(String dateString) throws Exception {
        Date start = formatter.parse(dateString+" 08:00:00");
        Date end = formatter.parse(Util.plusOneDay(dateString)+" 09:00:00");
        GetUserAvailabilityResults freeBusyInfo = service.getUserAvailability(
                new ArrayList<AttendeeInfo>() {
                    {
                        add(new AttendeeInfo(EMAIL));
                    }
                },
                new TimeWindow(start, end),
                AvailabilityData.FreeBusy);
        List<CalendarEvent> events  = new ArrayList<>();
        for(AttendeeAvailability attendeeAvailability: freeBusyInfo.getAttendeesAvailability())
            events.addAll(attendeeAvailability.getCalendarEvents());
        return events;
    }

    public List<Item> getEmails(int n) throws Exception{
        ItemView view = new ItemView(n);
        FindItemsResults<Item> findResults = service.findItems(inbox.getId(), view);
        service.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
        return findResults.getItems();
    }


    static class Util{
         static String plusOneDay(String dateString) {
            String[] date = dateString.split("-");
            StringBuilder plusOne = new StringBuilder("");
            for (int i = 0; i < 2; i++)
                plusOne.append(date[i] + "-");
            plusOne.append(Integer.parseInt(date[2]) + 1);
            return plusOne.toString();
        }

    }
}
