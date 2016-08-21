package workmail;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ARahman on 8/21/2016.
 */
public class WorkmailSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(WorkmailSpeechlet.class);
    private static final String SLOT_NUMBER = "Number";
    private static final WorkmailManager workmailManager = new WorkmailManager();

    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        return workmailManager.getBatmanResponse();
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = intent.getName();
        SpeechletResponse response = null;
        switch (intentName) {
            case "CalendarStatus": {
                response = workmailManager.getTodayCalendarStatusResponse();
                break;
            }

            case "TodaySchedule": {
                response = workmailManager.getTodaysAppointmentsResponse();
                break;
            }

            case "Email": {
                Slot numberSlot = intent.getSlot(SLOT_NUMBER);
                int emailsNumber = 0;
                if (numberSlot == null || numberSlot.getValue().equals("Last"))
                    emailsNumber = 1;
                else emailsNumber = Integer.parseInt(numberSlot.getValue());
                response = workmailManager.getEmailsResponse(emailsNumber);
            }
        }
        return response;
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }
}