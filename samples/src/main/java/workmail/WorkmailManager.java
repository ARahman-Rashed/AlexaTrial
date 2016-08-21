package workmail;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.availability.CalendarEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ARahman on 8/21/2016.
 */
public class WorkmailManager {

    static final String NO_APPOINTMENTS = "No, you don't have meetings for today";
    static final String YOU_HAVE_APPOINTMENTS = "Yes, you have meetings for today" +
            "Would you like to hear your meetings?";
    static final String TODAY = "today";
    static ExchangeServiceClient client = null;
    ManagerHelper helper = null;

    public WorkmailManager() {
        client = new ExchangeServiceClient();
        helper = new ManagerHelper();
    }

    public SpeechletResponse noAppointmentsResponse() {

        String speechText = NO_APPOINTMENTS;
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(NO_APPOINTMENTS);
        SimpleCard card = new SimpleCard();
        card.setTitle("Your schedule today");
        card.setContent(speechText);
        return SpeechletResponse.newTellResponse(speech, card);
    }

    public SpeechletResponse getTodayCalendarStatusResponse() {

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        String speechText = "";
        SimpleCard card = new SimpleCard();
        boolean busy = helper.dayHasAppointments(TODAY);
        if (!busy) {
            return noAppointmentsResponse();
        } else {
            speech.setText(YOU_HAVE_APPOINTMENTS);
            card.setTitle("Your schedule today");
            card.setContent(speechText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);
            return SpeechletResponse.newAskResponse(speech, reprompt);
        }
    }

    public SpeechletResponse getTodaysAppointmentsResponse() {

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        StringBuilder speechText = new StringBuilder("");
        SimpleCard card = new SimpleCard();
        card.setTitle("Today's calendar");

        List<CalendarEvent> eventsForToday = helper.getAppointmentsForDay(TODAY);
        if (eventsForToday.size() == 0)
            return noAppointmentsResponse();

        if (eventsForToday.size() == 1)
            speechText.append("You have only 1 event Today. \n");

        else speechText.append(String.format("You have: %d events for Today. \n", eventsForToday.size()));

        for (int i = 0; i < eventsForToday.size(); i++) {
            CalendarEvent event = eventsForToday.get(i);
            if (i > 0) {
                if (i < eventsForToday.size() - 1)
                    speechText.append("also, ");
                else speechText.append("finally, ");
            }
            String eventStartTime = String.format("hh:mm", event.getStartTime());
            String eventEndTime = String.format("hh:mm", event.getEndTime());
            String eventSubject = event.getDetails().getSubject();
            speechText.append(String.format("You have %s from %s to %s \n \n", eventSubject, eventStartTime, eventEndTime));
        }
        speech.setText(speechText.toString());
        card.setContent(speechText.toString());
        return SpeechletResponse.newTellResponse(speech, card);
    }

    public SpeechletResponse getBatmanResponse(){
        // TODO: Batman's "I am BATMAN!" response
        return null;
    }

    public SpeechletResponse getEmailsResponse(int n) {

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        StringBuilder speechText = new StringBuilder("");
        SimpleCard card = new SimpleCard();
        card.setContent("Emails:");
        List<EmailMessage> emails = helper.getEmails(n);
        for (EmailMessage email : emails) {
            try {
                speechText.append(String.format("Email subject: %s \n" +
                        "from: %s \n" +
                        "says: %s \n \n", email.getSubject(), email.getSender(), email.getBody()));
            } catch (ServiceLocalException e) {
                e.printStackTrace();
            }
        }
        speech.setText(speechText.toString());
        card.setContent(speechText.toString());
        return SpeechletResponse.newTellResponse(speech, card);
    }

    class ManagerHelper {

        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        public boolean dayHasAppointments(String date) {
            List<CalendarEvent> dayEvents = new ArrayList<>();
            try {
                dayEvents = date.equals(TODAY) ? client.getAppointmentsForDay(formatter.format(new Date())) :
                        client.getAppointmentsForDay(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return dayEvents.size() != 0;
        }

        public List<CalendarEvent> getAppointmentsForDay(String date) {
            date = date.equals(TODAY) ? formatter.format(new Date()) : date;
            List<CalendarEvent> dayEvents = new ArrayList<>();
            if (dayHasAppointments(date)) {
                try {
                    dayEvents = client.getAppointmentsForDay(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return dayEvents;
        }

        public List<EmailMessage> getEmails(int n) {
            List<EmailMessage> emails = new ArrayList<>();
            try {
                for (Item item : client.getEmails(n))
                    emails.add((EmailMessage) item);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return emails;
        }
    }
}
